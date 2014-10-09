/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.qsardb.conversion.table.Cell;
import org.qsardb.conversion.table.Column;
import org.qsardb.conversion.table.Row;
import org.qsardb.conversion.table.Table;

public class ColumnPreview extends JPanel {
	private final Table table;
	private final MappingRulesView rulesView;
	private final ArrayList<String> ids = new ArrayList<String>();
	private final ArrayList<String> data = new ArrayList<String>();
	private final PreviewTableModel model = new PreviewTableModel();
	private final JTable jtable = new JTable(model);

	private String columnHeader = "Preview";

	public ColumnPreview(MappingRulesView rulesView, Table table) {
		super(new BorderLayout());
		this.rulesView = rulesView;
		this.table = table;
		buildPanel();
	}

	private void update() {
		data.clear();
		ids.clear();

		MappingRule idRule = rulesView.getModel().getIdMappingRule();
		if (idRule != null) {
			ids.addAll(readColumn(idRule.getSourceColumnId()));
		}

		MappingRule rule = rulesView.getSelectedRule();
		if (rule != null) {
			data.addAll(readColumn(rule.getSourceColumnId()));
		}
		model.fireTableStructureChanged();
	}

	private ArrayList<String> readColumn(String colId) {
		ArrayList<String> values = new ArrayList<String>();
		try {
			Column col = table.getColumn(colId);
			Iterator<Row> rows = table.rows();

			boolean haveHeaderRow = rulesView.getModel().hasHeader();
			if (haveHeaderRow && rows.hasNext()) {
				columnHeader = getNextCellValue(rows, col);
			} else {
				columnHeader = "Preview [" + colId + "]";
			}

			while (rows.hasNext()) {
				values.add(getNextCellValue(rows, col));
			}
			return values;
		} catch (Exception e) {
			return values;
		}
	}

	private String getNextCellValue(Iterator<Row> rows, Column col) {
		Row row = rows.next();
		Cell cell = row.getValues().get(col);
		return cell != null ? cell.getText() : "";
	}

	private void buildPanel() {
		JScrollPane scrollPane = new JScrollPane(jtable);
		jtable.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);
		
		rulesView.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				update();
			}
		});

		rulesView.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				update();
			}
		});
	}

	private class PreviewTableModel extends AbstractTableModel {
		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Compound ID";
			} else if (column == 1) {
				return columnHeader;
			}
			throw new IllegalArgumentException("column="+column);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return ids.isEmpty() ? "Unassigned" : ids.get(rowIndex);
			} else if (columnIndex == 1) {
				return data.get(rowIndex);
			}
			throw new IllegalArgumentException("column="+columnIndex);
		}
	}
}