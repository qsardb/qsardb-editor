/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeValue;
import org.qsardb.editor.events.PropertyEvent;
import org.qsardb.model.Property;

public class PropertyModel extends ContainerModel<Property>{
	private final Property nullProperty = new Property(null);
	private final Property property;

	public PropertyModel(QdbContext context, Property property) {
		super(context, property != null);
		this.attributes.put(Attribute.Endpoint, new EndpointAttributeValue());
		this.attributes.put(Attribute.Species, new SpeciesAttributeValue());
		this.property = property != null ? property : nullProperty;
	}

	@Override
	public Property getContainer() {
		return property;
	}

	@Override
	protected void fireEvent() {
		getQdbContext().fire(new PropertyEvent(this, PropertyEvent.Type.Update, getContainer()));
	}

	private class EndpointAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setEndpoint(value);
			fireEvent();
		}

		@Override
		public String get() {
			return  getContainer().getEndpoint();
		}
	}

	private class SpeciesAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setSpecies(value);
			fireEvent();
		}
		
		@Override
		public String get() {
			return getContainer().getSpecies();
		}
	}
}
