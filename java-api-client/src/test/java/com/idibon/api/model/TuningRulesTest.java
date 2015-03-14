/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;
import javax.json.*;
import com.idibon.api.model.Collection;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TuningRulesTest {

    @Test public void testRegex() {
        TuningRules.Rule.Regex regex =
            new TuningRules.Rule.Regex(null, "/(?i)(gender|female)/", 1.0);
        assertTrue(regex.getPattern().matcher("Male and Female").find());
        assertTrue(regex.getPattern().matcher("female").find());
        assertTrue(regex.getPattern().matcher(" GENDER STUDIES").find());
    }

    @Test public void testRuleParsing() throws Exception {
        JsonValue one =
            Util.JSON_BF.createObjectBuilder().add("a", 1.0).build().get("a");
        TuningRules.Rule r = TuningRules.Rule.parse(null, "/hip\\s*hop/", one);
        assertThat(r, is(instanceOf(TuningRules.Rule.Regex.class)));
        r = TuningRules.Rule.parse(null, "string parsing", one);
        assertThat(r, is(instanceOf(TuningRules.Rule.Substring.class)));
    }
}
