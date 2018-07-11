/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;
import org.qsardb.model.StringPayload;

class EditValuesView extends EditCargoView {
	private JScrollPane jsp;
	private EditTextViewTableModel tm;
	private JTable table;

	public EditValuesView(ContainerModel model, String cargoId) {
		super(model, cargoId);
	}

	@Override
	protected JComponent buildContentPanel() {
		JPanel content = new JPanel(new BorderLayout());

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));

		JButton addRow = new JButton(new AbstractAction("Add row below") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.addRow(true);
				table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
			}
		});
		buttons.add(addRow);

		JButton addRowAbove = new JButton(new AbstractAction("Add row above") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.addRow(false);
				table.scrollRectToVisible(table.getCellRect(0, 0, true));
			}
		});
		buttons.add(addRowAbove);

		JButton removeRow = new JButton(new AbstractAction("Remove row") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				tm.removeRows(rows);
			}
		});
		buttons.add(removeRow);

		tm = new EditTextViewTableModel(model);
		table = new JTable(tm);

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
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		return new StringPayload(tm.getText());
	}
}
