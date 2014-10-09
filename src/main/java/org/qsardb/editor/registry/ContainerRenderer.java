/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.qsardb.model.Container;

class ContainerRenderer extends JLabel implements ListCellRenderer<Container> {

	public ContainerRenderer() {
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Container> list, Container value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value.getName() != null) {
			setText(value.getId() + ": " + value.getName());
		} else {
			setText(value.getId());
		}
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		return this;
	}
}
