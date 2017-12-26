/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.qsardb.editor.common.Utils;

class OpenAction extends AbstractAction {
	private final QdbEditor appFrame;
	private final boolean newQdb;

	OpenAction(QdbEditor appFrame, boolean newQdb) {
		super(newQdb ? "New" : "Open");
		this.appFrame = appFrame;
		this.newQdb = newQdb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = Utils.getFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("QsarDB archive directory", "qdb"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Zipped QsarDB archive", "zip"));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		if (fc.showOpenDialog(appFrame.getJFrame()) == JFileChooser.APPROVE_OPTION) {
			open(fc.getSelectedFile());
		}
	}

	private void open(File qdbDir) {
		if (qdbDir.isDirectory()) {
			String error = checkDirectory(qdbDir);
			if (error != null) {
				Utils.showError(appFrame.getJFrame(), error);
				return;
			}
		} else if (qdbDir.isFile() && newQdb) {
			String text = qdbDir.getName() + ": file exists";
			Utils.showError(appFrame.getJFrame(), text);
			return;
		} else if (!qdbDir.exists() && newQdb) {
			if (!qdbDir.mkdirs()) {
				Utils.showError("Unable to create: "+qdbDir);
				return;
			}
		}

		if (appFrame.checkDiscardChanges(appFrame.getJFrame())) {
			try {
				appFrame.getContext().loadArchive(qdbDir);
				AppPreferences.setLastQdbDirectory(qdbDir);
				appFrame.refreshEditor();
			} catch (IOException e) {
				String msg = "Can't open " + qdbDir + " - " + e.getMessage();
				Utils.showError(appFrame.getJFrame(), msg);
			}
		}
	}

	private String checkDirectory(File qdbDir) {
		boolean notEmpty = qdbDir.list().length > 0;
		if (newQdb && notEmpty) {
			return qdbDir.getName() + ": expected an empty or non-existent directory";
		} else if (!newQdb && !new File(qdbDir, "archive.xml").exists() && notEmpty) {
			return qdbDir.getName() + ": is not a QsarDB archive";
		}
		return null;
	}
}
