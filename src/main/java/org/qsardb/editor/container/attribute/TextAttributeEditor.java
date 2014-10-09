/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.attribute;

import java.awt.Dimension;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ContainerModel;

public class TextAttributeEditor extends AttributeEditor<AttributeValue> {
	private final JTextComponent text;
	private final DocumentListener textListener = new TextListener();

	public TextAttributeEditor(ContainerModel model, Attribute attr) {
		super(model, attr);
		text = Utils.createTextField();
	}

	public TextAttributeEditor(ContainerModel model, Attribute attr, int height) {
		super(model, attr);
		text = Utils.createTextArea();
		text.setMinimumSize(new Dimension(text.getMinimumSize().width, Math.min(32, height)));
		text.setMaximumSize(new Dimension(text.getMaximumSize().width, height));
	}

	@Override
	public JTextComponent getEditor() {
		return text;
	}

	@Override
	protected void updateEditor() {
		text.getDocument().removeDocumentListener(textListener);
		try {
			text.setEditable(isEditable());

			Object fromModel = value().get();
			Object fromEditor = getEditorValue();
			if (fromModel == null || !fromModel.equals(fromEditor)) {
				setEditorValue(fromModel);
			}
		} finally {
			text.getDocument().addDocumentListener(textListener);
		}
	}

	private void updateModelAttribute() {
		Object fromEditor = getEditorValue();
		if (!fromEditor.equals(value().get())) {
			value().set(fromEditor);
		}
	}

	protected Object getEditorValue() {
		return text.getText().trim();
	}

	protected void setEditorValue(Object value) {
		text.setText((String) value);
	}

	private class TextListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateModelAttribute();
		}
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateModelAttribute();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			updateModelAttribute();
		}
	}
}