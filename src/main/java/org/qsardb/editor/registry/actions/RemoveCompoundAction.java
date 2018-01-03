/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qsardb.cargo.map.MapCargo;
import org.qsardb.cargo.map.ReferencesCargo;
import org.qsardb.cargo.map.StringFormat;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.model.ByteArrayPayload;
import org.qsardb.model.Compound;
import org.qsardb.model.Parameter;
import org.qsardb.model.ParameterRegistry;
import org.qsardb.model.Qdb;

public class RemoveCompoundAction extends RemoveContainerAction<Compound>{
	public RemoveCompoundAction(QdbContext context) {
		super(context);
	}

	@Override
	protected CompoundEvent remove(ActionEvent e, Compound compound) {
		try {
			Qdb qdb = compound.getQdb();
			removeCompoundData(qdb.getPropertyRegistry(), compound);
			removeCompoundData(qdb.getDescriptorRegistry(), compound);
			removeCompoundData(qdb.getPredictionRegistry(), compound);
			compound.getRegistry().remove(compound);
			return new CompoundEvent(this, CompoundEvent.Type.Remove, compound);
		} catch (IOException ex) {
			Utils.showExceptionPanel("I/O error", ex);
		}
		return null;
	}

	private void removeCompoundData(ParameterRegistry<?,?> registry, Compound compound) throws IOException {
		for (Parameter<?,?> parameter: registry) {
			removeCompoundData(parameter, compound, ValuesCargo.class);
			removeCompoundData(parameter, compound, ReferencesCargo.class);
		}
	}

	private void removeCompoundData(Parameter<?,?> parameter, Compound compound, Class<? extends MapCargo> clazz) throws IOException {

		if (!parameter.hasCargo(clazz)) {
			return;
		}

		MapCargo values = parameter.getCargo(clazz);
		Map<String, String> map = new LinkedHashMap<>(values.loadStringMap());
		
		if (map.remove(compound.getId()) != null) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			values.formatMap(map, new StringFormat(), os);
			values.setPayload(new ByteArrayPayload(os.toByteArray()));
		}
	}
}