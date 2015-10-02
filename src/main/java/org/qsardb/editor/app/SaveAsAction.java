/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileSystemView;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.QdbException;
import org.qsardb.model.Storage;
import org.qsardb.storage.directory.DirectoryStorage;

class SaveAsAction extends AbstractAction {
	private final QdbContext qdbContext;

	public SaveAsAction(QdbContext context) {
		super("SaveAs");
		qdbContext = context;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String[] fileNames = qdbContext.getPath().split("\\\\");
		String fileName = fileNames[fileNames.length - 1];
		if (fileName.endsWith(".zip")) {
			fileName = fileName.split("\\.")[0];
		}
		try {
			JFileChooserX fc = new JFileChooserX();
			KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
			KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			KeyStroke ctrlShiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
			InputMap map = fc.getInputMap(JFileChooser.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			map.put(enter, "traverseEnterSelection");
			map.put(ctrlEnter, "folderSelection");
			map.put(ctrlShiftEnter, "folderCcreate");
			fc.getActionMap().put("folderSelection", fc.selectFolder);
			fc.getActionMap().put("traverseEnterSelection", fc.traverseEnter);
			fc.getActionMap().put("folderCcreate", fc.folderCcreate);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.showSaveDialog(null);
			File f = fc.getSelectedFile();
			if (f == null) {
				return;
			}
			if (!f.exists()) {
				f.mkdir();
				fileName = "";
			}

			f = new File(f, fileName);
			f.mkdir();
			Storage s = new DirectoryStorage(f);
			qdbContext.getQdb().copyTo(s);

		} catch (IOException ex) {
			Logger.getLogger(SaveAsAction.class.getName()).log(Level.SEVERE, null, ex);
		} catch (QdbException ex) {
			Logger.getLogger(SaveAsAction.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	class JFileChooserX extends JFileChooser {

		public Action selectFolder = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySelection();
			}
		};
		public Action traverseEnter = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentDirectory(getSelectedFile());
			}
		};
		public Action folderCcreate = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File f = getCurrentDirectory();
				try {
					FileSystemView.getFileSystemView().createNewFolder(f);
				} catch (IOException ex) {
					Logger.getLogger(OpenAction.class.getName()).log(Level.SEVERE, null, ex);
				}
				updateUI();
			}
		};

		public void applySelection() {
			if (getSelectedFile() == null) {
				setSelectedFile(getCurrentDirectory());
			}
			super.approveSelection();
		}

		@Override
		public void setCurrentDirectory(File dir) {
			super.setCurrentDirectory(dir);
		}
	}

}
