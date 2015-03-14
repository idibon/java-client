/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;
import javax.json.*;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * Enables applications to configure tuning rules (a.k.a., tuning dictionaries)
 * on a task.
 *
 * The tuning rules allow analysts / application writers to define string and
 * regular expression matching patterns that, when found in a piece of content,
 * will bias the predictive confidence either for or against a specific label.
 *
 * Every label in the task may have a unique, independent tuning dictionary
 * assigned to it.
 */
public class TuningRules extends AbstractMap<Label, List<TuningRules.Rule>> {

    /**
     * An individual rule (for a specific {@link com.idibon.api.model.Label}
     * in the tuning dictionary.
     *
     * Every rule has a weight that defines how much it influences the
     * predictive confidence. Valid weights are between 0.0 and 1.0, inclusive,
     * with the following definitions:
     * <ul>
     * <li>A value of <b>0.0</b> is an absolute blacklist. If the rule matches
     * any part of a document's content, the document will never be classified
     * as the rule's {@link com.idibon.api.model.Label}</li>
     * <li>A value of<b>1.0</b> is an absolute whitelist. If the rule matches
     * any part of a document's content, the document will always be classifed
     * as the rule's {@link com.idibon.api.model.Label}</li>
     * <li>A value of <b>0.5</b> is non-predictive. The rule will not affect
     * the predictive confidence in any way. Stop words (a, an, the, ...) are
     * examples of non-predictive phrases.</li>
     * <li>Values <b>less than 0.5</b> are anti-predictive. Rules that match
     * document content will reduce the predictive confidence in that rule's
     * {@com.idibon.api.model.Label}.</li>
     * <li> Values <b>greater than 0.5</b> are positively predictive. Rules
     * that match document content will increase the predictive confidence in
     * that {@com.idibon.api.model.Label}.</li>
     * </ul>
     */
    public static abstract class Rule {
        /**
         * The weight for the rule.
         */
        public final double weight;
        /**
         * The phrase that the rule matches.
         */
        public final String phrase;
        /**
         * The {@link com.idibon.api.model.Label} that the rule affects.
         */
        public final Label label;

        /**
         * Returns true if the rule is an absolute blacklist.
         */
        public boolean isBlacklist() {
            return weight == 0.0;
        }

        /**
         * Returns true if the rule is an absolute whitelist.
         */
        public boolean isWhitelist() {
            return weight == 1.0;
        }

        /**
         * Returns true if the rule is anti-predictive.
         */
        public boolean isAntiPredictive() {
            return weight < 0.5;
        }

        /**
         * Returns true if the rule is predictive.
         */
        public boolean isPredictive() {
            return weight > 0.5;
        }

        /**
         * A rule that will matches exact substrings.
         */
        public static class Substring extends Rule {
            Substring(Label label, String phrase, double weight) {
                super(label, phrase, weight);
            }
        }

        /**
         * A rule that matches regular expressions.
         */
        public static class Regex extends Rule {
            public Pattern getPattern() {
                if (_pattern == null) {
                    _pattern = Pattern.compile(
                        this.phrase.substring(1, this.phrase.length() - 1));
                }
                return _pattern;
            }

            Regex(Label label, String phrase, double weight) {
                super(label, phrase, weight);
            }

            private volatile Pattern _pattern;
        }

        /**
         * Parses the rule from the raw JSON structure returned by the API.
         *
         * @param label The {@link com.idibon.api.model.Label} that the rule
         *        affects.
         * @param phrase The raw phrase returned from the API.
         * @param weight The raw JSON weight value returned from the API.
         *
         * @return A rule instance.
         */
        static Rule parse(Label label, String phrase, JsonValue weight)
              throws IOException {
            if (!(weight instanceof JsonNumber))
                throw new IOException("API returned invalid weight: " + weight);
            double w = ((JsonNumber)weight).doubleValue();

            if (phrase.startsWith("/") && phrase.endsWith("/")) {
                return new Regex(label, phrase, w);
            } else {
                return new Substring(label, phrase, w);
            }
        }

        protected Rule(Label label, String phrase, double weight) {
            this.label = label;
            this.phrase = phrase;
            this.weight = weight;
        }
    }

    /**
     * {@link java.util.Map#entrySet}.
     */
    public Set<Map.Entry<Label, List<Rule>>> entrySet() {
        return _rules.entrySet();
    }

    /**
     * {@link java.util.Map#get}.
     */
    @Override public List<Rule> get(Object other) {
        return _rules.get(other);
    }

    TuningRules(Map<Label, List<Rule>> rules) {
        _rules = rules;
    }

    private final Map<Label, List<Rule>> _rules;

    static TuningRules parse(Task task, JsonObject configData)
          throws IOException {
        Map<Label, List<Rule>> rules = (configData != null)
            ? readJson(task, configData.getJsonObject("tuning"))
            : Collections.<Label, List<Rule>>emptyMap();
        return new TuningRules(rules);
    }

    private static Map<Label, List<Rule>> readJson(Task t, JsonObject json)
          throws IOException {
        if (json == null)
            return Collections.emptyMap();

        Map<Label, List<Rule>> tuning = new HashMap<>();

        for (Map.Entry<String, JsonValue> l : json.entrySet()) {
            Label label = t.label(l.getKey());
            JsonObject phrases = (JsonObject)l.getValue();
            List<Rule> rules = new ArrayList<>();

            for (Map.Entry<String, JsonValue> p : phrases.entrySet())
                rules.add(Rule.parse(label, p.getKey(), p.getValue()));

            tuning.put(label, rules);
        }

        return tuning;
    }
}
