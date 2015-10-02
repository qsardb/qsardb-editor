/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.model.*;

public class Make<C extends Container> {

	public static Compound compound(QdbContext context, String hintID) {
		CompoundView view = new CompoundView(new QdbContext(context), hintID);
		return new Make<Compound>(view).showDialog("compound");
	}

	public static Property property(QdbContext context, String hintID) {
		PropertyView view = new PropertyView(new QdbContext(context), hintID);
		return new Make<Property>(view).showDialog("property");
	}

	public static Descriptor descriptor(QdbContext context, String hintID) {
		DescriptorView view = new DescriptorView(new QdbContext(context), hintID);
		return new Make<Descriptor>(view).showDialog("descriptor");
	}

	public static Model model(QdbContext context, String hintID) {
		ModelView view = new ModelView(new QdbContext(context), hintID);
		return new Make<Model>(view).showDialog("model");
	}

	public static Prediction prediction(QdbContext context, String hintID) {
		PredictionView view = new PredictionView(new QdbContext(context), hintID);
		return new Make<Prediction>(view).showDialog("prediction");
	}

	private final JDialog dialog = new JDialog();
	private final JButton cancelButton = new JButton("Cancel");
	private final JButton okButton = new JButton("OK");
	private final ContainerView<C> view;
	private boolean okPressed = false;

	public Make(ContainerView view) {
		this.view = view;
	}

	public C showDialog(String title) {
		dialog.setContentPane(buildContentPane(view));
		dialog.setTitle("Create a new "+title);
		Utils.configureWindowIcon(dialog);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setLocationByPlatform(true);
		dialog.pack();
		dialog.setVisible(true);

		return okPressed ? view.getContainer() : null;
	}
	public JDialog getDialog() {
		return dialog;
	}
	protected JPanel buildContentPane(final ContainerView<C> view) {
		ManagedJPanel contentPane = new ManagedJPanel(view.qdbContext, this);
		contentPane.setLayout(new BorderLayout());
		contentPane.add(view.buildView(), BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttons, BorderLayout.SOUTH);
		buttons.add(Box.createHorizontalStrut(300));
		buttons.add(cancelButton);
		buttons.add(okButton);

		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = "OK".equals(e.getActionCommand());
				dialog.dispose();
			}
		};
		cancelButton.addActionListener(buttonListener);
		okButton.addActionListener(buttonListener);

		okButton.setEnabled(view.getModel().isValid());
		return contentPane;
	}

	@Subscribe public void handle(ContainerEvent e) {
		okButton.setEnabled(view.getModel().isValid());
	}
}