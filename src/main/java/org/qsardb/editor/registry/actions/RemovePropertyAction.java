/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.PropertyEvent;
import org.qsardb.model.Model;
import org.qsardb.model.ModelRegistry;
import org.qsardb.model.Property;

public class RemovePropertyAction extends RemoveContainerAction<Property>{
	public RemovePropertyAction(QdbContext context) {
		super(context);
	}

	@Override
	protected PropertyEvent remove(ActionEvent e, Property property) {
		ModelRegistry models = property.getQdb().getModelRegistry();
		Collection<Model> dependencies = models.getByProperty(property);
		if (dependencies.isEmpty()) {
			property.getRegistry().remove(property);
			return new PropertyEvent(this, PropertyEvent.Type.Remove, property);
		}
		
		StringBuilder sb = new StringBuilder("The following models depend on this property:");
		for (Model m: dependencies) {
			sb.append('\n').append(m.getId());
		}
		Utils.showError(e, sb.toString());
		return null;
	}
}