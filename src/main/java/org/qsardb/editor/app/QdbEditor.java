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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.QdbEvent;
import org.qsardb.editor.importer.ImportAction;
import org.qsardb.editor.registry.Edit;
import org.qsardb.editor.validator.ValidateArchiveView;

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
	private final ExportAction exportAction;

	private QdbEditor(QdbContext context) {
		qdbContext = context;
		importAction = new ImportAction(context);
		saveAction = new SaveAction(context);
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
			tabs.addTab("Archive", new EditArchiveView(qdbContext).createView());
			tabs.addTab("Compounds", Edit.compounds(qdbContext).createView());
			tabs.addTab("Properties", Edit.properties(qdbContext).createView());
			tabs.addTab("Descriptors", Edit.descriptors(qdbContext).createView());
			tabs.addTab("Models", Edit.models(qdbContext).createView());
			tabs.addTab("Predictions", Edit.predictions(qdbContext).createView());
			tabs.addTab("Validation", new ValidateArchiveView(qdbContext).createView());

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

		SwingUtilities.invokeLater(app);
	}
}