/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.qsardb.conversion.csv.CsvUtil;
import org.qsardb.conversion.csv.CsvWorkbook;
import org.qsardb.conversion.excel.ExcelWorkbook;
import org.qsardb.conversion.opendocument.OpenDocumentWorkbook;
import org.qsardb.conversion.sdfile.SDFile;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;

public class ImportAction extends AbstractAction {
	protected final QdbContext qdbContext;

	public ImportAction(QdbContext context) {
		super("Import data");
		qdbContext = context;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = Utils.getFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma/tab separated values", "csv", "tsv", "txt"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Excel spreadsheet", "xls", "xlsx"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("OpenDocument spreadsheet", "ods"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("SD file", "sdf"));

		Component parent = (Component) e.getSource();
		if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			setEnabled(false);
			new ShowDialogTask(fc.getSelectedFile(), parent).execute();
		}
	}

	private static Table loadTable(File source) throws Exception {
		if (source.getName().toLowerCase().endsWith(".sdf")) {
			return new SDFile(source);
		}

		return tableFromSpreadsheet(source);
	}

	private static Table tableFromSpreadsheet(File source) throws Exception {
		InputStream is = new FileInputStream(source);
		try {
			String name = source.getName().toLowerCase();
			if (name.endsWith(".csv") || name.endsWith(".tsv") || name.endsWith(".txt")) {
				int sheetCount = new CsvWorkbook(is).getWorksheetCount();
				is = new FileInputStream(source);
				if(sheetCount==1){return new CsvWorkbook(is, CsvUtil.getFormat(source)).getWorksheet(0);}
				Object[] choices = new Object[sheetCount];
				for (int i = 0; i < sheetCount; i++) {
					choices[i] = i + 1;
				}
				int answer = (Integer) JOptionPane.showInputDialog(null, "Choose sheet", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
				is = new FileInputStream(source);
				return new CsvWorkbook(is, CsvUtil.getFormat(source)).getWorksheet(answer-1);
			} else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
				int sheetCount = new ExcelWorkbook(is).getWorksheetCount();
				is = new FileInputStream(source);
				if(sheetCount==1){return new ExcelWorkbook(is).getWorksheet(0);}
				Object[] choices = new Object[sheetCount];
				for (int i = 0; i < sheetCount; i++) {
					choices[i] = i + 1;
				}
				int answer = (Integer) JOptionPane.showInputDialog(null, "Choose sheet", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
				is = new FileInputStream(source);
				return new ExcelWorkbook(is).getWorksheet(answer-1);
			} else if (name.endsWith(".ods")) {
				int sheetCount = new OpenDocumentWorkbook(is).getWorksheetCount();
				is = new FileInputStream(source);
				if(sheetCount==1){return new OpenDocumentWorkbook(is).getWorksheet(0);}
				Object[] choices = new Object[sheetCount];
				for (int i = 0; i < sheetCount; i++) {
					choices[i] = i + 1;
				}
				int answer = (Integer) JOptionPane.showInputDialog(null, "Choose sheet", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
				is = new FileInputStream(source);
				return new OpenDocumentWorkbook(is).getWorksheet(answer-1);
			}
		} finally {
			is.close();
		}

		throw new UnsupportedOperationException("Unsupported file type: "+source.getName());
	}

	private class ShowDialogTask extends SwingWorker<Table, Void>{
		private final File file;
		private final Component parent;

		public ShowDialogTask(File inputFile, Component dialogParent) {
			file = inputFile;
			parent = dialogParent;
		}

		@Override
		protected Table doInBackground() throws Exception {
			return loadTable(file);
		}

		@Override
		protected void done() {
			try {
				Table table = get();
				ImportDataView panel = new ImportDataView(qdbContext, table);
				panel.show(parent);
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof UnsupportedOperationException) {
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