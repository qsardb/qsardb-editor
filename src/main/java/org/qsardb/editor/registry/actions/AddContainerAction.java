/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.model.*;

public abstract class AddContainerAction<C extends Container> extends AbstractAction {

	protected final QdbContext qdbContext;
	private final ContainerRegistry containerRegistry;
	private String idHint = "id";
	protected C container;
	protected ContainerEvent event;

	public AddContainerAction(QdbContext context, String name, ContainerRegistry registry) {
		super(name);
		qdbContext = context;
		containerRegistry = registry;
	}

	protected abstract void makeContainer(String idHint);

	@Override
	public void actionPerformed(ActionEvent e) {
		idHint = idHint.replace(" ", "_");
		idHint = idHint.replace("/", "_");
		idHint = idHint.replace(":", "_");
		idHint = idHint.replace("<", "_lt_");
		idHint = idHint.replace(">", "_gt_");
		idHint = idHint.replace("*", ".");
		if (!IdUtil.validate(idHint)) {
			idHint = "id";
		}

		makeContainer(idHint);
		if (container != null && event != null) {
			try {
				containerRegistry.add(container);
				qdbContext.fire(event);
			} catch (IllegalArgumentException ex) {
				Utils.showError(e, ex.getMessage());
			}
		}
	}

	public void setIdHint(String idHint) {
		this.idHint = idHint;
	}
}