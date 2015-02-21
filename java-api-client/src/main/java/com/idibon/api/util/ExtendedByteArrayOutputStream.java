/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * This class adds a number of data processing facilities useful for
 * handling raw HTTP byte streams (especially chunked-transfer streams)
 * to ByteArrayOutputStream, to reduce the number of memory copies and/or
 * String allocations that would be needed using standard JDK classes.
 */
public class ExtendedByteArrayOutputStream extends ByteArrayOutputStream {

    public ExtendedByteArrayOutputStream(int size) {
        super(size);
    }

    public ExtendedByteArrayOutputStream() {
        super();
    }

    /**
     * Returns the first index in the output stream where the provided byte
     * sequence appears, or -1 if it is not found.
     *
     * @param sequence Byte sequence to locate
     */
    public int indexOf(byte[] sequence) {
        return indexOf(sequence, 0);
    }

    /**
     * Returns the first index at or after the provided start position where
     * the provided sequence appears, or -1 if the sequence does not appear.
     *
     * @param sequence Byte sequence to locate
     * @param startPos The index to start the search from
     */
    public int indexOf(byte[] sequence, int startPos) {
        for (int i = startPos; i <= this.count - sequence.length; i++) {
            for (int j = 0; ; j++) {
                if (this.buf[i + j] != sequence[j]) break;
                else if (j == sequence.length - 1) return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the output stream ends with the provided byte sequence
     *
     * @param sequence The byte sequence to compare against
     */
    public boolean endsWith(byte[] sequence) {
        if (sequence.length > this.count) return false;

        int offset = this.count - sequence.length;
        return indexOf(sequence, offset) == offset;
    }


    /**
     * Returns an input stream to read all of the data that has been output
     * to this stream. Same as toInputStream(0, count);
     */
    public InputStream toInputStream() {
        return toInputStream(0, this.count);
    }

    /**
     * Returns an input stream slice of the output byte stream.
     *
     * @param index The starting offset in the byte stream
     * @param length The number of bytes to include in the returned InputStream
     */
    public InputStream toInputStream(int index, int length) {
        return this.new SliceInputStream(index, length);
    }

    /**
     * Drops the first lengthToDrop bytes from the output
     *
     * @param lengthToDrop The number of bytes to remove.
     */
    public void dropFirst(int lengthToDrop) {
        lengthToDrop = Math.min(lengthToDrop, this.count);
        int bytesToCopy = this.count - lengthToDrop;
        System.arraycopy(this.buf, lengthToDrop, this.buf, 0, bytesToCopy);
        this.count = bytesToCopy;
    }

    /**
     * Internal class returned by toInputStream
     */
    private class SliceInputStream extends InputStream {

        SliceInputStream(int offset, int length) {
            _cursor = offset;
            _limit = offset + length;
        }

        public int read() {
            if (_cursor >= _limit)
                return -1;

            byte v = ExtendedByteArrayOutputStream.this.buf[_cursor++];
            return (v + 0x100) & 0xff;
        }

        @Override public int read(byte[] b, int off, int len) {
            if (_cursor >= _limit)
                return -1;

            int limit = Math.min(available(), len);
            for (int i = 0; i < limit; )
                b[i++] = ExtendedByteArrayOutputStream.this.buf[_cursor++];
            return limit;
        }

        @Override public int available() {
            return Math.max(0, _limit - _cursor);
        }

        private final int _limit;
        private int _cursor;
    }
}
