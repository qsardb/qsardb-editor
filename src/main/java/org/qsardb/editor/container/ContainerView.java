/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeEditor;
import org.qsardb.editor.container.attribute.LabelAttributeEditor;
import org.qsardb.editor.container.attribute.TextAttributeEditor;
import org.qsardb.editor.container.cargo.BibTeXCargoView;
import org.qsardb.editor.container.cargo.CargoView;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.events.ImportEvent;
import org.qsardb.model.Container;

public abstract class ContainerView<C extends Container> {

	protected final ArrayList<AttributeEditor> attrEditors;
	protected final ArrayList<CargoView> cargoViews;

	protected final QdbContext qdbContext;
	private ContainerModel<C> model;

	public ContainerView(ContainerModel<C> model) {
		this.attrEditors = new ArrayList<AttributeEditor>();
		this.attrEditors.add(new TextAttributeEditor(model, Attribute.ID));
		this.attrEditors.add(new TextAttributeEditor(model, Attribute.Name));
		this.attrEditors.add(new TextAttributeEditor(model, Attribute.Description, 60));
		this.attrEditors.add(new LabelAttributeEditor(model, Attribute.Labels));

		this.cargoViews = new ArrayList<CargoView>();
		this.cargoViews.add(new BibTeXCargoView());
		this.qdbContext = model.getQdbContext();
		this.model = model;
	}

	public void setModel(ContainerModel<C> model) {
		this.model = model;
		for (AttributeEditor ae: attrEditors) {
			ae.setModel(model);
		}
		for (CargoView view: cargoViews) {
			view.setModel(model);
		}
		updateView();
	}

	public abstract void setContainer(C container);

	public C getContainer() {
		return model.getContainer();
	}

	public ContainerModel<C> getModel() {
		return model;
	}

	public JPanel buildView() {
		JPanel content = buildContent();

		JPanel view = new ManagedJPanel(qdbContext, this);
		view.setLayout(new BorderLayout());
		view.add(content, BorderLayout.CENTER);
		updateView();

		return view;
	}

	protected void updateView(ContainerEvent e) {
		if (getContainer().equals(e.getContainer())) {
			if (e.getType() == ContainerEvent.Type.Update) {
				updateView();
			}
		}
	}

	private void updateView() {
		for (AttributeEditor editor: attrEditors) {
			editor.update();
		}
		for (CargoView view: cargoViews) {
			view.updateView();
		}
	}

	@Subscribe public void handleImport(ImportEvent e) {
		updateView();
	}

	private JPanel buildContent() {
		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		GroupLayout.SequentialGroup hgroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hgroup);
		GroupLayout.ParallelGroup col0 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
		GroupLayout.ParallelGroup col1 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

		for (AttributeEditor ae: attrEditors) {
			col0.addComponent(ae.getLabel());
			col1.addComponent(ae.getEditor());
		}

		hgroup.addGroup(col0).addGroup(col1);
		GroupLayout.SequentialGroup vgroup = layout.createSequentialGroup();
		layout.setVerticalGroup(vgroup);
		for (AttributeEditor ae: attrEditors) {
			vgroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(ae.getLabel()).addComponent(ae.getEditor()));
		}

		vgroup.addGap(16);

		for (CargoView view : cargoViews) {
			view.setModel(model);
			col0.addComponent(view.getLabel());
			col1.addComponent(view.getView());
			vgroup.addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(view.getLabel()).addComponent(view.getView()));
		}

		return panel;
	}
}