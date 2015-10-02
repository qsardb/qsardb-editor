/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.Make;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.editor.events.PredictionEvent;
import org.qsardb.model.Prediction;

public class AddPredictionAction extends AddContainerAction<Prediction> {

	public AddPredictionAction(QdbContext context, String name) {
		super(context, name, context.getQdb().getPredictionRegistry());
		enabled = !qdbContext.getQdb().getModelRegistry().isEmpty();
	}

	@Override
	protected void makeContainer(String idHint) {
		container = Make.prediction(qdbContext, idHint);
		qdbContext.fire(new ModelEvent(this, ModelEvent.Type.Add, null));
		event = new PredictionEvent(this, PredictionEvent.Type.Add, container);
	}

	@Subscribe public void handleEnabledState(ContainerEvent e) {
		setEnabled(!qdbContext.getQdb().getModelRegistry().isEmpty());
	}
	
}
