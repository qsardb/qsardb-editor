/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.cargo.map.StringFormat;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.ByteArrayPayload;
import org.qsardb.model.Payload;
import org.qsardb.model.Property;
import org.qsardb.model.QdbException;

class EditValuesView extends EditCargoView {
	private JScrollPane jsp;
	private final EditTextViewTableModel tm;
	private final JTable table;

	public EditValuesView(ContainerModel model, String cargoId) {
		super(model, cargoId);
		tm = new EditTextViewTableModel(model);
		table = new JTable(tm);
	}

	@Override
	protected JComponent buildContentPanel() {
		JPanel content = new JPanel(new BorderLayout());

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));

		final JButton addRowBelow = new JButton(new AbstractAction("Add row below") {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
				int i = table.getSelectedRow() + 1;
				tm.addRow(i);
				table.setRowSelectionInterval(i, i);
			}
		});
		addRowBelow.setEnabled(table.getRowCount() == 0);
		buttons.add(addRowBelow);

		final JButton addRowAbove = new JButton(new AbstractAction("Add row above") {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
				int i = table.getSelectedRow();
				tm.addRow(i);
				table.setRowSelectionInterval(i, i);
			}
		});
		addRowAbove.setEnabled(false);
		buttons.add(addRowAbove);

		final JButton removeRow = new JButton(new AbstractAction("Remove row") {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
				int[] rows = table.getSelectedRows();
				tm.removeRows(rows);
			}
		});
		removeRow.setEnabled(false);
		buttons.add(removeRow);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int n = table.getSelectedRows().length; 
					removeRow.setEnabled(n > 0);
					addRowBelow.setEnabled(n == 1);
					addRowAbove.setEnabled(n == 1);
				}
			}
		});

		DefaultCellEditor singleclick = new DefaultCellEditor(new JTextField());
		singleclick.setClickCountToStart(1);
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.setDefaultEditor(table.getColumnClass(i), singleclick);
		}

		jsp = new JScrollPane(table);
		jsp.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		content.add(buttons, BorderLayout.NORTH);
		content.add(jsp, BorderLayout.CENTER);
		return content;
	}

	@Override
	protected Payload createPayload() throws QdbException {
		stopCellEditing();

		Property tmpContainer = new Property(model.getContainer().getId());
		tmpContainer.setName(model.getContainer().getName());
		ValuesCargo tmpCargo = tmpContainer.getOrAddCargo(ValuesCargo.class);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			tmpCargo.formatMap(tm.getValues(), new StringFormat(), os);
			return new ByteArrayPayload(os.toByteArray());
		} catch (IOException ex) {
			throw new QdbException("Unable to serialize values cargo: "+ex.getMessage(), ex);
		}
	}

	private void stopCellEditing() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
	}
}
