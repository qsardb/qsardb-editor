/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;

public abstract class EditCargoView {
	private JDialog frame;

	protected final ContainerModel model;
	protected final String cargoId;

	EditCargoView(ContainerModel model, String cargoId) {
		this.model = model;
		this.cargoId = cargoId;
	}

	protected abstract JComponent buildContentPanel();
	protected abstract Payload createPayload() throws QdbException;

	public void showModal(String title) {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(buildContentPanel(), BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
		contentPane.add(buttons, BorderLayout.SOUTH);

		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		buttons.add(closeButton);
		JButton saveButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					model.setCargoPayload(cargoId, createPayload());
					frame.dispose();
				} catch (QdbException ex) {
					Utils.showError(frame, "Can't serialize editor content: "+ex.getMessage());
				}
			}
		});
		buttons.add(saveButton);

		frame = new JDialog((Window) null, title);
		frame.setModal(true);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(contentPane, BorderLayout.CENTER);
		frame.setSize(600, 400); // XXX
		frame.setVisible(true);
	}
}