/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import org.qsardb.editor.common.Utils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.QdbException;
import org.qsardb.storage.zipfile.ZipFileOutput;

public class ExportAction extends AbstractAction {
	private final QdbContext qdbContext;

	public ExportAction(QdbContext context) {
		super("Export to ZIP");
		qdbContext = context;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component parent = (Component) e.getSource();
		JFileChooser fc = Utils.getFileChooser();
		if (fc.showDialog(parent, "Export") == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			try {
				String file_name = f.toString();
				if (!file_name.endsWith(".zip")) {
					file_name += ".zip";
					f = new File(file_name);
				}
				ZipFileOutput storage = new ZipFileOutput(f);
				qdbContext.getQdb().copyTo(storage);
				storage.close();
			} catch (IOException | QdbException ex) {
				Utils.showExceptionPanel("Export failed: "+ex.getMessage(), ex);
			}
		}
	}
}