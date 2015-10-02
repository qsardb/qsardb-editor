/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.registry.actions.AddContainerAction;
import org.qsardb.editor.registry.actions.AddDescriptorAction;
import org.qsardb.editor.registry.actions.AddModelAction;
import org.qsardb.editor.registry.actions.AddPredictionAction;
import org.qsardb.editor.registry.actions.AddPropertyAction;

public class ImportDataView {
	private JDialog dialog;
	private ManagedJPanel panel;
	private final QdbContext qdbContext;

	private final AddContainerAction addPropertyAction;
	private final AddContainerAction addDescriptorAction;
	private final AddContainerAction addModelAction;
	private final AddContainerAction addPredictionAction;

	private final MappingRulesView mappingRules;
	private final ColumnPreview preview;

	private final JButton cancelButton = new JButton("Cancel");
	private final JButton importButton;

	public ImportDataView(QdbContext context, Table sourceTable) {
		qdbContext = context;
		addPredictionAction = new AddPredictionAction(context, "Prediction");
		addModelAction = new AddModelAction(context, "Model");
		addDescriptorAction = new AddDescriptorAction(context, "Descriptor");
		addPropertyAction = new AddPropertyAction(context, "Property");
		mappingRules = new MappingRulesView(qdbContext, sourceTable);
		preview = new ColumnPreview(mappingRules, sourceTable);
		importButton = new JButton(new PerformImportAction(context, sourceTable, mappingRules.getModel()));
		buildPanel();
		bindListeners();
	}

	public void show(Component parent) {
		dialog = new JDialog((Window)null, "Import data");
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setPreferredSize(new Dimension(800, 600));
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(true);
		Utils.configureWindowIcon(dialog);
		dialog.setVisible(true);
	}

	private void buildPanel() {
		panel = new ManagedJPanel(qdbContext, this, addModelAction, addPredictionAction);

		panel.setLayout(new BorderLayout());

		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
		header.add(new JLabel("New:"));
		header.add(new JButton(addPropertyAction));
		header.add(new JButton(addDescriptorAction));
		header.add(new JButton(addModelAction));
		header.add(new JButton(addPredictionAction));
		panel.add(header, BorderLayout.NORTH);

		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitter.setDividerSize(6);
		splitter.setDividerLocation(400);
		panel.add(splitter, BorderLayout.CENTER);

		splitter.setLeftComponent(mappingRules.buildView());

		splitter.setRightComponent(preview);
		
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(footer, BorderLayout.SOUTH);
		footer.add(cancelButton);
		footer.add(Box.createHorizontalStrut(10));
		footer.add(importButton);
	}

	private void bindListeners() {
		// Listener to set Id hint for new container actions.
		mappingRules.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String idHint = "";
				MappingRule m = mappingRules.getSelectedRule();
				if (m != null) {
					idHint = m.getSourceColumnHeading().replaceAll(" ", "_");
				}
				addPropertyAction.setIdHint(idHint);
				addDescriptorAction.setIdHint(idHint);
				addPredictionAction.setIdHint(idHint);
			}
		});

		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		};
		cancelButton.addActionListener(buttonListener);
		importButton.addActionListener(buttonListener);
	}

	@Subscribe public void autoMapNewContainer(ContainerEvent e) {
		Object src = e.getSource();
		if (src == addPropertyAction || src == addDescriptorAction || src == addPredictionAction || DescriptorImportAction.class.isInstance(src)) {
			mappingRules.getModel().mapByContainer(e.getContainer());
		}
	}

	@Subscribe public void onDescriptorRemoved(ContainerEvent e) {
		if (e.getType() == ContainerEvent.Type.Remove) {
			mappingRules.getModel().disableByContainer(e.getContainer());
		}
	}
}