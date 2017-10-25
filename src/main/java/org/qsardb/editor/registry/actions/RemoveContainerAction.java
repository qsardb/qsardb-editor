/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.model.Container;

abstract public class RemoveContainerAction<C extends Container> extends AbstractAction {
	private final QdbContext qdbContext;
	private List<C> targetContainers;

	public RemoveContainerAction(QdbContext context) {
		super("Remove");
		qdbContext = context;
		setEnabled(false);
	}

	public void setTarget(List<C> container) {
		targetContainers = container;
		setEnabled(container != null && !container.isEmpty());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (C container: targetContainers) {
			ContainerEvent event = remove(e, container);
			if (event != null) {
				qdbContext.fire(event);
				setTarget(null);
			}
		}
	}

	abstract protected ContainerEvent remove(ActionEvent e, C targetContainer);
}