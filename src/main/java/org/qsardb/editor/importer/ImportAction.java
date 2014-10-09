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
	private final QdbContext qdbContext;

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

		return tableFromSpreadsheet(source, 0);
	}

	private static Table tableFromSpreadsheet(File source, int page) throws Exception {
		InputStream is = new FileInputStream(source);
		try {
			String name = source.getName().toLowerCase();
			if (name.endsWith(".csv") || name.endsWith(".tsv") || name.endsWith(".txt")) {
				return new CsvWorkbook(is, CsvUtil.getFormat(source)).getWorksheet(page);
			} else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
				return new ExcelWorkbook(is).getWorksheet(page);
			} else if (name.endsWith(".ods")) {
				return new OpenDocumentWorkbook(is).getWorksheet(page);
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