/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
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
		JFileChooserX fc = new JFileChooserX(newQdb);
		FileFilter filter = new FileNameExtensionFilter(null, "zip");
		fc.setFileFilter(filter);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("QsarDB archive directory", "qdb"));

		File workDir = new File(System.getProperty("user.dir"));
		fc.setCurrentDirectory(workDir);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

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
		if (fc.showOpenDialog(appFrame.getJFrame()) == JFileChooser.APPROVE_OPTION) {
			if (fc.getSelectedFile().exists() && newQdb) {
				File f = fc.getSelectedFile();
				try {
					f = FileSystemView.getFileSystemView().createNewFolder(f);
				} catch (IOException ex) {
					Logger.getLogger(OpenAction.class.getName()).log(Level.SEVERE, null, ex);
				}
				open(f);
				return;
			}
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

	class JFileChooserX extends JFileChooser {

		private JButton approveButton;
		private boolean newQdb;

		public JFileChooserX(boolean newQdb) {
			this.newQdb = newQdb;
			if (approveButton == null) {
				approveButton = lookupButton(JFileChooserX.this, getUI().getApproveButtonText(this));
			}
			approveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					applySelection();
				}
			});
		}

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

		private JButton lookupButton(Container c, String text) {
			JButton temp = null;
			for (Component comp : c.getComponents()) {
				if (comp == null) {
					continue;
				}
				if (comp instanceof JButton && (temp = (JButton) comp).getText() != null && temp.getText().equals(text)) {
					return temp;
				} else if (comp instanceof Container) {
					if ((temp = lookupButton((Container) comp, text)) != null) {
						return temp;
					}
				}
			}
			return temp;
		}
	}

}