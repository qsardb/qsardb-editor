/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.PropertyEvent;
import org.qsardb.editor.registry.actions.AddPropertyAction;
import org.qsardb.editor.registry.actions.RemovePropertyAction;

public class PropertyRegistryView extends RegistryView {

	public PropertyRegistryView(QdbContext context) {
		super(new RegistryModel(context, context.getQdb().getPropertyRegistry()));
		newAction = new AddPropertyAction(context, "New");
		removeAction = new RemovePropertyAction(context);
	}

	@Subscribe public void handle(PropertyEvent e) {
		refreshView(e);
	}
}
