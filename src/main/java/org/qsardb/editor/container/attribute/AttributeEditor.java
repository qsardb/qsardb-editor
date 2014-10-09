/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.attribute;

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.qsardb.editor.container.ContainerModel;

public abstract class AttributeEditor <V extends AttributeValue> {
	private ContainerModel containerModel;
	private final Attribute attribute;
	private final JLabel label = new JLabel();

	public AttributeEditor(ContainerModel model, Attribute attr) {
		containerModel = model;
		attribute = attr;
		label.setText(attr.toString());
	}

	public abstract JComponent getEditor();

	public JComponent getLabel() {
		return label;
	}

	public void setModel(ContainerModel model) {
		containerModel = model;
		update();
	}

	public void update() {
		getLabel().setForeground(isValid() ? Color.BLACK : Color.RED);
		updateEditor();
	}

	protected abstract void updateEditor();

	protected V value() {
		return (V) containerModel.getAttributeValue(attribute);
	}

	private boolean isValid() {
		return containerModel.isEmpty() || value().isValid();
	}

	protected boolean isEditable() {
		return !containerModel.isEmpty() && value().isEditable();
	}
}