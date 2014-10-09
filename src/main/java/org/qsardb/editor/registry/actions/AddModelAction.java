/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.Make;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.model.Model;
import org.qsardb.model.Qdb;

public class AddModelAction extends AddContainerAction<Model> {

	public AddModelAction(QdbContext context, String name) {
		super(context, name, context.getQdb().getModelRegistry());
		enabled = hasRequiredContainers();
	}

	@Override
	protected void makeContainer(String idHint) {
		container = Make.model(qdbContext, idHint);
		event = new ModelEvent(this, ModelEvent.Type.Add, container);
	}

	private boolean hasRequiredContainers() {
		Qdb qdb = qdbContext.getQdb();
		boolean haveProperties = !qdb.getPropertyRegistry().isEmpty();
		boolean haveDescriptors = !qdb.getDescriptorRegistry().isEmpty();
		return haveProperties && haveDescriptors;
	}

	@Subscribe public void handleEnabledState(ContainerEvent e) {
		setEnabled(hasRequiredContainers());
	}
}