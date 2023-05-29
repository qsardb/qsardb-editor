/*
 * Copyright (c) 2023 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.bind.JAXBException;

import org.dmg.pmml.AbstractSimpleVisitor;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.IOUtil;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.VisitorAction;
import org.qsardb.cargo.pmml.FieldNameUtil;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ModelModel;
import org.qsardb.model.ByteArrayPayload;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Payload;
import org.qsardb.model.Property;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;
import org.xml.sax.SAXException;

public class PmmlImporter extends EditCargoView {
	private final ArrayList<String> pmmlFields = new ArrayList<>();
	private final ArrayList<String> qdbFields = new ArrayList<>();
	private final ArrayList<String> allQdbDescriptors = new ArrayList<>();
	private final LinkedHashMap<String, String> mapping = new LinkedHashMap<>();

	private final Qdb qdb;
	private final Model container;
	private PMML pmml;

	public static void main(String[] args) throws Exception { // SS: remove this
		QdbContext ctx = new QdbContext();
		ctx.loadArchive(new File("/tmp/test.qdb"));
		Model m = ctx.getQdb().getModel("M2");
		ModelModel modelModel = new ModelModel(ctx, m);
		//File f = new File("/tmp/test.qdb/models/M2/pmml");
		File f = new File("rf.pmml");
		//File f = new File("rf2a.pmml");
		//File f = new File("m1b.pmml");
		PmmlImporter gui = new PmmlImporter(modelModel);
		boolean ok = gui.autoLoad(f);
		if (!ok) {
			gui.showModal("Edit PMML fields");
		}
		ctx.getQdb().storeChanges();
		ctx.getQdb().close();
	}

	public PmmlImporter(ModelModel model) {
		super(model, PMMLCargo.ID);
		qdb = model.getQdbContext().getQdb();
		container = model.getContainer();

		allQdbDescriptors.add("");
		for (Descriptor d: qdb.getDescriptorRegistry()) {
			allQdbDescriptors.add(d.getId());
		}
	}

	public boolean autoLoad(File f) {
		try {
			pmml = IOUtil.unmarshal(f);
		} catch (IOException | SAXException | JAXBException e) {
			throw new RuntimeException(e); // XXX
		}

		List<org.dmg.pmml.Model> models = pmml.getModels();
		if (models.size() != 1) {
			throw new IllegalArgumentException();
		}
		MiningSchema ms = models.get(0).getMiningSchema();

		boolean descsMatching = true;

		for (MiningField df : ms.getMiningFields()) {
			FieldName name = df.getName();
			if (df.getUsageType() == FieldUsageType.ACTIVE) {
				Descriptor d = FieldNameUtil.decodeDescriptor(qdb, df.getName());
				String qdbField = "";
				if (d != null) {
					qdbField = d.getId();
				} else {
					descsMatching = false;
				}
				pmmlFields.add(name.getValue());
				qdbFields.add(qdbField);
			} else if (df.getUsageType() == FieldUsageType.PREDICTED) {
				Property p = FieldNameUtil.decodeProperty(qdb, name);
				if (!container.getProperty().equals(p)) {
					String pid = container.getProperty().getId();
					FieldName qdbField = FieldNameUtil.encodePropertyId(pid);
					mapping.put(name.getValue(), qdbField.getValue());
				}
			} else {
				throw new IllegalArgumentException("usageType="+df.getUsageType().value());
			}
		}

		if (!descsMatching) {
			return false;
		}

		try {
			model.setCargoPayload(cargoId, createPayload());
		} catch (QdbException ex) {
			Utils.showError("Can't serialize editor content: "+ex.getMessage()); // XXX: throw io exception?
		}
		return true;
	}

	@Override
	protected JComponent buildContentPanel() {
		JPanel view = new JPanel(new BorderLayout());
		view.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		AbstractTableModel tableModel = new AbstractTableModel() {
			@Override
			public int getRowCount() {
				return pmmlFields.size();
			}

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (columnIndex == 0) {
					return pmmlFields.get(rowIndex);
				} else if (columnIndex == 1) {
					return qdbFields.get(rowIndex);
				} else {
					throw new IllegalStateException();
				}
			}

			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				String v = (String) value;

				if (columnIndex == 1) {
					for (int i=0; i<qdbFields.size(); i++) {
						if (v.isEmpty()) {
						} else if (qdbFields.get(i).equals(v)) {
							qdbFields.set(i, "");
						}
					}
					qdbFields.set(rowIndex, v);
					fireTableDataChanged();
				} else {
					throw new IllegalStateException();
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 1;
			}
		};

		JTable table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setHeaderValue("PMML fields");
		table.getColumnModel().getColumn(1).setHeaderValue("Descriptors");

		JComboBox<String> comboBox = new JComboBox<>(allQdbDescriptors.toArray(new String[0]));
		DefaultCellEditor editor = new DefaultCellEditor(comboBox);
		table.getColumnModel().getColumn(1).setCellEditor(editor);

		ComboBoxCellRenderer renderer = new ComboBoxCellRenderer();
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);
		table.setRowHeight(renderer.getPreferredSize().height + 2);

		view.add(new JScrollPane(table), BorderLayout.CENTER);

		return view;
	}

	@Override
	protected Payload createPayload() throws QdbException {
		for (int i=0; i<pmmlFields.size(); i++) {
			String pmmlField = pmmlFields.get(i);
			String qdbField = qdbFields.get(i);

			if (qdbField.isEmpty()) {
				// leave it unchanged, user can define it later
				continue;
			}

			qdbField = FieldNameUtil.encodeDescriptorId(qdbField).getValue();
			if (!pmmlField.equals(qdbField)) {
				mapping.put(pmmlField, qdbField);
			}
		}
		if (!mapping.isEmpty()) {
			pmml.accept(new Visitor(mapping));
		}

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			IOUtil.marshal(pmml, os);
			return new ByteArrayPayload(os.toByteArray());
		} catch (JAXBException ex) {
			throw new RuntimeException("Can't serialize PMML", ex);
		}
	}

	private static class Visitor extends AbstractSimpleVisitor {

		private final LinkedHashMap<String, String> mapping;

		private Visitor(LinkedHashMap<String, String> mapping) {
			this.mapping = mapping;
		}

		@Override
		public VisitorAction visit(PMMLObject o) {
			java.lang.reflect.Field[] fields = o.getClass().getDeclaredFields();
			for (java.lang.reflect.Field f : fields) {
				if (f.getType().equals(FieldName.class)) {
					try {
						f.setAccessible(true);
						FieldName name = (FieldName) f.get(o);
						if (name != null && mapping.containsKey(name.getValue())) {
							f.set(o, FieldName.create(mapping.get(name.getValue())));
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException();
					}
				}
			}
			return VisitorAction.CONTINUE;
		}
	}

	private class ComboBoxCellRenderer extends JComboBox<String> implements TableCellRenderer {

		public ComboBoxCellRenderer() {
			super(allQdbDescriptors.toArray(new String[0]));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setSelectedItem(value);

			if (isSelected) {
				setBackground(table.getSelectionBackground());
			} else {
				setBackground(table.getBackground());
			}

			return this;
		}
	}

}
