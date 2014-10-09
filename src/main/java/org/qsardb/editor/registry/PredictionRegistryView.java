/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.PredictionEvent;
import org.qsardb.editor.registry.actions.AddPredictionAction;
import org.qsardb.editor.registry.actions.RemovePredictionAction;

public class PredictionRegistryView extends RegistryView {

	public PredictionRegistryView(QdbContext context) {
		super(new RegistryModel(context, context.getQdb().getPredictionRegistry()));
		newAction = new AddPredictionAction(context, "New");
		removeAction = new RemovePredictionAction(context);
	}

	@Subscribe public void handle(PredictionEvent e) {
		refreshView(e);
	}
}
