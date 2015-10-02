/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.qsardb.conversion.csv.CsvUtil;
import org.qsardb.conversion.csv.CsvWorkbook;
import org.qsardb.conversion.excel.ExcelWorkbook;
import org.qsardb.conversion.opendocument.OpenDocumentWorkbook;
import org.qsardb.conversion.table.Row;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.editor.registry.DescriptorRegistryView;
import org.qsardb.model.Descriptor;

public class DescriptorImportAction extends ImportAction {
	private boolean openedFile = false;

	protected boolean isFileopened() {
		return openedFile;
	}

	public DescriptorImportAction(QdbContext context) {
		super(context);
		openedFile = false;
		super.putValue(Action.NAME, "Import descriptors");
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
			InputStream is = null;
			try {
				Table t = null;
				is = new FileInputStream(fc.getSelectedFile());
				try {
					String name = fc.getSelectedFile().getName().toLowerCase();
					if (name.endsWith(".csv") || name.endsWith(".tsv") || name.endsWith(".txt")) {
						int sheetCount = new CsvWorkbook(is).getWorksheetCount();
						is = new FileInputStream(fc.getSelectedFile());
						if (sheetCount == 1) {
							t = new CsvWorkbook(is, CsvUtil.getFormat(fc.getSelectedFile())).getWorksheet(0);
						} else {
							Object[] choices = new Object[sheetCount];
							for (int i = 0; i < sheetCount; i++) {
								//choices[i] = i + 1;
								choices[i] = new CsvWorkbook(is, CsvUtil.getFormat(fc.getSelectedFile())).getWorksheet(i).getId();
							}
							int answer = (Integer) JOptionPane.showInputDialog(null, "Choose sheet", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
							is = new FileInputStream(fc.getSelectedFile());
							t = new CsvWorkbook(is, CsvUtil.getFormat(fc.getSelectedFile())).getWorksheet(answer - 1);
						}
					} else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
						int sheetCount = new ExcelWorkbook(is).getWorksheetCount();
						is = new FileInputStream(fc.getSelectedFile());
						if (sheetCount == 1) {
							t = new ExcelWorkbook(is).getWorksheet(0);
						} else {
							Object[] choices = new Object[sheetCount];
							for (int i = 0; i < sheetCount; i++) {
								choices[i] = new CsvWorkbook(is, CsvUtil.getFormat(fc.getSelectedFile())).getWorksheet(i).getId();
								//choices[i] = i + 1;
							}
							int answer = (Integer) JOptionPane.showInputDialog(null, "Choose sheet", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
							is = new FileInputStream(fc.getSelectedFile());
							t = new ExcelWorkbook(is).getWorksheet(answer - 1);
						}
					} else if (name.endsWith(".ods")) {
						int sheetCount = new OpenDocumentWorkbook(is).getWorksheetCount();
						is = new FileInputStream(fc.getSelectedFile());
						if (sheetCount == 1) {
							t = new OpenDocumentWorkbook(is).getWorksheet(0);
						} else {
							Object[] choices = new Object[sheetCount];
							for (int i = 0; i < sheetCount; i++) {
								choices[i] = i + 1;
							}
							int answer = (Integer) JOptionPane.showInputDialog(null, "Choose sheet", "Sheet selection", JOptionPane.PLAIN_MESSAGE, null, choices, 0);
							is = new FileInputStream(fc.getSelectedFile());
							t = new OpenDocumentWorkbook(is).getWorksheet(answer - 1);
						}
					}
				} catch (IOException ex) {
					Logger.getLogger(DescriptorRegistryView.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvalidFormatException ex) {
					Logger.getLogger(DescriptorRegistryView.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					try {
						is.close();
					} catch (IOException ex) {
						Logger.getLogger(DescriptorRegistryView.class.getName()).log(Level.SEVERE, null, ex);
					}
				}

				try {
					Iterator<Row> i = t.rows();
					i.next();
					while (i.hasNext()) {
						Row r = i.next();

						Descriptor container = new Descriptor(r.getValues().values().toArray()[0].toString());
						container.setName(r.getValues().values().toArray()[1].toString());
						container.setDescription(r.getValues().values().toArray()[2].toString());
						container.setApplication(r.getValues().values().toArray()[3].toString());
						if (!qdbContext.getQdb().getDescriptorRegistry().contains(container)) {
							qdbContext.getQdb().getDescriptorRegistry().add(container);
							qdbContext.fire(new DescriptorEvent(this, DescriptorEvent.Type.Add, container));
						}
						openedFile = true;
					}
				} catch (Exception ex) {
					Utils.showExceptionPanel(ex.getMessage(), ex);
					Logger.getLogger(DescriptorRegistryView.class.getName()).log(Level.SEVERE, null, ex);
				}

			} catch (FileNotFoundException ex) {
				Logger.getLogger(DescriptorRegistryView.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				try {
					is.close();
				} catch (IOException ex) {
					Logger.getLogger(DescriptorRegistryView.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

		}
	}
}
