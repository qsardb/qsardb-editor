/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import org.qsardb.model.Property;

public class PropertyEvent extends ContainerEvent<Property> {
	public PropertyEvent(Object source, PropertyEvent.Type type, Property property) {
		super(source, type, property);
	}
}
