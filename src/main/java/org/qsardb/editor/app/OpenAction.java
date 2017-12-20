/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import org.qsardb.editor.common.Utils;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;
import org.qsardb.storage.memory.MemoryStorage;
import org.qsardb.storage.zipfile.ZipFileInput;

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
		MemoryStorage ms = new MemoryStorage();

		if (qdbDir.toString().endsWith(".zip")) {
			try {
				Qdb qdbZip = new Qdb(new ZipFileInput(qdbDir));
				qdbZip.copyTo(ms);
				qdbZip.close();

				Qdb qdb = new Qdb(ms);
				appFrame.getContext().loadQdb(qdb, qdbDir.toString());
				appFrame.refreshEditor();
			} catch (Exception ex) {
				Utils.showError(appFrame.getJFrame(), ex.getMessage());
				Logger.getLogger(OpenAction.class.getName()).log(Level.SEVERE, null, ex);
				return;
			}
		} else {
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
					String msg = "Can't open " + qdbDir + " - " + e.getMessage();
					Utils.showError(appFrame.getJFrame(), msg);
				}
			}
		}
	}
}
