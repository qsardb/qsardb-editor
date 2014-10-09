/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.PredictionEvent;
import org.qsardb.model.Prediction;

public class RemovePredictionAction extends RemoveContainerAction<Prediction>{
	public RemovePredictionAction(QdbContext context) {
		super(context);
	}

	@Override
	protected PredictionEvent remove(ActionEvent e, Prediction prediction) {
		prediction.getRegistry().remove(prediction);
		return new PredictionEvent(this, PredictionEvent.Type.Remove, prediction);
	}
}