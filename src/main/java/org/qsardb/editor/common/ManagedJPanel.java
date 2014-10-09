/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.common;

import javax.swing.JPanel;

public class ManagedJPanel extends JPanel {
	private final QdbContext qdbContext;
	private final Object[] qdbListeners;

	public ManagedJPanel(QdbContext context, Object... listners) {
		qdbContext = context;
		qdbListeners = listners;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		for (Object listener: qdbListeners) {
			qdbContext.addListener(listener);
		}
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		for (Object listener: qdbListeners) {
			qdbContext.removeListener(listener);
		}
	}
}
