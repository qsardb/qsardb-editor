/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.qsardb.conversion.csv.CsvUtil;
import org.qsardb.conversion.csv.CsvWorkbook;
import org.qsardb.conversion.excel.ExcelWorkbook;
import org.qsardb.conversion.opendocument.OpenDocumentWorkbook;
import org.qsardb.conversion.spreadsheet.Workbook;
import org.qsardb.conversion.spreadsheet.Worksheet;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;

public abstract class TableImportAction extends AbstractAction {
	protected final QdbContext qdbContext;

	public TableImportAction(QdbContext context, String name) {
		super(name);
		qdbContext = context;
	}

	protected abstract void performImport(Table table, Component parent);

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = Utils.getFileChooser();
		customiseFilters(fc);

		Component parent = (Component) e.getSource();
		if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			setEnabled(false);
			new ShowDialogTask(fc.getSelectedFile(), parent).execute();
		}
	}

	protected void customiseFilters(JFileChooser fc) {
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma/tab separated values", "csv", "tsv", "txt"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Excel spreadsheet", "xls", "xlsx"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("OpenDocument spreadsheet", "ods"));
	}

	protected Map<String, Table> loadTables(File source) throws Exception {
		LinkedHashMap<String, Table> result = new LinkedHashMap<String, Table>();
		InputStream is = new FileInputStream(source);
		try {
			String name = source.getName().toLowerCase();

			Workbook workbook = null;
			if (name.endsWith(".csv") || name.endsWith(".tsv") || name.endsWith(".txt")) {
				workbook = new CsvWorkbook(is, CsvUtil.getFormat(source));
			} else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
				workbook = new ExcelWorkbook(is);
			} else if (name.endsWith(".ods")) {
				workbook = new OpenDocumentWorkbook(is);
			}

			if (workbook == null) {
				throw new UnsupportedOperationException("Unsupported file type: "+source.getName());
			}
			for (int i=0; i<workbook.getWorksheetCount(); i++) {
				Worksheet sheet = workbook.getWorksheet(i);
				String sheetName = sheet.getId() != null ? sheet.getId() : "Sheet"+(i+1);
				result.put(sheetName, sheet);
			}
			return result;
		} finally {
			is.close();
		}
	}

	private class ShowDialogTask extends SwingWorker<Map<String, Table>, Void>{
		private final File file;
		private final Component parent;

		public ShowDialogTask(File inputFile, Component dialogParent) {
			file = inputFile;
			parent = dialogParent;
		}

		@Override
		protected Map<String, Table> doInBackground() throws Exception {
			return loadTables(file);
		}

		@Override
		protected void done() {
			try {
				Map<String, Table> tables = get();
				String[] keys = tables.keySet().toArray(new String[0]);
				String key = null;
				if (keys.length == 1) {
					key = keys[0];
				} else if (keys.length > 1) {
					String[] choices = tables.keySet().toArray(new String[0]);
					key = (String) JOptionPane.showInputDialog(parent, "Choose a sheet for importing", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
				}
				Table table = tables.get(key);
				if (table != null) {
					performImport(table, parent);
				}
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof UnsupportedOperationException) {
					Utils.showError(cause.getMessage());
				} else if (cause instanceof IOException) {
					Utils.showError(cause.getMessage());
				} else {
					Utils.showExceptionPanel(e.getMessage(), e);
				}
			} finally {
				setEnabled(true);
			}
		}
	}
}