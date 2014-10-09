/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.editor.container.ModelModel;
import org.qsardb.editor.registry.Select;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;

public class EditMlrView extends EditCargoView {
	private final ModelModel containerModel;

	private final EditMlrModel mlrModel = new EditMlrModel();
	private final JTable table = new JTable(mlrModel);

	public EditMlrView(ModelModel model) {
		super(model, PMMLCargo.ID);
		containerModel = model;
	}

	@Override
	protected JComponent buildContentPanel() {
		JPanel view = new JPanel(new BorderLayout());
		view.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		try {
			mlrModel.setModel(containerModel);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Failed to parse PMML: "+ex.getMessage());
		}

		table.getColumnModel().getColumn(0).setMaxWidth(120);
		table.getColumnModel().getColumn(1).setMaxWidth(120);

		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		view.add(toolbar, BorderLayout.NORTH);
		toolbar.add(new JButton(new AbstractAction("Add descriptor") {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Descriptor> descs = Select.descriptors(containerModel.getQdbContext(), "id", mlrModel.getDescriptors());
				int nextRow = mlrModel.getRowCount();
				for (Descriptor d: descs) {
					mlrModel.addDescriptor(d);
				}
				if (!descs.isEmpty()) {
					if (table.editCellAt(nextRow, 0)) {
						table.getEditorComponent().requestFocusInWindow();
					}
				}
			}
		}));
		final AbstractAction removeAction = new AbstractAction("Remove descriptor") {
			@Override
			public void actionPerformed(ActionEvent e) {
				mlrModel.removeDescriptor(table.getSelectedRow());
			}
		};
		removeAction.setEnabled(false);
		toolbar.add(new JButton(removeAction));

		view.add(new JScrollPane(table), BorderLayout.CENTER);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					removeAction.setEnabled(table.getSelectedRow() > 0);
				}
			}
		});

		return view;
	}

	@Override
	protected Payload createPayload() throws QdbException {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		try {
			return mlrModel.getPayload();
		} catch (NumberFormatException ex) {
			throw new QdbException("Invalid equation: "+ex.getMessage());
		}
	}
}