/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.util.*;
import org.junit.*;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class MemoizeTest {

    @Test public void testWeakReferences() throws Exception {
        Memoize<Long> memo = Memoize.liveReferences(Long.class);
        WeakReference<Long> weak = null;

        do {
            Long a = new Long(12345);
            Long b = new Long(12345);
            assertThat(a, is(not(sameInstance(b))));
            assertThat(memo.memoize(a), is(sameInstance(a)));
            assertThat(memo.memoize(b), is(sameInstance(a)));
            weak = new WeakReference<>(memo.memoize(a));
            assertThat(weak.get(), is(sameInstance(a)));
        } while (false);

        /* the GC has to run a couple of times before it collects weakly-
         * reachable objects. */
        for (int i = 0; i < 4; i++) System.gc();
        assertThat(weak.get(), is(nullValue()));
    }

    @Test public void testSoftReferences() throws Exception {
        Memoize<Long> memo = Memoize.cacheReferences(Long.class);
        WeakReference<Long> weak = null;

        do {
            Long a = new Long(12345);
            Long b = new Long(12345);
            assertThat(a, is(not(sameInstance(b))));
            assertThat(memo.memoize(a), is(sameInstance(a)));
            assertThat(memo.memoize(b), is(sameInstance(a)));
            weak = new WeakReference<>(memo.memoize(a));
            assertThat(weak.get(), is(sameInstance(a)));
        } while (false);

        // softly-reachable values should live until an OOME
        for (int i = 0; i < 4; i++) System.gc();
        assertThat(weak.get(), is(not(nullValue())));

        // consume all available memory...
        List<byte[]> waste = new ArrayList<>();
        for ( ; ; ) {
            try {
                // repeatedly allocate 2MiB chunks of memory
                waste.add(new byte[2 << 20]);
            } catch (OutOfMemoryError _) {
                // stop on an OOME
                break;
            }
        }
        // since an OOME occurred, the soft reference should be purged
        assertThat(weak.get(), is(nullValue()));
    }
}
