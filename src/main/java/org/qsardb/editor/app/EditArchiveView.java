/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import org.qsardb.editor.events.ArchiveXmlEvent;
import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.Utils;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Archive;

class EditArchiveView {
	private final DocumentListener listener = createListener();
	private final JTextField nameField = Utils.createTextField();
	private final JTextArea descField = Utils.createTextArea();
	private final QdbContext qdbContext;

	public EditArchiveView(QdbContext context) {
		qdbContext = context;
	}

	public JComponent createView() {
		ManagedJPanel panel = new ManagedJPanel(qdbContext, this);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		JLabel header = new JLabel("QsarDB archive overview");

		JLabel nameLabel = new JLabel("Name");
		nameField.getDocument().addDocumentListener(listener);

		JLabel descLabel = new JLabel("Description");
		descField.getDocument().addDocumentListener(listener);
		JScrollPane descScroll = new JScrollPane(descField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(header, GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
					.addComponent(nameLabel)
					.addComponent(descLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(nameField)
					.addComponent(descScroll)))
		);

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(header)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(nameLabel).addComponent(nameField))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(descLabel).addComponent(descScroll)));

		updateView();
		return panel;
	}

	private DocumentListener createListener() {
		return new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateModel();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateModel();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateModel();
			}
		};
	}

	private void updateModel() {
		Archive archive = qdbContext.getQdb().getArchive();
		if (!getName().equals(archive.getName())) {
			archive.setName(getName());
		}
		if (!getDescription().equals(archive.getDescription())) {
			archive.setDescription(getDescription());
		}
		qdbContext.fire(new ArchiveXmlEvent(this));
	}

	private String getName() {
		return nameField.getText().trim();
	}

	private String getDescription() {
		return descField.getText().trim();
	}

	private void updateView() {
		nameField.getDocument().removeDocumentListener(listener);
		descField.getDocument().removeDocumentListener(listener);
		try {
			Archive archive = qdbContext.getQdb().getArchive();
			if (!getName().equals(archive.getName())) {
				nameField.setText(archive.getName());
			}
			if (!getDescription().equals(archive.getDescription())) {
				descField.setText(archive.getDescription());
			}
		} finally {
			nameField.getDocument().addDocumentListener(listener);
			descField.getDocument().addDocumentListener(listener);
		}
	}

	@Subscribe public void handle(ArchiveXmlEvent evt) {
		updateView();
	}
}