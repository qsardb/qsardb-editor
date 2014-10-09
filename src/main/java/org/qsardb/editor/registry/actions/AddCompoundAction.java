/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.Make;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.model.Compound;

public class AddCompoundAction extends AddContainerAction<Compound> {

	public AddCompoundAction(QdbContext context, String actionName) {
		super(context, actionName, context.getQdb().getCompoundRegistry());
	}

	@Override
	protected void makeContainer(String idHint) {
		container = Make.compound(qdbContext, idHint);
		event = new CompoundEvent(this, CompoundEvent.Type.Add, container);
	}
}