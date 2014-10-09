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

		File workDir = new File(System.getProperty("user.dir"));
		fc.setCurrentDirectory(workDir);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(appFrame.getJFrame()) == JFileChooser.APPROVE_OPTION) {
			open(fc.getSelectedFile());
		}
	}

	private void open(File qdbDir) {
		if (newQdb && qdbDir.isDirectory() && qdbDir.list().length > 0) {
			String text = qdbDir.getName() + ": expected empty or non-existent directory";
			Utils.showError(appFrame.getJFrame(), text);
			return;
		} else if (!newQdb && !new File(qdbDir, "archive.xml").exists()) {
			String text = qdbDir.getName() + ": is not QsarDB archive";
			Utils.showError(appFrame.getJFrame(), text);
			return;
		}

		if (appFrame.checkDiscardChanges(appFrame.getJFrame())) {
			try {
				appFrame.getContext().loadArchive(qdbDir);
				AppPreferences.setLastQdbDirectory(qdbDir);
				appFrame.refreshEditor();
			} catch (IOException e) {
				String msg = "Can't open "+qdbDir+" - "+e.getMessage();
				Utils.showError(appFrame.getJFrame(), msg);
			}
		}
	}
	
}