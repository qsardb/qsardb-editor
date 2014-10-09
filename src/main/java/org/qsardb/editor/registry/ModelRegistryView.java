/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.editor.registry.actions.AddModelAction;
import org.qsardb.editor.registry.actions.RemoveModelAction;

public class ModelRegistryView extends RegistryView {

	public ModelRegistryView(QdbContext context) {
		super(new RegistryModel(context, context.getQdb().getModelRegistry()));
		newAction = new AddModelAction(context, "New");
		removeAction = new RemoveModelAction(context);
	}

	@Subscribe public void handle(ModelEvent e) {
		refreshView(e);
	}
}
