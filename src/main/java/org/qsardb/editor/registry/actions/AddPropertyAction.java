/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.Make;
import org.qsardb.editor.events.PropertyEvent;
import org.qsardb.model.Property;

public class AddPropertyAction extends AddContainerAction<Property> {

	public AddPropertyAction(QdbContext context, String name) {
		super(context, name, context.getQdb().getPropertyRegistry());
	}

	@Override
	protected void makeContainer(String idHint) {
		container = Make.property(qdbContext, idHint);
		event = new PropertyEvent(this, PropertyEvent.Type.Add, container);
	}
	
}
