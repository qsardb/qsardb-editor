/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry.actions;

import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.ContainerView;
import org.qsardb.editor.container.DescriptorView;
import org.qsardb.editor.container.Make;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.editor.importer.DescriptorImportAction;
import org.qsardb.model.Descriptor;

public class AddDescriptorAction extends AddContainerAction<Descriptor> {

	public AddDescriptorAction(QdbContext context, String name) {
		super(context, name, context.getQdb().getDescriptorRegistry());
	}

	@Override
	protected void makeContainer(String idHint) {
		DescriptorView view = new DescriptorView(new QdbContext(qdbContext), idHint);
		Make make = new Make<Descriptor>(view) {
			@Override
			protected JPanel buildContentPane(ContainerView<Descriptor> view) {
				JPanel p = super.buildContentPane(view);
				JButton descIButton = new JButton(new DescriptorImportAction(qdbContext) {
					@Override
					protected void performImport(Table table, Component parent) {
						super.performImport(table, parent);
						getDialog().dispose();
					}
				});
				descIButton.setToolTipText("Spreadsheet must contain the following columns (with header): DescriptorID, Name, Application");
				((JPanel) p.getComponent(1)).add(descIButton, FlowLayout.LEFT);

				return p;
			}
		};
		container = (Descriptor) make.showDialog("descriptor");
		event = new DescriptorEvent(this, DescriptorEvent.Type.Add, container);
	}
	
}
