/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.*;
import org.qsardb.model.*;

public class Edit <C extends Container> {

	private final RegistryView registryView;
	private final ContainerView containerView;

	private Edit(RegistryView registryView, ContainerView containerView) {
		this.registryView = registryView;
		this.containerView = containerView;
	}

	public static Edit compounds(QdbContext context) {
		CompoundModel model = new CompoundModel(context, null);
		return new Edit(new CompoundRegistryView(context), new CompoundView(model));
	}

	public static Edit properties(QdbContext context) {
		PropertyModel model = new PropertyModel(context, null);
		return new Edit(new PropertyRegistryView(context), new PropertyView(model));
	}

	public static Edit descriptors(QdbContext context) {
		DescriptorModel model = new DescriptorModel(context, null);
		return new Edit(new DescriptorRegistryView(context), new DescriptorView(model));
	}

	public static Edit models(QdbContext context) {
		ModelModel model = new ModelModel(context, null);
		return new Edit(new ModelRegistryView(context), new ModelView(model));
	}

	public static Edit predictions(QdbContext context) {
		PredictionModel model = new PredictionModel(context, null);
		return new Edit(new PredictionRegistryView(context), new PredictionView(model));
	}

	public JComponent createView() {
		JPanel panel = new JPanel(new BorderLayout());
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		panel.add(splitter, BorderLayout.CENTER);
		splitter.setDividerSize(6);
		splitter.setDividerLocation(400);

		splitter.setLeftComponent(registryView.buildView());

		splitter.setRightComponent(containerView.buildView());

		Dimension minWidth = new Dimension(0, 0);
		splitter.getLeftComponent().setMinimumSize(minWidth);
		splitter.getRightComponent().setMinimumSize(minWidth);

		registryView.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Container c = registryView.getSelectedContainer();
					containerView.setContainer(c);
				}
			}
		});

		return panel;
	}
}