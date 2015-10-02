/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.qsardb.editor.common.QdbContext;
import org.apache.commons.io.FileUtils;
import org.qsardb.editor.common.Utils;

public class SaveAction extends AbstractAction {
	private final QdbContext qdbContext;

	public SaveAction(QdbContext context) {
		super("Save");
		qdbContext = context;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (qdbContext.getPath().endsWith(".zip")) {
			File f = new File(qdbContext.getPath());
			if (f.exists()) {
				File backup = new File(f.getPath() + ".bak");
				if (backup.exists()) {
					backup.delete();
				}
				try {
					FileUtils.copyFile(f, backup);
				} catch (IOException ex) {
					Utils.showExceptionPanel(ex.getMessage(), ex);
					Logger.getLogger(SaveAction.class.getName()).log(Level.SEVERE, null, ex);
					return;
				}
			}
			try {
				qdbContext.storeChangesZip(f);
			} catch (IOException ex) {
				Utils.showExceptionPanel(ex.getMessage(), ex);
				Logger.getLogger(SaveAction.class.getName()).log(Level.SEVERE, null, ex);
				return;
			}
			setEnabled(false);

		} else {

			try {
				qdbContext.storeChanges();
				setEnabled(false);
			} catch (IOException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}
}
