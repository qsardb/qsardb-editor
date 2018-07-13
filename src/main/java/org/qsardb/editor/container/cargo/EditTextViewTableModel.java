/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.container.ContainerModel;
import org.qsardb.model.QdbException;

public class EditTextViewTableModel extends AbstractTableModel {
	private Map<String, String>  values;

	private static final String[] header = {"row", "id", "value"};
	private ArrayList<String> rows;

	public EditTextViewTableModel(ContainerModel model) {
		try {
			values = new LinkedHashMap(loadValuesCargo(model));
			rows = new ArrayList<>(values.keySet());
		} catch (IOException ex) {
			throw new IllegalArgumentException("Can't load values cargo: " + ex.getMessage(), ex);
		}
	}

	public Map<String, String> getValues() throws QdbException {
		LinkedHashMap<String, String> map = new LinkedHashMap<>(rows.size());
		for (int i=0; i<rows.size(); i++) {
			String key = rows.get(i);
			String value = values.getOrDefault(key, "");
			if (key.isEmpty() || value.isEmpty()) {
				throw new QdbException("Row "+(i+1)+" has a missing value");
			}
			map.put(key, value);
		}
		return map;
	}

	public void removeRows(int[] selection) {
		for (int i=selection.length-1; i>=0; i--) {
			int index = selection[i];
			String key = rows.get(index);
			rows.remove(index);
			values.remove(key);
		}
		fireTableDataChanged();
	}

	public void addRow(int selection) {
		rows.add(selection, "");
		fireTableRowsInserted(selection, selection);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex != 0;
	}

	@Override
	public String getColumnName(int column) {
		return header[column];
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return header.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String cid = rows.get(rowIndex);
		switch (columnIndex) {
			case 0:
				return String.valueOf(rowIndex + 1);
			case 1:
				return cid;
			case 2:
				return values.getOrDefault(cid, "");
			default:
				throw new IllegalArgumentException("column="+columnIndex);
		}
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		String curKey = rows.get(rowIndex);
		String newValue = (String)value;

		switch (columnIndex) {
			case 1:
				if (curKey.equals(newValue)) {
					return;
				} else if (values.containsKey(newValue)) {
					return; // XXX
				}
				values.put(newValue, values.getOrDefault(curKey, ""));
				values.remove(curKey);
				rows.set(rowIndex, newValue);
				break;
			case 2:
				values.put(curKey, newValue);
				break;
			default:
				throw new IllegalArgumentException("columnIndex="+columnIndex);
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	private Map<String, String> loadValuesCargo(ContainerModel model) throws IOException {
		if (model.getContainer().hasCargo(ValuesCargo.class)) {
			ValuesCargo vcProp = (ValuesCargo) model.getContainer().getCargo(ValuesCargo.class);
			return vcProp.loadStringMap();
		}
		return new LinkedHashMap<>();
	}
}
