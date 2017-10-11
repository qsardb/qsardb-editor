/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import org.qsardb.conversion.table.Cell;
import org.qsardb.conversion.table.Row;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.model.Descriptor;
import org.qsardb.model.DescriptorRegistry;

public class DescriptorImportAction extends TableImportAction {
	public DescriptorImportAction(QdbContext context) {
		super(context, "Import descriptors");
	}

	@Override
	protected void performImport(Table table, Component parent) {
		try {
			DescriptorRegistry dr = qdbContext.getQdb().getDescriptorRegistry();
			Iterator<Row> i = table.rows();
			if (!checkFormat(i.next())) {
				Utils.showError("Invalid header, expected columns named: DescriptorID,Name,Application,Description");
				return;
			}
			while (i.hasNext()) {
				Row r = i.next();
				ArrayList<String> values = getValues(r);

				String did = values.get(0);
				Descriptor descriptor = dr.get(did); 
				if (descriptor == null) {
					descriptor = new Descriptor(did);
				}

				descriptor.setName(values.get(1));
				descriptor.setApplication(values.get(2));
				descriptor.setDescription(values.get(3));

				if (dr.contains(descriptor)) {
					qdbContext.fire(new DescriptorEvent(this, DescriptorEvent.Type.Update, descriptor));
				} else {
					dr.add(descriptor);
					qdbContext.fire(new DescriptorEvent(this, DescriptorEvent.Type.Add, descriptor));
				}
			}
		} catch (Exception ex) {
			Utils.showExceptionPanel(ex.getMessage(), ex);
		}
	}

	private boolean checkFormat(Row r) {
		ArrayList<String> h = getValues(r);
		return ("DescriptorID".equalsIgnoreCase(h.get(0)) || "ID".equalsIgnoreCase(h.get(0)))
				&& "Name".equalsIgnoreCase(h.get(1))
				&& "Application".equalsIgnoreCase(h.get(2))
				&& "DescriptorID".equalsIgnoreCase(h.get(3));
	}

	private ArrayList<String> getValues(Row row) {
		ArrayList<String> result = new ArrayList<String>();
		for (Cell cell: row.getValues().values()) {
			result.add(cell.getText() != null ? cell.getText() : "");
		}
		return result;
	}
}
