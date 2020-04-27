/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.importer;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import org.qsardb.conversion.sdfile.SDFile;
import org.qsardb.conversion.table.Column;
import org.qsardb.conversion.table.Row;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Container;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Prediction;
import org.qsardb.model.Property;

public class MappingRulesModel extends AbstractTableModel {

	private ArrayList<MappingRule> mappings = new ArrayList<MappingRule>();
	private final boolean hasHeader;

	public MappingRulesModel(QdbContext context, Table dataTable) {
		this.hasHeader = !(dataTable instanceof SDFile);

		try {
			Row firstRow = dataTable.rows().next();
			Iterator<Column> columns = dataTable.columns();
			while (columns.hasNext()) {
				Column col = columns.next();
				String colHeading;
				if (dataTable instanceof SDFile) {
					colHeading = col.getId();
				} else {
					String val = firstRow.getValues().get(col).getText();
					colHeading = (val != null) ? val : "Column "+col.getId();
				}
				mappings.add(new MappingRule(col.getId(), colHeading));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		mapAttribute("id", MapTo.COMPOUND_ID);
		mapAttribute("name", MapTo.COMPOUND_NAME);
		mapAttribute("cas", MapTo.CAS_NUMBER);
		mapAttribute("inchi", MapTo.INCHI);
		mapAttribute("smiles", MapTo.SMILES);
		mapAttribute("molfile", MapTo.MOLFILE);
		
		for (Descriptor dscr : context.getQdb().getDescriptorRegistry()) {
			mapByContainer(dscr);
		}
		for (Property prop : context.getQdb().getPropertyRegistry()) {
			mapByContainer(prop);
		}
		for (Prediction pred : context.getQdb().getPredictionRegistry()) {
			mapByContainer(pred);
		}
	}

	private void mapAttribute(String name, MapTo target) {
		for (int i=0; i<mappings.size(); i++) {
			String colHeading = mappings.get(i).getSourceColumnHeading();
			if (name.equalsIgnoreCase(colHeading)) {
				updateMappingRule(i, target, null);
				return;
			}
		}
	}

	public boolean hasHeader() {
		return hasHeader;
	}

	public MappingRule getMappingRule(int rowIndex) {
		return mappings.get(rowIndex);
	}

	public MappingRule getIdMappingRule() {
		for (MappingRule mr: mappings) {
			if (mr.getMapTo() == MapTo.COMPOUND_ID) {
				return mr;
			}
		}
		return null;
	}

	public void updateMappingRule(int rowIndex, MapTo mapTo, Container arg) {
		MappingRule rule = mappings.get(rowIndex);
		rule.update(mapTo, arg);

		// resolve possible mapping conflicts with other columns
		for (int i=0; i<mappings.size(); i++) {
			if (i == rowIndex) {
				continue;
			}
			if (mappings.get(i).resolveConflicts(rule)) {
				fireTableCellUpdated(i, 1);
			}
		}

		mappings.set(rowIndex, rule);
		fireTableCellUpdated(rowIndex, 1);
	}

	public void mapByContainer(Container c) {
		if (c == null) return;

		for (int i=0; i<mappings.size(); i++) {
			MappingRule m = mappings.get(i);
			boolean headingMatches = c.getId().equals(m.getSourceColumnHeading());
			if (headingMatches && m.getMapTo() == MapTo.IGNORE) {
				if (c instanceof Property) {
					updateMappingRule(i, MapTo.PROPERTY_VALUES, c);
				} else if (c instanceof Descriptor) {
					updateMappingRule(i, MapTo.DESCRIPTOR_VALUES, c);
				} else if (c instanceof Prediction) {
					updateMappingRule(i, MapTo.PREDICTION_VALUES, c);
				}
				return;
			}
		}
	}

	public void disableByContainer(Container c) {
		for (int i=0; i<mappings.size(); i++) {
			MappingRule m = mappings.get(i);
			if (c.equals(m.getArgument())) {
				updateMappingRule(i, MapTo.IGNORE, null);
				break;
			}
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return "Source";
			case 1:
				return "Mapped to";
			case 2:
				return "Setup";
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public int getRowCount() {
		return mappings.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return mappings.get(rowIndex).getSourceColumnHeading();
		}
		if (columnIndex == 1) {
			MappingRule v = mappings.get(rowIndex);
			StringBuilder sb = new StringBuilder(v.getName());
			if (v.getArgument() != null) {
				sb.append(" [").append(v.getArgument().getId()).append("]");
			}
			return sb.toString();
		}
		if (columnIndex == 2) {
			return "Edit";
		}
		throw new IllegalArgumentException("Column index: " + columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
	}
}
