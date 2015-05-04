/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http.impl;

import java.util.*;
import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.idibon.api.http.HttpInterface;

public class JdkHttpInterfaceTest {

    @Test public void testParallelismConfiguration() {
        JdkHttpInterface http = new JdkHttpInterface();
        assertThat(http.getProperty(HttpInterface.Property.ParallelRequestLimit, -1),
                   is(equalTo(JdkHttpInterface.DEFAULT_CONNECTION_LIMIT)));
        http.maxConnections(50);
        assertThat(http.getProperty(HttpInterface.Property.ParallelRequestLimit, -1),
                   is(equalTo(50)));
    }
}
