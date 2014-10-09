/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import org.qsardb.cargo.bibtex.BibTeXCargo;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.editor.common.Utils;
import org.qsardb.resolution.doi.DOIResolver;

public class ResolveDoiDialog {
	private JDialog frame;
	private final JTextField doiField = Utils.createTextField();
	private final ContainerModel model;
	private ResolveTask resolveTask;

	public ResolveDoiDialog(ContainerModel model) {
		this.model = model;
	}

	public void showModal() {
		frame = new JDialog((Window)null, "Resolve DOI");
		frame.setModal(true);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(buildView(), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel buildView() {
		JPanel panel = new JPanel(new BorderLayout(2, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		panel.add(new JLabel("Enter DOI code:"), BorderLayout.WEST);
		doiField.setColumns(30);
		panel.add(doiField, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttons, BorderLayout.SOUTH);

		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				if (resolveTask != null) {
					resolveTask.cancel(true);
				}
			}
		});
		buttons.add(closeButton);

		JButton resolveButton = new JButton(new AbstractAction("Resolve") {
			@Override
			public void actionPerformed(ActionEvent e) {
				doiField.setEnabled(false);
				setEnabled(false);
				resolveTask = new ResolveTask();
				resolveTask.execute();
			}
		});
		buttons.add(resolveButton);

		return panel;
	}

	private class ResolveTask extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			String doi = doiField.getText().trim();
			return DOIResolver.asBibTeX(doi);
		}

		@Override
		protected void done() {
			frame.dispose();

			EditTextView editor = new EditTextView(model, BibTeXCargo.ID);
			String oldBibTeX = editor.getText();
			String newline = oldBibTeX.trim().isEmpty() ? "" : "\n\n";
			try {
				editor.setText(oldBibTeX + newline + get());
				editor.showModal("Edit BibTeX");
			} catch (ExecutionException ex) {
				Utils.showError("Can't resolve DOI: "+ doiField.getText());
			} catch (Exception ex) {
				// Ignore
			}
		}
	}
}
