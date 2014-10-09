/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.Make;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.model.Descriptor;

public class AddDescriptorAction extends AddContainerAction<Descriptor> {

	public AddDescriptorAction(QdbContext context, String name) {
		super(context, name, context.getQdb().getDescriptorRegistry());
	}

	@Override
	protected void makeContainer(String idHint) {
		container = Make.descriptor(qdbContext, idHint);
		event = new DescriptorEvent(this, DescriptorEvent.Type.Add, container);
	}
	
}
