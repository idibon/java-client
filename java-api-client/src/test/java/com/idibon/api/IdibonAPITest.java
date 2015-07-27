/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.util.*;
import javax.json.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class IdibonAPITest {

    @Test public void testValidateCollectionName() throws Throwable {
        Method validateCollectionName = IdibonAPI.class
            .getDeclaredMethod("validateCollectionName", String.class);
        validateCollectionName.setAccessible(true);

        assertInvalidCollectionName(validateCollectionName, "_test");
        assertInvalidCollectionName(validateCollectionName, "Has Spaces");
        assertInvalidCollectionName(validateCollectionName, "99Luftballoons");
        assertInvalidCollectionName(validateCollectionName, "Luftballoons+99");
        assertInvalidCollectionName(validateCollectionName, "A☃");

        validateCollectionName.invoke(null, "this_is_acceptable");
        validateCollectionName.invoke(null, "this-is-acceptable2");
        validateCollectionName.invoke(null, "SoIsThis---");
    }

    private void assertInvalidCollectionName(Method validator, String name)
            throws Throwable {
        try {
            try {
                validator.invoke(null, name);
                throw new RuntimeException("Should fail: " + name);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        } catch (IllegalArgumentException _) {
            // ignore; this is expected for invalid names
        }
    }
}
