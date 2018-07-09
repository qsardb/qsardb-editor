/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.qsardb.editor.common.Utils;
import org.qsardb.model.QdbException;
import org.qsardb.storage.directory.DirectoryStorage;

class SaveACopyAction extends AbstractAction {
	private final QdbEditor appFrame;

	public SaveACopyAction(QdbEditor appFrame) {
		super("Save a copy");
		this.appFrame = appFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = Utils.getFileChooser();

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (fc.showSaveDialog(appFrame.getJFrame()) == JFileChooser.APPROVE_OPTION) {
			File dir = fc.getSelectedFile();
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Utils.showError(e, "Unable to create directory: "+dir);
					return;
				}
			}

			if (dir.list().length > 0) {
				Utils.showError(e, "Non-empty directory: "+dir);
				return;
			}

			try {
				DirectoryStorage storage = new DirectoryStorage(dir);
				appFrame.getContext().getQdb().copyTo(storage);
			} catch (IOException | QdbException ex) {
				Utils.showExceptionPanel("Unable to save a copy: "+dir, ex);
			}
		}
	}
}