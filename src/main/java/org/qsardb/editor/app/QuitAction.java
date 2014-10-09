/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class QuitAction extends AbstractAction {
	private final QdbEditor appFrame;

	public QuitAction(QdbEditor appFrame) {
		super("Quit");
		this.appFrame = appFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (appFrame.checkDiscardChanges(appFrame.getJFrame())) {
			System.exit(0);
		}
	}
	
}
