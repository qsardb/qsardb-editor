/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.model.Container;

abstract public class RemoveContainerAction<C extends Container> extends AbstractAction {
	private final QdbContext qdbContext;
	private C targetContainer;

	public RemoveContainerAction(QdbContext context) {
		super("Remove");
		qdbContext = context;
		setEnabled(false);
	}

	public void setTarget(C container) {
		targetContainer = container;
		setEnabled(container != null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String id = targetContainer != null ? targetContainer.getId() : "null";
		if (targetContainer != null) {
			ContainerEvent event = remove(e, targetContainer);
			if (event != null) {
				setTarget(null);
				qdbContext.fire(event);
			}
		} 
	}

	abstract protected ContainerEvent remove(ActionEvent e, C targetContainer);
}
