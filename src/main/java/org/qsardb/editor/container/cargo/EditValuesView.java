/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;
import org.qsardb.model.StringPayload;

class EditValuesView extends EditCargoView {
	private JScrollPane jsp;
	private EditTextViewTableModel tm;
	private Action removeRow;
	private Action addRow;
	private Action addRowAbove;

	public EditValuesView(ContainerModel model, String cargoId) {
		super(model, cargoId);
	}

	@Override
	public void showModal(String title) {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(buildContentPanel(), BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(BoxLayout.LINE_AXIS, 8, 6));
		JButton addRowButton = new JButton(addRow);
		JButton addRowAboveButton = new JButton(addRowAbove);
		JButton deleteRowButton = new JButton(removeRow);
		buttons.add(addRowButton, Box.LEFT_ALIGNMENT);
		buttons.add(deleteRowButton, Box.LEFT_ALIGNMENT);
		buttons.add(Box.createHorizontalGlue());

		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		buttons.add(closeButton, Box.RIGHT_ALIGNMENT);
		JButton saveButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					model.setCargoPayload(cargoId, createPayload());
					frame.dispose();
				} catch (QdbException ex) {
					Utils.showError(frame, "Can't serialize editor content: " + ex.getMessage());
				}
			}
		});
		buttons.add(saveButton, Box.RIGHT_ALIGNMENT);

		Box bv = Box.createHorizontalBox();
		bv.add(addRowButton);
		bv.add(addRowAboveButton);
		bv.add(deleteRowButton);
		bv.add(Box.createHorizontalGlue());
		bv.add(closeButton);
		bv.add(saveButton);
		bv.setBorder(new EmptyBorder(6, 8, 6, 8));

		contentPane.add(bv, BorderLayout.SOUTH);
		frame = new JDialog((Window) null, title);
		frame.setModal(true);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(contentPane, BorderLayout.CENTER);
		frame.setSize(600, 400);
		Utils.configureWindowIcon(frame);
		frame.setVisible(true);
	}

	@Override
	protected JComponent buildContentPanel() {
		tm = new EditTextViewTableModel(model);
		jsp = tm.getScrollPane();
		removeRow = new AbstractAction("Remove row") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.removeRow();
			}
		};
		addRow = new AbstractAction("Add row below") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.addRow(true);
			}
		};
		addRowAbove = new AbstractAction("Add row above") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tm.addRow(false);
			}
		};
		return jsp;
	}

	@Override
	protected Payload createPayload() throws QdbException {
		return new StringPayload(tm.getText());
	}
}
