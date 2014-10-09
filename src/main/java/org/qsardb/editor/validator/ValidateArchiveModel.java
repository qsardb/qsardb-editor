/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.validator;

import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Qdb;
import org.qsardb.storage.memory.MemoryStorage;
import org.qsardb.validation.Message;
import org.qsardb.validation.MessageCollector;

class ValidateArchiveModel extends AbstractTableModel {
	private final QdbContext qdbContext;
	private final ArrayList<Message> messages = new ArrayList<Message>();
	private final ImageIcon errorIcon;
	private final ImageIcon warningIcon;

	public ValidateArchiveModel(QdbContext context) {
		qdbContext = context;
		errorIcon = loadIcon("error.png");
		warningIcon = loadIcon("warning.png");
	}

	public void validate(String level) {
		messages.clear();

		ValidationLevel validator = ValidationLevel.valueOf(level);
		MessageCollector collector = new MessageCollector() {
			@Override
			public void add(Message message) {
				messages.add(message);
			}
		};

		// XXX validators seem have side effects and modify Qdb object
		// this will need further investigation... temporary workaround
		// is to copy the qdb object to temporary storage and validate
		// on the copy instead
		try {
			MemoryStorage storage = new MemoryStorage();
			qdbContext.getQdb().copyTo(storage);
			Qdb tmpQdb = new Qdb(storage);
			validator.validate(tmpQdb, collector);
			tmpQdb.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			fireTableDataChanged();
		}
	}

	private ImageIcon loadIcon(String path) {
		URL url = getClass().getResource(path);
		if (url != null) {
			return new ImageIcon(url);
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return messages.size();
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "";
		} else if (column == 1) {
			return "Path";
		} else if (column == 2) {
			return "Message";
		}
		throw new IllegalArgumentException("column="+column);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			Message.Level level = messages.get(rowIndex).getLevel();
			if (level == Message.Level.ERROR && errorIcon != null) {
				return errorIcon;
			}
			if (level == Message.Level.WARNING && warningIcon != null) {
				return warningIcon;
			}
			throw new IllegalStateException();
		} else if (columnIndex == 1) {
			return messages.get(rowIndex).getPath();
		} else if (columnIndex == 2) {
			return messages.get(rowIndex).getContent();
		}
		throw new IllegalArgumentException("column="+columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return ImageIcon.class;
		} else {
			return super.getColumnClass(columnIndex);
		}
	}
}