/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import org.qsardb.model.Prediction;

public class PredictionEvent extends ContainerEvent<Prediction> {
	public PredictionEvent(Object source, PredictionEvent.Type type, Prediction prediction) {
		super(source, type, prediction);
	}
}
