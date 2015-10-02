/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.registry.actions.AddContainerAction;
import org.qsardb.editor.registry.actions.RemoveContainerAction;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.events.ImportEvent;
import org.qsardb.model.Container;

public abstract class RegistryView<C extends Container> {

	private final RegistryModel model;
	protected AddContainerAction newAction;
	protected RemoveContainerAction removeAction;
	private final JList<C> list = new JList<C>();

	public RegistryView(RegistryModel registryModel) {
		this.model = registryModel;
		this.list.setModel(registryModel.getListModel());
		this.list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public RegistryModel getModel() {
		return model;
	}

	public void selectContainer(String id) {
		int row = model.rowById(id);
		if (row != -1) {
			list.setSelectedIndex(row);
			list.ensureIndexIsVisible(row);
		} else {
			newAction.setIdHint(id);
		}

		removeAction.setTarget(getSelectedContainers());
	}

	public void clearSelection() {
		list.clearSelection();
		newAction.setIdHint("id");
		removeAction.setTarget(null);
	}

	public C getSelectedContainer() {
		return list.getSelectedValue();
	}

	public List<C> getSelectedContainers() {
		return list.getSelectedValuesList();
	}

	ListSelectionModel getSelectionModel() {
		return list.getSelectionModel();
	}

	public JPanel buildView() {
		JPanel panel = new ManagedJPanel(model.getQdbContext(), this, newAction);

		panel.setLayout(new BorderLayout(6, 6));
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		panel.add(buttons, BorderLayout.NORTH);
		buttons.add(new JLabel("Registry:"));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(new JButton(newAction));
		buttons.add(Box.createHorizontalStrut(4));
		buttons.add(new JButton(removeAction));

		list.setCellRenderer(new ContainerRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		panel.add(scrollPane, BorderLayout.CENTER);

		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					removeAction.setTarget(getSelectedContainers());
				}
			}
		});
		
		return panel;
	}

	public void enableMultipleSelection(boolean enable) {
		if (enable) {
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}

	protected void refreshView(ContainerEvent e) {
		getModel().refresh();

		if (e.getSource() == newAction) {
			selectContainer(e.getContainer().getId());
		} else if (e.getSource() == removeAction) {
			clearSelection();
		}
	}

	@Subscribe public void handleImport(ImportEvent e) {
		getModel().refresh();
	}
}