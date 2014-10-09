/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.*;

public class Select<C extends Container> {

	public static Property property(QdbContext context, String idHint) {
		PropertyRegistryView view = new PropertyRegistryView(context);
		view.selectContainer(idHint);
		return new Select<Property>(view).showDialog("Select property");
	}

	public static Descriptor descriptor(QdbContext context, String idHint) {
		DescriptorRegistryView view = new DescriptorRegistryView(context);
		view.selectContainer(idHint);
		return new Select<Descriptor>(view).showDialog("Select descriptors");
	}

	public static List<Descriptor> descriptors(QdbContext context, String idHint, List<String> hideIds) {
		DescriptorRegistryView view = new DescriptorRegistryView(context);
		Select<Descriptor> select = new Select<Descriptor>(view);
		view.getModel().hideContainers(hideIds);
		view.enableMultipleSelection(true);
		view.selectContainer(idHint);
		select.showDialog("Select descriptor");
		return view.getSelectedContainers();
	}

	public static Prediction prediction(QdbContext context, String idHint) {
		ModelRegistry models = context.getQdb().getModelRegistry();
		if (models.size() == 0) {
			JOptionPane.showMessageDialog(null, "No models in the archive. At least one model is required.");
			return null;
		}

		PredictionRegistryView view = new PredictionRegistryView(context);
		view.selectContainer(idHint);
		return new Select<Prediction>(view).showDialog("Select prediction");
	}

	private final RegistryView<C> view;
	private final JDialog dialog = new JDialog();

	public Select(RegistryView<C> view) {
		this.view = view;
	}

	private C showDialog(String title) {
		dialog.setTitle(title);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		dialog.getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
		dialog.setLocationByPlatform(true);
		dialog.pack();
		dialog.setVisible(true);

		return view.getSelectedContainer();
	}

	private JPanel buildContentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(view.buildView(), BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttons, BorderLayout.SOUTH);
		buttons.add(Box.createHorizontalStrut(300));
		buttons.add(new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.clearSelection();
				dialog.dispose();
			}
		}));
		final JButton okButton = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttons.add(okButton);
		okButton.setEnabled(view.getSelectedContainer() != null);

		view.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					okButton.setEnabled(view.getSelectedContainer() != null);
				}
			}
		});

		return panel;
	}
}