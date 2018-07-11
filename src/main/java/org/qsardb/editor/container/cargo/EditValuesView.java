/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;
import org.qsardb.model.StringPayload;

class EditValuesView extends EditCargoView {
	private JScrollPane jsp;
	private EditTextViewTableModel tm;

	public EditValuesView(ContainerModel model, String cargoId) {
		super(model, cargoId);
	}

	@Override
	protected JComponent buildContentPanel() {
		JPanel content = new JPanel(new BorderLayout());

		tm = new EditTextViewTableModel(model);
		jsp = tm.getScrollPane();
		jsp.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));

		JButton addRow = new JButton(new AbstractAction("Add row below") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.addRow(true);
			}
		});
		buttons.add(addRow);

		JButton addRowAbove = new JButton(new AbstractAction("Add row above") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.addRow(false);
			}
		});
		buttons.add(addRowAbove);

		JButton removeRow = new JButton(new AbstractAction("Remove row") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.removeRow();
			}
		});
		buttons.add(removeRow);

		content.add(buttons, BorderLayout.NORTH);
		content.add(jsp, BorderLayout.CENTER);
		return content;
	}

	@Override
	protected Payload createPayload() throws QdbException {
		return new StringPayload(tm.getText());
	}
}
