/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.Payload;
import org.qsardb.model.QdbException;
import org.qsardb.model.StringPayload;

/**
 * Edits given cargo as a plain text.
 */
public class EditTextView extends EditCargoView {
	private final JTextArea textArea = Utils.createTextArea();

	public EditTextView(ContainerModel model, String cargoId) {
		super(model, cargoId);
		initTextArea();
	}

	String getText() {
		return textArea.getText();
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	@Override
	protected JComponent buildContentPanel() {
		return new JScrollPane(textArea);
	}

	@Override
	protected Payload createPayload() throws QdbException {
		return new StringPayload(textArea.getText());
	}

	private void initTextArea() {
		try {
			textArea.setText(model.loadCargoString(cargoId));
			textArea.setCaretPosition(0);
			textArea.setLineWrap(true);
			textArea.setTabSize(2);
		} catch (IOException ex) {
			Utils.showError("Can't load cargo: "+cargoId+"\n"+ex.getMessage());
		}
	}
}