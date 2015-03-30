/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.util.*;
import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.idibon.api.model.DocumentContent;
import static com.idibon.api.util.Adapters.wrapCharSequence;
import static com.idibon.api.util.UnicodeExtractor.extract;

public class UnicodeExtractorTest {

    @Test public void testOutOfBounds() throws Exception {
        DocumentContent doc = wrapCharSequence("This is a document.");
        assertThat(extract(doc, -5, 5), is(""));
        assertThat(extract(doc, 19, 1), is(""));
        assertThat(extract(doc, 25, 1), is(""));
        assertThat(extract(doc, -1, 8), is("This is"));
        assertThat(extract(doc, 16, 4), is("nt."));
        assertThat(extract(doc, -10, 100), is("This is a document."));
        assertThat(extract(doc, 0, -1), is(""));
    }

    @Test public void testNoSurrogates() throws Exception {
        DocumentContent doc = wrapCharSequence("This is a document.");
        assertThat(extract(doc, 0, 4), is("This"));
        assertThat(extract(doc, 0, 19), is("This is a document."));
    }

    @Test public void testSingleSurrogateMiddle() throws Exception {
        DocumentContent doc = wrapCharSequence("before \ud83d\udca9 after");
        assertThat(extract(doc, 0, 5), is("befor"));
        assertThat(extract(doc, 3, 9), is("ore \ud83d\udca9 aft"));
        assertThat(extract(doc, 9, 12), is("after"));
        assertThat(extract(doc, 7, 1), is("\ud83d\udca9"));
    }

    @Test public void testEmojiMath() throws Exception {
        DocumentContent doc = wrapCharSequence("\ud83d\udc68+\ud83c\udf63=\ud83d\udca9");
        assertThat(extract(doc, -1, 2), is("\ud83d\udc68"));
        assertThat(extract(doc, 0, 1), is("\ud83d\udc68"));
        assertThat(extract(doc, 0, 3), is("\ud83d\udc68+\ud83c\udf63"));
        assertThat(extract(doc, -2, 9), is("\ud83d\udc68+\ud83c\udf63=\ud83d\udca9"));
        assertThat(extract(doc, 4, 1), is("\ud83d\udca9"));
        assertThat(extract(doc, 1, 3), is("+\ud83c\udf63="));
    }

    @Test public void testBrokenUtf16() throws Exception {
        DocumentContent doc = wrapCharSequence("Broken: \ud83d\ufffd!");
        assertThat(extract(doc, 0, 8), is("Broken: "));
        assertThat(extract(doc, 8, 1), is("\ud83d"));
        assertThat(extract(doc, 8, 2), is("\ud83d\ufffd"));
        assertThat(extract(doc, 9, 2), is("\ufffd!"));
    }
}
