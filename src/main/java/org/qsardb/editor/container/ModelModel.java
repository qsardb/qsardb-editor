/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeValue;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.model.Model;
import org.qsardb.model.Property;

public class ModelModel extends ContainerModel<Model> {
	private final Model nullModel = new Model(null, null);
	private final Model model;

	public ModelModel(QdbContext context, Model model) {
		super(context, model != null);
		this.attributes.put(Attribute.PropertyId, new PropertyIdAttributeValue());
		this.model = model != null ? model : nullModel;
	}

	@Override
	public Model getContainer() {
		return model;
	}

	@Override
	protected void fireEvent() {
		getQdbContext().fire(new ModelEvent(this, ModelEvent.Type.Update, getContainer()));
	}

	public String getPropertyId() {
		return  getContainer().getProperty().getId();
	}

	private class PropertyIdAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			Property property = getQdbContext().getQdb().getProperty(value);
			if (property == null) {
				throw new IllegalStateException("Property is null");
			}
			getContainer().setProperty(property);
			fireEvent();
		}

		@Override
		public String get() {
			Property property = getContainer().getProperty();
			return property != null ? property.getId() : null;
		}

		@Override
		public boolean isValid() {
			return get() != null;
		}
	}
}
