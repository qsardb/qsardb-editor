/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.io.FileUtils;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.QdbEvent;
import org.qsardb.editor.importer.ImportAction;
import org.qsardb.editor.registry.Edit;
import org.qsardb.editor.validator.ValidateArchiveView;
import org.qsardb.editor.visualizer.VisualizerTab;

/**
 * Provides main editor for working with QDB archives.
 */
public class QdbEditor implements Runnable {
	@Parameter (
		names = {"--dir"},
		description = "QDB directory"
	)
	private File qdbDir;

	private final QdbContext qdbContext;

	private final JFrame frame = new JFrame("QsarDB");
	private final JTabbedPane tabs = new JTabbedPane();
	private final ImportAction importAction;
	private final SaveAction saveAction;
	private final SaveAsAction saveAsAction;
	private final ExportAction exportAction;

	private QdbEditor(QdbContext context) {
		qdbContext = context;
		importAction = new ImportAction(context);
		saveAction = new SaveAction(context);
		saveAsAction = new SaveAsAction(context);
		exportAction = new ExportAction(context);
	}

	public void show() {
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (checkDiscardChanges(frame)) {
					frame.dispose();
				}
			}
		});

		JPanel view = buildView();
		frame.getContentPane().add(view, BorderLayout.CENTER);

		Utils.configureWindowIcon(frame);
		frame.setSize(940, 600);
		frame.setVisible(true);
	}

	public QdbContext getContext() {
		return qdbContext;
	}

	public Component getJFrame() {
		return frame;
	}

	private JPanel buildView() {
		JPanel panel = new ManagedJPanel(qdbContext, this);

		panel.setLayout(new BorderLayout());
		panel.add(createToolbar(), BorderLayout.NORTH);
		panel.add(tabs, BorderLayout.CENTER);

		refreshEditor();
		return panel;
	}

	private JPanel createToolbar() {
		JPanel toolbar = new JPanel();
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		toolbar.add(new JButton(new OpenAction(this, true)));
		toolbar.add(new JButton(new OpenAction(this, false)));
		toolbar.add(new JButton(importAction));
		toolbar.add(new JButton(saveAction));
		toolbar.add(new JButton(saveAsAction));
		toolbar.add(new JButton(exportAction));
		JButton quitButton = new JButton(new QuitAction(this));
		toolbar.add(quitButton);
		return toolbar;
	}

	void refreshEditor() {
		boolean haveQdb = qdbContext.getQdb() != null;
		importAction.setEnabled(haveQdb);
		saveAction.setEnabled(false);
		exportAction.setEnabled(haveQdb);

		if (haveQdb) {
			tabs.removeAll();
			final VisualizerTab v = new VisualizerTab(qdbContext);
			tabs.addTab("Archive", new EditArchiveView(qdbContext).createView());
			tabs.addTab("Compounds", Edit.compounds(qdbContext).createView());
			tabs.addTab("Properties", Edit.properties(qdbContext).createView());
			tabs.addTab("Descriptors", Edit.descriptors(qdbContext).createView());
			tabs.addTab("Models", Edit.models(qdbContext).createView());
			tabs.addTab("Predictions", Edit.predictions(qdbContext).createView());
			tabs.addTab("Validation", new ValidateArchiveView(qdbContext).createView());
			tabs.addTab("Visualizer", v.make());
			tabs.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (tabs.getSelectedIndex() == 7 && v.isUpdateNeeded()) {
						v.refreshView();
						v.setUpdateNeeded(false);
					}
				}
			});
			tabs.setMnemonicAt(0, KeyEvent.VK_1);
			tabs.setMnemonicAt(1, KeyEvent.VK_2);
			tabs.setMnemonicAt(2, KeyEvent.VK_3);
			tabs.setMnemonicAt(3, KeyEvent.VK_4);
			tabs.setMnemonicAt(4, KeyEvent.VK_5);
			tabs.setMnemonicAt(5, KeyEvent.VK_6);
			tabs.setMnemonicAt(6, KeyEvent.VK_7);
			tabs.setMnemonicAt(7, KeyEvent.VK_8);

			frame.setTitle("QsarDB: "+qdbContext.getPath());
		}
	}

	public boolean checkDiscardChanges(Component frame) {
		if (qdbContext.isSavingNeeded()) {
			String msg = "There are unsaved changes. If you continue, these changes will be lost.";
			Object[] options = {"OK", "Cancel"};
			int r = JOptionPane.showOptionDialog(frame, msg, "Discard changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (r == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}

	@Subscribe public void toggleSaveButton(QdbEvent e) {
		saveAction.setEnabled(qdbContext.isSavingNeeded());
	}

	@Override
	public void run() {
		if (qdbDir == null) {
			qdbDir = AppPreferences.getLastQdbDirectory();
		}

		if (qdbDir.isDirectory()) {
			try {
				qdbContext.loadArchive(qdbDir);
			} catch (IOException e) {
				String msg = "Can't open "+qdbDir+" - "+e.getMessage();
				Utils.showError((Component)null, msg);
			}
		}

		show();
	}
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

		QdbContext context = new QdbContext();
		QdbEditor app = new QdbEditor(context);

		JCommander commander = new JCommander(app);
		commander.setProgramName(app.getClass().getName());
		try {
			commander.parse(args);
		} catch (ParameterException e) {
			commander.usage();
			System.exit(1);
		}

		Thread t = new Thread() {

			@Override
			public void run() {
				File f = new File(System.getProperty("user.dir").concat("//resources"));
				if (f.exists()) {
					try {
						FileUtils.deleteDirectory(f);
					} catch (IOException ex) {
						Logger.getLogger(QuitAction.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(t);
		SwingUtilities.invokeLater(app);
	}
}