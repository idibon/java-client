/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.util.Iterator;
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
}
