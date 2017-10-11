/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.qsardb.conversion.sdfile.SDFile;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;

public class DataImportAction extends TableImportAction {
	public DataImportAction(QdbContext context) {
		super(context, "Import data");
	}

	@Override
	protected void customiseFilters(JFileChooser fc) {
		super.customiseFilters(fc);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("SD file", "sdf"));
	}

	@Override
	protected Map<String, Table> loadTables(File source) throws Exception {
		if (source.getName().toLowerCase().endsWith(".sdf")) {
			Table table = new SDFile(source);
			return Collections.singletonMap("Sheet1", table);
		}
		return super.loadTables(source);
	}
	
	@Override
	protected void performImport(Table table, Component parent) {
		ImportDataView panel = new ImportDataView(qdbContext, table);
		panel.show(parent);
	}
}