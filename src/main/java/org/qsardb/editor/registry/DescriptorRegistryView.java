/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.editor.registry.actions.AddDescriptorAction;
import org.qsardb.editor.registry.actions.RemoveDescriptorAction;

public class DescriptorRegistryView extends RegistryView {

	public DescriptorRegistryView(QdbContext context) {
		super(new RegistryModel(context, context.getQdb().getDescriptorRegistry()));
		newAction = new AddDescriptorAction(context, "New");
		removeAction = new RemoveDescriptorAction(context);
	}

	@Subscribe public void handle(DescriptorEvent e) {
		refreshView(e);
	}
}
