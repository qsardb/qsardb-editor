/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import org.qsardb.model.Compound;

public class CompoundEvent extends ContainerEvent<Compound> {
	public CompoundEvent(Object source, CompoundEvent.Type type, Compound compound) {
		super(source, type, compound);
	}
}
