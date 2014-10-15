/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBException;
import org.dmg.pmml.IOUtil;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.jpmml.evaluator.RegressionModelEvaluator;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.conversion.regression.Equation;
import org.qsardb.conversion.regression.RegressionUtil;
import org.qsardb.editor.container.ModelModel;
import org.qsardb.model.ByteArrayPayload;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Payload;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;

public class EditMlrModel extends AbstractTableModel {
	private final ArrayList<Equation.Term> data = new ArrayList<Equation.Term>();
	private ModelModel model = null;

	public EditMlrModel() {
		Equation.Term intercept = new Equation.Term();
		intercept.setCoefficient("");
		data.add(intercept);
	}

	public void setModel(ModelModel model) throws IOException {
		this.model = model;
		Equation eq = loadEq();
		if (eq == null) {
			return;
		}

		data.clear();
		for (Equation.Term term: eq.getTerms()) {
			if (term.isIntercept()) {
				data.add(0, term);
			} else {
				data.add(term);
			}
		}
	}

	public void addDescriptor(Descriptor d) {
		Equation.Term term = new Equation.Term();
		term.setIdentifier(d.getId());
		data.add(term);
		fireTableRowsInserted(data.size() - 1, data.size());
	}

	public void removeDescriptor(int row) {
		data.remove(row);
		fireTableRowsDeleted(row, row);
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return "Coefficient";
			case 1:
				return "Id";
			case 2:
				return "Descriptor name";
		}
		throw new IllegalArgumentException("column=" + column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Equation.Term t = data.get(rowIndex);
		if (columnIndex == 0) {
			return data.get(rowIndex).getCoefficient();
		} else if (columnIndex == 1) {
			return t.isIntercept() ? "" : t.getIdentifier();
		} else if (columnIndex == 2) {
			if (t.isIntercept()) {
				return "Intercept";
			} else {
				Descriptor d = model.getQdbContext().getQdb().getDescriptor(t.getIdentifier());
				return d.getName();
			}
		}
		throw new IllegalArgumentException("col: " + columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			data.get(rowIndex).setCoefficient(aValue.toString());
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	List<String> getDescriptors() {
		ArrayList<String> l = new ArrayList<String>();
		for (Equation.Term term: data) {
			if (!term.isIntercept()) {
				l.add(term.getIdentifier());
			}
		}
		return l;
	}

	private Equation loadEq() throws IOException {
		if (!model.hasCargo(PMMLCargo.ID)) {
			return null;
		}

		try {
			PMMLCargo cargo = model.getContainer().getCargo(PMMLCargo.class);
			PMML pmml = cargo.loadPmml();

			Model pmmlModel = pmml.getModels().get(0);
			if (pmmlModel instanceof RegressionModel) {
				Qdb qdb = model.getQdbContext().getQdb();
				RegressionModel mlrModel = (RegressionModel)pmmlModel;
				return RegressionUtil.format(qdb, mlrModel);
			}
		} catch (QdbException e) {
			throw new IOException("Failed to parse PMML", e);
		}

		return null;
	}

	Payload getPayload() throws NumberFormatException {
		Equation eq = new Equation();
		eq.setIdentifier(model.getPropertyId());
		eq.setTerms(data);

		Qdb qdb = model.getQdbContext().getQdb();
		PMML pmml = RegressionUtil.parse(qdb, eq);
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			IOUtil.marshal(pmml, os);
			return new ByteArrayPayload(os.toByteArray());
		} catch (JAXBException ex) {
			throw new RuntimeException("Can't serialize PMML", ex);
		}
	}
}