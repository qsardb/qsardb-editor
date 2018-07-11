/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ContainerModel;

public class EditTextViewTableModel extends AbstractTableModel {
	private JTable table;
	private JScrollPane jsp;
	private ContainerModel model;
	private Map values;
	private final static int numOfColumns = 3;

	public EditTextViewTableModel(ContainerModel model) {
		values = new LinkedHashMap();
		initialize(model);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex != 0) {
			return true;
		}
		return super.isCellEditable(rowIndex, columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "row";
		} else if (column == 1) {
			return "id";
		} else {
			return "value";
		}
	}

	public int getRowHeight() {
		return table.getRowHeight();
	}

	public String getText() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		String valuesString = "id\t\n";
		if (values.containsKey("")) {
			values.remove("");
		}

		Iterator iterator = values.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = (String) values.get(key);

			valuesString += key + "\t" + value + "\n";
		}
		return valuesString;

	}

	public void setText(String text) {
		loadData();
	}

	private void loadData() {
		if (model.getContainer().hasCargo(ValuesCargo.class)) {
			ValuesCargo vcProp = (ValuesCargo) model.getContainer().getCargo(ValuesCargo.class);
			values = new LinkedHashMap();
			try {
				values = new LinkedHashMap(vcProp.loadStringMap());
			} catch (IOException ex) {
				Utils.showError("Can't load values cargo" + "\n" + ex.getMessage());
			}
		}
	}

	public void removeRow() {
		int[] n = table.getSelectedRows();
		Map BufferedValues = new LinkedHashMap(values);
		for (int i : n) {
			Object o = getValueAt(i, 1);
			BufferedValues.remove(o);
		}
		values = BufferedValues;
		fireTableDataChanged();
	}

	public void addRow(boolean above) {
		if (above) {
			if (values.containsKey("")) {
				values.remove("");
			}
			values.put("", null);
			fireTableDataChanged();
		} else {
			Map bufferedValues = new LinkedHashMap();
			bufferedValues.put("", null);
			bufferedValues.putAll(values);
			values = bufferedValues;
			fireTableDataChanged();
		}

		if (above) {
			table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
		} else {
			table.scrollRectToVisible(table.getCellRect(0, 0, true));
		}
	}

	private void initialize(ContainerModel model) {
		this.model = model;
		try {
			setText(model.loadCargoString("values"));
		} catch (IOException ex) {
			Utils.showError("Can't load values cargo" + "\n" + ex.getMessage());
		}
		table = new JTable(this);

		DefaultCellEditor singleclick = new DefaultCellEditor(new JTextField());
		singleclick.setClickCountToStart(1);
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.setDefaultEditor(table.getColumnClass(i), singleclick);
		}

		this.jsp = new JScrollPane(table);
	}

	public JScrollPane getScrollPane() {
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		return jsp;
	}

	@Override
	public int getRowCount() {
		return values.size();
	}

	@Override
	public int getColumnCount() {
		return numOfColumns;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object o = values.keySet().toArray()[rowIndex];
		if (columnIndex == 0) {
			return rowIndex + 1;
		} else if (columnIndex == 1) {
			return o;
		} else {
			return values.get(o);
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
		if (columnIndex == 2) {
			values.put(getValueAt(rowIndex, 1), aValue);
		} else if (columnIndex == 1) {
			Object o = getValueAt(rowIndex, 2);
			Map bufferedValues = new LinkedHashMap();
			int i = 0;
			for (Object k : values.keySet()) {
				if (i == rowIndex) {
					bufferedValues.put(aValue, getValueAt(rowIndex, 2));
					i++;
					continue;
				}
				if (k.equals(aValue) || k.equals(getValueAt(rowIndex, columnIndex))) {
					i++;
					continue;
				}
				bufferedValues.put(k, values.get(k));
				i++;
			}
			values = bufferedValues;
		}
		fireTableDataChanged();
	}
}
