/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.attribute;

public enum Attribute {
	ID("Id *"), Name("Name *"), Description, Labels, Cargos,
	InChi, CAS,
	Application,
	Endpoint, Species,
	PropertyId("Property"),
	ModelId("Model"), PredictionType("Type")
	;
	
	private final String attrName;

	private Attribute() {
		attrName = name();
	}

	private Attribute(String name) {
		attrName = name;
	}

	@Override
	public String toString() {
		return attrName;
	}
}
