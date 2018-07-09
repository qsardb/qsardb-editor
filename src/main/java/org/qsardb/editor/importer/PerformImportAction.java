/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.importer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.qsardb.conversion.table.Mapping;
import org.qsardb.conversion.table.Table;
import org.qsardb.conversion.table.Table2Qdb;
import org.qsardb.conversion.table.TableSetup;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.ImportEvent;
import org.qsardb.model.Qdb;

public class PerformImportAction extends AbstractAction {

	private final QdbContext qdbContext;
	private final MappingRulesModel rulesModel;
	private final Table dataTable;

	public PerformImportAction(QdbContext context, Table table, MappingRulesModel model) {
		super("Import");
		qdbContext = context;
		dataTable = table;
		rulesModel = model;
		setEnabled(rulesModel.getIdMappingRule() != null);

		rulesModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				setEnabled(rulesModel.getIdMappingRule() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			importTable();
		} catch (IllegalArgumentException ex) {
			Utils.showExceptionPanel(ex.getMessage(), ex);
		}
	}

	public void importTable() {
		MappingRule idRule = rulesModel.getIdMappingRule();
		try {
			for (int i=0; i<rulesModel.getRowCount(); i++) {
				MappingRule rule = rulesModel.getMappingRule(i);
				if (rule.getMapTo() == MapTo.IGNORE || idRule.equals(rule)) {
					continue;
				}

				importColumn(idRule, rule);
			}
		} finally {
			qdbContext.fire(new ImportEvent(this));
		}
	}

	private void importColumn(MappingRule idRule, MappingRule rule) {
		TableSetup setup = new TableSetup();
		if (rulesModel.hasHeader()) {
			setup.setIgnored("1");
		}
		
		Mapping idMapping = idRule.getMapTo().createMapping(null);
		setup.addMapping(idRule.getSourceColumnId(), idMapping);
		
		Mapping mapping = rule.getMapTo().createMapping(rule.getArgument());
		setup.addMapping(rule.getSourceColumnId(), mapping);
		
		try {
			Qdb qdb = qdbContext.getQdb();
			Table2Qdb.convert(qdb, dataTable, setup);
		} catch (Exception ex) {
			String msg = "Error mapping column "+rule.getSourceColumnHeading()+ " to " + rule.getMapTo() + ": " + ex.getMessage();
			throw new IllegalArgumentException(msg, ex);
		}
	}
}