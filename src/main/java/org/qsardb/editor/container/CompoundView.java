/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.TextAttributeEditor;
import org.qsardb.editor.container.cargo.CargoInfo;
import org.qsardb.editor.container.cargo.CargoView;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.visualizer.CompoundVisualizer;
import org.qsardb.model.Compound;

public class CompoundView extends ContainerView<Compound> {
	public JPanel graphingPane;

	public CompoundView(CompoundModel model) {
		super(model);
		attrEditors.add(new TextAttributeEditor(model, Attribute.CAS));
		attrEditors.add(new TextAttributeEditor(model, Attribute.InChi));
		cargoViews.add(0, new CargoView(CargoInfo.SMILES));
		cargoViews.add(0, new CargoView(CargoInfo.MDL_molfile));
	}

	public CompoundView(QdbContext context, String hintID) {
		this(new CompoundModel(context, new Compound(hintID)));
	}

	@Override
	public void setContainer(Compound container) {
		setModel(new CompoundModel(qdbContext, container));
		paintMolecule(container);
	}

	@Override
	protected void updateView(ContainerEvent e) {
		super.updateView(e);
		if (getContainer().equals(e.getContainer())) {
			if (e.getType() == ContainerEvent.Type.Update) {
				paintMolecule((Compound) e.getContainer());
			}
		}
	}
	
	private void paintMolecule(Compound container){
		if (panel.getComponentCount() > 1) {
			this.panel.remove(1);
		}
		CompoundVisualizer cv;
		if (container != null) {
			cv = new CompoundVisualizer(container);
			JPanel p = new JPanel(new BorderLayout());
			p.add(cv.get2Drenders());
			this.panel.add(p);
		}
		panel.updateUI();
	}

	@Subscribe public void handle(CompoundEvent e) {
		updateView(e);
	}
}
