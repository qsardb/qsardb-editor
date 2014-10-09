/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import org.qsardb.model.Descriptor;

public class DescriptorEvent extends ContainerEvent<Descriptor> {
	public DescriptorEvent(Object source, DescriptorEvent.Type type, Descriptor descriptor) {
		super(source, type, descriptor);
	}
}
