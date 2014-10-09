/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.attribute;

public abstract class AttributeValue <T> {

	public abstract void set(T value);

	public abstract T get();

	public boolean isValid() {
		return true;
	}

	public boolean isEditable() {
		return true;
	}
}
