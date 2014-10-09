/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.validator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import org.qsardb.editor.common.QdbContext;

public class ValidateArchiveView {
	private final ValidateArchiveModel model;

	public ValidateArchiveView(QdbContext context) {
		model = new ValidateArchiveModel(context);
	}

	public JComponent createView() {
		JPanel view = new JPanel(new BorderLayout());
		view.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
		view.add(controls, BorderLayout.NORTH);

		controls.add(new JLabel("QsarDB validation level:"));
		ButtonGroup validationLevels = new ButtonGroup();
		for (ValidationLevel level: ValidationLevel.values()) {
			ValidationAction action = new ValidationAction(level);
			JRadioButton levelButton = new JRadioButton(action);
			validationLevels.add(levelButton);
			controls.add(levelButton);
		}

		JTable table = new JTable(model);
		view.add(new JScrollPane(table), BorderLayout.CENTER);

		// row height is determined by error/warning icon size
		table.setRowHeight(24);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setMaxWidth(28);
		columnModel.getColumn(0).setMinWidth(28);
		columnModel.getColumn(1).setWidth(140);
		columnModel.getColumn(2).setPreferredWidth(600);

		return view;
	}

	private class ValidationAction extends  AbstractAction {
		public ValidationAction(ValidationLevel level) {
			super(level.name().toLowerCase());
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String level = (String) getValue(NAME);
			model.validate(level.toUpperCase());
		}
	}
}