/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import static com.idibon.api.model.Util.JSON_BF;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Iterator;
import javax.json.*;
import com.idibon.api.util.Either;

/**
 * Generates predictions for one or more predictable items.
 */
class PredictionIterableTrivial<T extends Prediction<T>>
      implements PredictionIterable<T> {

    public Iterator<Either<APIFailure<DocumentContent>, T>> iterator() {
        return this.new Iter();
    }

    /**
     * Returns the key words and phrases from the document content that
     * affected the prediction.
     *
     * This is the same as calling
     * {@link com.idibon.api.model.PredictionIterableTrivial#withSignificantFeatures(double)}
     * with a value of DEFAULT_FEATURE_THRESHOLD.
     *
     * @return This
     */
    public PredictionIterableTrivial<T> withSignificantFeatures() {
        return withSignificantFeatures(DEFAULT_FEATURE_THRESHOLD);
    }

    /**
     * Returns words and phrases from the document content that affected
     * the prediction above the provided threshold.
     *
     * @param threshold Defines the cutoff threshold to include features.
     *        Should be 0.0 - 1.0.
     * @return This
     */
    public PredictionIterableTrivial<T> withSignificantFeatures(double threshold) {
        _includeFeatures = true;
		
        try {
			_trivialPrediction = getTrivialAcceptPrediction();
		} catch (IOException e) {
			throw new Error("Error retrieving task labels");			
		}
        
        return this;
    }

    PredictionIterableTrivial(Class<T> clazz, Task target,
          Iterable<? extends DocumentContent> items) {
        try {
            _constructor = clazz.getDeclaredConstructor(
                JsonArray.class, DocumentContent.class, Task.class);
        } catch (Exception ex) {
            throw new Error("Impossible");
        }

        _target = target;
        _items = items;

		try {
			_trivialPrediction = getTrivialAcceptPrediction();
		} catch (IOException e) {
			throw new Error("Error retrieving task labels");			
		}
    }
    
    /**
     * Private helper function to generate a stock prediction tree for use
     * in trivial accept cases. Returns an array consisting of a single prediction
     * with confidence levels set to 1.0 for all labels.
     */
    private JsonArray getTrivialAcceptPrediction() throws IOException {

    	JsonObjectBuilder featuresBuilder = JSON_BF.createObjectBuilder();
    	JsonObjectBuilder classesBuilder = JSON_BF.createObjectBuilder();
    	JsonObjectBuilder predictionBuilder = JSON_BF.createObjectBuilder();
    	JsonArrayBuilder predictionArrayBuilder = JSON_BF.createArrayBuilder();
    	String labelName = DEFAULT_LABEL_NAME;
    	
    	for (Label label: _target.getLabels()) {
    		labelName = label.getName();
    		classesBuilder.add(labelName, TRIVIAL_ACCEPT_CONFIDENCE);
    	}

		/* Use an actual label as the class name. Normally, this would be the label with the
    	 * highest confidence val, but since they are all 1.0 it doesn't matter */
    	predictionBuilder.add("class", labelName);
    	predictionBuilder.add("confidence", TRIVIAL_ACCEPT_CONFIDENCE);
    	predictionBuilder.add("classes", classesBuilder);
    	
        if (_includeFeatures) {
        	predictionBuilder.add("features", featuresBuilder);
        }
    	    	
    	// Shove the single prediction into an array
    	predictionArrayBuilder.add(predictionBuilder);    	    	
    	return predictionArrayBuilder.build();
    }

    // Include significant features with the results?
    private boolean _includeFeatures = false;

    private static final double TRIVIAL_ACCEPT_CONFIDENCE = 1.0;
    private static final String DEFAULT_LABEL_NAME = "Label1";
    
    // The task being predicted against
    private final Task _target;

    // Type of predictions (span vs document) being performed
    private final Constructor<T> _constructor;

    // The items that will have a trivial prediction
    private final Iterable<? extends DocumentContent> _items;
    
    // The predefined contents of the trivial prediction
    private JsonArray _trivialPrediction;
    
    /**
     * Iterates over predictable items and returns the trivial prediction
     * for each.
     */
    private class Iter
          implements Iterator<Either<APIFailure<DocumentContent>, T>> {
        private Iter() {
            _itemIt = _items.iterator();
        }

        public boolean hasNext() {
            return _itemIt.hasNext();
        }

        public Either<APIFailure<DocumentContent>, T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            
            try {
                T prediction = _constructor.newInstance(
                    _trivialPrediction, _itemIt.next(), _target);
                return Either.right(prediction);
            } catch (InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException _) {
                throw new Error("Impossible");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private final Iterator<? extends DocumentContent> _itemIt;
    }
}
