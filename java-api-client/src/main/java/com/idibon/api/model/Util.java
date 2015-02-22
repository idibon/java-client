/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;
import javax.json.*;

final class Util {

    /**
     * Creates a JsonArray from an array of Java Strings.
     *
     * @param strings Array of strings
     * @returns JsonArray
     */
    static JsonArray toJson(String[] strings) {
        JsonArrayBuilder b = JSON_BF.createArrayBuilder();
        for (String s : strings) {
            if (s == null) b.addNull();
            else b.add(s);
        }
        return b.build();
    }

    /**
     * Creates a JsonArray from an iterable collection of strings.
     *
     * @param strings
     * @returns JsonArray
     */
    static JsonArray toJson(Iterable<String> strings) {
        JsonArrayBuilder b = JSON_BF.createArrayBuilder();
        for (String s : strings) {
            if (s == null) b.addNull();
            else b.add(s);
        }
        return b.build();
    }

    /**
     * Common JsonReaderFactory for this package.
     */
    static final JsonBuilderFactory JSON_BF = Json.createBuilderFactory(null);
}
