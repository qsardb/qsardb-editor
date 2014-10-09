/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import org.qsardb.editor.container.cargo.CargoInfo;
import org.qsardb.editor.container.cargo.CargoView;
import org.qsardb.model.Container;

abstract class ParameterView<C extends Container>  extends ContainerView<C> {

	public ParameterView(ContainerModel<C> model) {
		super(model);
		cargoViews.add(0, new CargoView(CargoInfo.UCUM));
		cargoViews.add(0, new CargoView(CargoInfo.Values));
	}
}
