/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;

public class SaveAction extends AbstractAction {
	private final QdbContext qdbContext;

	public SaveAction(QdbContext context) {
		super("Save");
		qdbContext = context;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			qdbContext.storeChanges();
			setEnabled(false);
		} catch (IOException ex) {
			Utils.showExceptionPanel("Saving error: "+ex.getMessage(), ex);
		}
	}
}
