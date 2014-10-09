/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import org.qsardb.model.Model;

public class ModelEvent extends ContainerEvent<Model> {
	public ModelEvent(Object source, ModelEvent.Type type, Model model) {
		super(source, type, model);
	}
}
