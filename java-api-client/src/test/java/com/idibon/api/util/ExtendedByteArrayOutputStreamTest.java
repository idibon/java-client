/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.io.InputStream;

import java.util.Random;
import java.util.Arrays;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ExtendedByteArrayOutputStreamTest {

    @Test public void testIndexOfAndEndsWith() throws Exception {
        byte[] testData = new byte[1024];
        new Random().nextBytes(testData);

        // create test data where the MSB of each byte is always 1
        for (int i = 0; i < testData.length; i++)
            if (testData[i] > 0) testData[i] = (byte)-testData[i];

        ExtendedByteArrayOutputStream str = new ExtendedByteArrayOutputStream();
        str.write(testData);

        for (int i = 0; i < 64; i++) {
            byte[] chunk = Arrays.copyOfRange(testData, i, testData.length);
            assertThat(str.indexOf(chunk), is(i));
            assertTrue(str.endsWith(chunk));
        }

        for (int i = 64; i < testData.length - 1; i++) {
            byte[] chunk = Arrays.copyOfRange(testData, i, testData.length);
            assertTrue(str.endsWith(chunk));
        }

        // positive values will not be located in the data stream
        byte[] missingData = new byte[]{ (byte)1, (byte)1 };
        assertThat(str.indexOf(missingData), is(-1));
    }

    @Test public void testInputStreamSliceReadAvailable() throws Exception {
        byte[] testData = new byte[1024];
        new Random().nextBytes(testData);

        ExtendedByteArrayOutputStream str = new ExtendedByteArrayOutputStream();
        str.write(testData);

        for (int offset = 0; offset < 512; offset += 16) {
            for (int length = 1; length <= 512; length *= 2) {
                InputStream slice = str.toInputStream(offset, length);
                for (int i = 0; i < length; i++) {
                    int v = (testData[offset + i] + 0x100) & 0xff;
                    assertThat(slice.available(), is(length - i));
                    assertThat(slice.read(), is(v));
                }
                assertThat(slice.read(), is(-1));
            }
        }
    }

    @Test public void testDropFirst() throws Exception {
        byte[] testData = new byte[1024];
        new Random().nextBytes(testData);

        ExtendedByteArrayOutputStream str = new ExtendedByteArrayOutputStream();
        str.write(testData);

        int cutpoint = testData.length / 2;
        str.dropFirst(cutpoint);
        assertThat(str.size(), is(testData.length - cutpoint));

        InputStream input = str.toInputStream();
        for (int i = cutpoint; i < testData.length; i++)
            assertThat(input.read(), is((testData[i] + 0x100) & 0xff));

        str.dropFirst(testData.length - cutpoint);
        assertThat(str.size(), is(0));
    }
}
