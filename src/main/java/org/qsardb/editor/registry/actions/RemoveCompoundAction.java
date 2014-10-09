/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.model.*;

public class RemoveCompoundAction extends RemoveContainerAction<Compound>{
	public RemoveCompoundAction(QdbContext context) {
		super(context);
	}

	@Override
	protected CompoundEvent remove(ActionEvent e, Compound compound) {
		try {
			Qdb qdb = compound.getQdb();
			removeValues(qdb.getPropertyRegistry(), compound);
			removeValues(qdb.getDescriptorRegistry(), compound);
			removeValues(qdb.getPredictionRegistry(), compound);
			compound.getRegistry().remove(compound);
			return new CompoundEvent(this, CompoundEvent.Type.Remove, compound);
		} catch (IOException ex) {
			Utils.showExceptionPanel("I/O error", ex);
		}
		return null;
	}

	private void removeValues(ParameterRegistry registry, Compound compound) throws IOException {
		for (Object o: registry) {
			ValuesCargo values = getValuesCargo((Container)o);
			Map<String, String> map = new LinkedHashMap<String, String>(values.loadStringMap());
			map.remove(compound.getId());
			values.storeStringMap(map);
		}
	}

	private ValuesCargo getValuesCargo(Container container) {
		if (container.hasCargo(ValuesCargo.class)) {
			if (container instanceof Property) {
				return ((Property)container).getCargo(ValuesCargo.class);
			} else if (container instanceof Descriptor) {
				return ((Descriptor)container).getCargo(ValuesCargo.class);
			} else if (container instanceof Prediction) {
				return ((Prediction)container).getCargo(ValuesCargo.class);
			}
			throw new IllegalArgumentException();
		}
		return null;
	}
}