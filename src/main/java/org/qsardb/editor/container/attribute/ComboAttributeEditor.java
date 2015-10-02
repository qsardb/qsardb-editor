/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.qsardb.editor.container.ContainerModel;

public abstract class ComboAttributeEditor extends AttributeEditor<AttributeValue<String>> {
	private final JComboBox<String> comboBox = new JComboBox<String>();
	private final ActionListener comboListener = createComboboxListener();

	public ComboAttributeEditor(ContainerModel model, Attribute attr) {
		super(model, attr);
		this.comboBox.setPrototypeDisplayValue(String.format("[%040d]", 0));
	}

	protected abstract List<String> getChoices();

	@Override
	public JComponent getEditor() {
		return comboBox;
	}

	public String getValue() {
		int row = comboBox.getSelectedIndex();
		if (row == -1) {
			return null;
		}
		return comboBox.getItemAt(row);
	}

	@Override
	public void updateEditor() {
		comboBox.removeActionListener(comboListener);
		try {
			comboBox.setEnabled(isEditable());
			comboBox.removeAllItems();
			if (isEditable()) {
				for (String item: getChoices()) {
					comboBox.addItem(item);
				}
				comboBox.setSelectedIndex(-1);
				comboBox.setSelectedItem(value().get());
			}
		} finally {
			comboBox.addActionListener(comboListener);
		}
	}

	protected void updateModel() {
		String fromView = getValue();
		if (!fromView.equals(value().get())) {
			value().set(fromView);
		}
	}

	private ActionListener createComboboxListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateModel();
			}
		};
	}
}