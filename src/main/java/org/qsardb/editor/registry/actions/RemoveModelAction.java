/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
import org.qsardb.model.PredictionRegistry;

public class RemoveModelAction extends RemoveContainerAction<Model>{
	public RemoveModelAction(QdbContext context) {
		super(context);
	}

	@Override
	protected ModelEvent remove(ActionEvent e, Model model) {
		PredictionRegistry predictions = model.getQdb().getPredictionRegistry();
		Collection<Prediction> dependencies = predictions.getByModel(model);
		if (dependencies.isEmpty()) {
			model.getRegistry().remove(model);
			return new ModelEvent(this, ModelEvent.Type.Remove, model);
		}
		
		StringBuilder sb = new StringBuilder("The following predictions depend on this model:");
		for (Prediction p: dependencies) {
			sb.append('\n').append(p.getId());
		}
		Utils.showError(e, sb.toString());
		return null;
	}
}