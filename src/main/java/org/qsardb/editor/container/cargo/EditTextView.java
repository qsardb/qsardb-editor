/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;
import org.qsardb.model.StringPayload;

/**
 * Edits given cargo as a plain text.
 */
public class EditTextView extends EditCargoView {
	private JScrollPane jsp;
	EditTextViewPlainModel etvt;

	public EditTextView(ContainerModel model, String cargoId) {
		super(model, cargoId);
		initTextArea();
	}

	String getText() {
		return etvt.getText();
	}

	public void setText(String text) {
		etvt.setText(text);
	}

	@Override
	protected JComponent buildContentPanel() {
		jsp = etvt.getScrollPane();
		return jsp;
	}

	@Override
	protected Payload createPayload() throws QdbException {
		return new StringPayload(etvt.getText());
	}

	private void initTextArea() {
		etvt = new EditTextViewPlainModel(model, cargoId);
	}
}