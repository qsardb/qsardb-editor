/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.editor.registry.actions.AddCompoundAction;
import org.qsardb.editor.registry.actions.RemoveCompoundAction;

public class CompoundRegistryView extends RegistryView {

	public CompoundRegistryView(QdbContext context) {
		super(new RegistryModel(context, context.getQdb().getCompoundRegistry()));
		newAction = new AddCompoundAction(context, "New");
		removeAction = new RemoveCompoundAction(context);
	}

	@Subscribe public void handle(CompoundEvent e) {
		refreshView(e);
	}
}