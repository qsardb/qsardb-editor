/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.events;

import java.util.EventObject;

/**
 * Base class for events that are sent through QdbContext#fire.
 */
public abstract class QdbEvent extends EventObject {
	public QdbEvent(Object source) {
		super(source);
	}
}
