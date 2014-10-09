/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import org.qsardb.model.Container;

public abstract class ContainerEvent <C extends Container> extends QdbEvent {
	public enum Type {
		Add, Remove, Update;
	}

	private final Type type;
	private final C container;

	public ContainerEvent(Object source, Type type, C container) {
		super(source);
		this.type = type;
		this.container = container;
	}

	public Type getType() {
		return type;
	}

	public C getContainer() {
		return container;
	}
}