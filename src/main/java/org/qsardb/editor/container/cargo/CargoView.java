/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.*;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.common.Utils;
import org.qsardb.model.FilePayload;
import org.qsardb.model.Payload;

public class CargoView {
	private View view;
	private final JLabel label = new JLabel();
	private final Action editAction = createEditTextAction();
	private final Action importAction = createImportAction();
	private final Action deleteAction = createDeleteAction();

	private final String cargoId;
	protected ContainerModel model;

	public CargoView(CargoInfo cargo) { 
		this.cargoId = cargo.getCargoId();
		label.setText(cargo.getName());
	}

	public void setModel(ContainerModel model) {
		this.model = model;
		updateView();
	}

	public JLabel getLabel() {
		return label;
	}

	public JPanel getView() {
		if (view == null) {
			initView();
		}
		return view;
	}

	private void initView() {
		view = new View();
		view.add(new JButton(editAction));
		view.add(Box.createHorizontalStrut(6));
		view.add(new JButton(importAction));
		for (Action action : getAdditionalActions()) {
			view.add(Box.createHorizontalStrut(6));
			view.add(new JButton(action));
		}
		view.add(Box.createHorizontalStrut(6));
		view.add(new JButton(deleteAction));
		updateView();
	}

	public void updateView() {
		editAction.setEnabled(isEnabled());
		editAction.putValue(Action.NAME, hasCargo() ? "Edit" : "New");
		importAction.setEnabled(isEnabled());
		for (Action action : getAdditionalActions()) {
			action.setEnabled(isEnabled());
		}
		deleteAction.setEnabled(isDeleteEnabled());
	}

	protected boolean hasCargo() {
		return model != null && model.hasCargo(cargoId);
	}

	protected boolean isEnabled() {
		return model != null && !model.isEmpty();
	}

	protected boolean isDeleteEnabled() {
		return model != null && !model.isEmpty() && hasCargo();
	}

	protected Action[] getAdditionalActions() {
		return new Action[]{};
	}

	protected Action createEditTextAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String title = String.format("Edit %s cargo", label.getText());
				if (cargoId.equals(ValuesCargo.ID)) {
					new EditValuesView(model, cargoId).showModal(title);
				} else {
					new EditTextView(model, cargoId).showModal(title);
				}
				updateView();
			}
		};
	}

	protected Action createDeleteAction() {
		return new AbstractAction("Delete") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int k = JOptionPane.showConfirmDialog(view, "Confirm delete", "Confirm delete", JOptionPane.OK_CANCEL_OPTION);
				if (k != JOptionPane.OK_OPTION) {
					return;
				}
				model.setCargoPayload(cargoId, null);
				model.getContainer().removeCargo(cargoId);
				updateView();
			}
		};
	}

	private Action createImportAction() {
		return new AbstractAction("Attach") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = Utils.getFileChooser();
				if (fc.showOpenDialog((Component)e.getSource()) == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					Payload payload = new FilePayload(f);
					model.setCargoPayload(cargoId, payload);
					updateView();
				}
			}
		};
	}

	private static class View extends JPanel {
		public View() {
			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		}

		@Override
		public int getBaseline(int width, int height) {
			return new JButton("baseline").getBaseline(width, height);
		}
	}
}
