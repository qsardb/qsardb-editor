/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import org.qsardb.model.Container;

public class MappingRule {
	private final String sourceColumnId;
	private final String sourceColumnHeading;
	private MapTo mapping = MapTo.IGNORE;
	private Container containerArgument;

	public MappingRule(String sourceColumnId, String heading) {
		this.sourceColumnId = sourceColumnId;
		this.sourceColumnHeading = heading;
	}

	public String getName() {
		return mapping.toString();
	}

	public String getSourceColumnId() {
		return sourceColumnId;
	}

	public String getSourceColumnHeading() {
		return sourceColumnHeading;
	}

	public MapTo getMapTo() {
		return mapping;
	}

	public Container getArgument() {
		return containerArgument;
	}

	public void update(MapTo newMapping, Container newArgument) {
		mapping = newMapping;
		containerArgument = newArgument;
	}

	public boolean resolveConflicts(MappingRule other) {
		if (mapping != other.mapping) {
			return false;
		} 

		if (mapping.isUniqueColumn()) {
			resetToIgnored();
			return true;
		}

		if (containerArgument != null && containerArgument.equals(other.containerArgument)) {
			resetToIgnored();
			return true;
		}
		return false;
	}

	private void resetToIgnored() {
			mapping = MapTo.IGNORE;
			containerArgument = null;
	}
}