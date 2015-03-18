/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.util.Iterator;
import javax.json.JsonObject;
import com.idibon.api.model.*;

/**
 * Static utility class for adapting between data types.
 */
public final class Adapters {

    /**
     * Adapts between {@link com.idibon.api.model.AnnotationBuilder} instances
     * and {@link com.idibon.api.model.Annotation}.
     *
     * @param builders A list of AnnotationBuilder objects ready to build into
     *        annotations.
     * @return An iterable that lazily calls build on each AnnotationBuilder
     *         to construct its annotation.
     */
    public static <T extends Annotation> Iterable<T> buildAnnotations(
          final Iterable<? extends AnnotationBuilder<T>> builders) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return Adapters.buildAnnotations(builders.iterator());
            }
        };
    }

    /**
     * Adapts between {@link com.idibon.api.model.AnnotationBuilder} instances
     * and {@link com.idibon.api.model.Annotation}.
     *
     * @param builders An iteration of AnnotationBuilder objects ready to build
     *        into annotations.
     * @return An iterator that lazily calls build on each AnnotationBuilder
     *         to construct its annotation.
     */
    public static <T extends Annotation> Iterator<T> buildAnnotations(
          final Iterator<? extends AnnotationBuilder<T>> builders) {
        return new Iterator<T>() {
            public boolean hasNext() {
                return builders.hasNext();
            }
            public T next() {
                return builders.next().build();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Adapts between {@link java.lang.CharSequence} instances and
     * {@link com.idibon.api.model.DocumentContent}.
     *
     * This can be used to wrap Java Strings in a format suitable for uploading
     * either as ephemeral predictions or for permanent storage under server-
     * assigned names.
     *
     * @param sequences An iteration of {@link java.lang.CharSequence}
     *        instances.
     * @return An iterable that lazily wraps each CharSequence in a
     *         {@link com.idibon.api.model.DocumentContent} facade
     */
    public static Iterable<DocumentContent> wrapCharSequences(
          final Iterable<? extends CharSequence> sequences) {
        return new Iterable<DocumentContent>() {
            public Iterator<DocumentContent> iterator() {
                return Adapters.wrapCharSequences(sequences.iterator());
            }
        };
    }

    /**
     * Adapts between {@link java.lang.CharSequence} instances and
     * {@link com.idibon.api.model.DocumentContent}.
     *
     * This can be used to wrap Java Strings in a format suitable for uploading
     * either as ephemeral predictions or for permanent storage under server-
     * assigned names.
     *
     * @param sequences An iteration of {@link java.lang.CharSequence}
     *        instances.
     * @return An iterator that lazily wraps each CharSequence in a
     *         {@link com.idibon.api.model.DocumentContent} facade
     */
    public static Iterator<DocumentContent> wrapCharSequences(
          final Iterator<? extends CharSequence> sequences) {
        return new Iterator<DocumentContent>() {
            public boolean hasNext() {
                return sequences.hasNext();
            }
            public DocumentContent next() {
                return new CharSequenceContent(sequences.next());
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Simple facade to return a CharSequence as unnamed DocumentContent
     */
    private static class CharSequenceContent implements DocumentContent {
        CharSequenceContent(CharSequence sequence) {
            _sequence = sequence;
        }
        public String getContent() {
            return _sequence.toString();
        }
        public JsonObject getMetadata() {
            return null;
        }

        private final CharSequence _sequence;
    }
}