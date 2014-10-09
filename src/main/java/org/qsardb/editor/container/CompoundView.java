/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.TextAttributeEditor;
import org.qsardb.editor.container.cargo.CargoInfo;
import org.qsardb.editor.container.cargo.CargoView;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.model.Compound;

public class CompoundView extends ContainerView<Compound> {

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
	}

	@Subscribe public void handle(CompoundEvent e) {
		updateView(e);
	}
}
