/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.util.*;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.idibon.api.model.DocumentContent;

/**
 * The UnicodeExtractor is used to convert from the offsets and lengths
 * used by the Idibon API (Unicode codepoints, i.e., int32 indices into
 * a UTF-32 encoded bytestream) to the char indices in Java Strings
 * (UTF-16).
 *
 * All methods in this class are thread-safe.
 */
public class UnicodeExtractor {

    /**
     * Extracts the String that starts at the offset'th unicode codepoint
     * in the document content
     * ({@link com.idibon.api.model.DocumentContent#getContent}), and
     * is length codepoints long.
     *
     * Note that because Java measures string length in UTF-16 values,
     * calling length() on the returned string may result in a larger
     * value than the length paramter.
     *
     * <i>Note</i>: This method truncates the returned Strings if the offset
     * or length are out-of-bounds rather than throwing an exception.
     *
     * @param document The document content from which the text (probably
     *          either an {@link com.idibon.api.model.Annotation.SpanAssignment}
     *          or {@link com.idibon.api.model.SpanPrediction}) should be
     *          extracted.
     * @param offset The offset, measured in unicode codepoints, of the start
     *          of the text to extract.
     * @param length The length, measured in unicode codepoints, of the
     *          extracted text.
     * @return The extracted text.
     */
    public static String extract(DocumentContent document,
          int offset, int length) throws IOException {

        if (offset < 0) {
            length += offset;
            offset = 0;
        }

        if (length <= 0) return "";

        String content = document.getContent();
        /* pairs will store the initial index of every surrogate pair character
         * in the text, ordered by increasing offset in the text. */
        int[] pairs = findSurrogates(document, content);

        /* each surrogate pair will have 2 offsets in the string
         * (String.charAt). to compute the java string location given an
         * annotation or prediction's code point index, count the number
         * of surrogate pair characters that occur before the span begins
         * (offsAdj), and the number before the span ends (endAdj). these
         * adjustments can just be added directly to the start and end
         * locations to get the java positions. */

        int offsAdj = 0;
        /* loop until we reach a surrogate pair offset that is at or after the
         * start of the span. */
        for ( ; offsAdj < pairs.length && offset > pairs[offsAdj]; offsAdj++) ;

        int endAdj = offsAdj;
        /* and continue looping until we reach the first surrogate-pair offset
         * after the span. */
        for (int end = offset + length - 1;
               endAdj < pairs.length && end >= pairs[endAdj]; endAdj++) ;

        int start = Math.min(content.length(), offset + offsAdj);
        int end = Math.min(content.length(), offset + length + endAdj);

        return content.substring(start, end);
    }

    /**
     * Returns an array of the codepoint offset of all the initial surrogate-
     * pair values in a UTF-16 encoded string.
     */
    static int[] findSurrogates(DocumentContent document, String content) {
        // check if the data is already cached...
        LOCK.readLock().lock();
        try {
            int[] cached = SURROGATES.get(document);
            if (cached != null) return cached;
        } finally {
            LOCK.readLock().unlock();
        }

        int count = 0;

        // count up the number of surrogates
        for (int i = 0, len = content.length(); i < len; i++) {
            if (isValidSurrogatePair(content, i)) {
                count++;  // found a surrogate pair
                i++;      // skip over the second character in the pair

            }
        }

        int[] surrogates = (count == 0) ? NO_SURROGATES : new int[count];
        for (int i = 0, j = 0; j < surrogates.length; i++) {
            if (isValidSurrogatePair(content, i)) {
                /* Transform from UTF-16 indices to code point indices by
                 * subtracting the number of surrogate pair sequences
                 * encountered so far. */
                surrogates[j] = i - j;
                i++;
                j++;
            }
        }

        LOCK.writeLock().lock();
        try {
            SURROGATES.put(document, surrogates);
        } finally {
            LOCK.writeLock().unlock();
        }

        return surrogates;
    }

    /**
     * Tests if the 2-char sequence starting at index i in seq is a UTF-16
     * surrogate pair sequence.
     *
     * @param seq The text to test
     * @param index The index to test
     * @return true if index is the start of a surrogate-pair sequence.
     */
    static boolean isValidSurrogatePair(CharSequence seq, int index) {
        char ch = seq.charAt(index);
        if (ch < 0xd800 || ch > 0xdbff) return false;
        ch = seq.charAt(index + 1);
        return ch >= 0xdc00 && ch <= 0xdfff;
    }

    private UnicodeExtractor() { }

    /**
     * Keeps track of all of the initial characters (0xd800 - 0xdbff in
     * well-formed UTF-16 streams) in a surrogate pair sequence.
     *
     * Stored in a WeakHashMap so that the lookup table is destroyed when the
     * last reference to the DocumentContent instance it matches goes out-of-
     * scope.
     */
    private static final Map<DocumentContent, int[]> SURROGATES =
        new WeakHashMap<>();

    /**
     * Provides thread-safe access to SURROGATES
     */
    private static final ReentrantReadWriteLock LOCK =
        new ReentrantReadWriteLock();

    /**
     * Most strings won't have any surrogates, so don't bother instantiating
     * a unique zero-length array for each.
     */
    private static final int[] NO_SURROGATES = new int[0];
}
