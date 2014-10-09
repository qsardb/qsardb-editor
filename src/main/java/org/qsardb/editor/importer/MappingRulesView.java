package org.qsardb.editor.importer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.qsardb.conversion.sdfile.SDFile;
import org.qsardb.conversion.table.Table;
import org.qsardb.editor.common.QdbContext;

public class MappingRulesView {
	private final JTable table;
	private final MappingRulesModel model;
	private final QdbContext qdbContext;

	public MappingRulesView(QdbContext context, Table dataTable) {
		qdbContext = context;
		model = new MappingRulesModel(context, dataTable);

		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn editColumn = table.getColumnModel().getColumn(2);
		RenderSetupCell setupCell = new RenderSetupCell();
		editColumn.setCellRenderer(setupCell);

		// set table row height based on the tallest edit column
		Component comp = table.prepareRenderer(table.getCellRenderer(0, 2), 0, 2);
		table.setRowHeight(comp.getPreferredSize().height);

		table.getColumnModel().getColumn(0).setWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		editColumn.setMaxWidth(comp.getPreferredSize().width + 4);
	}

	public JPanel buildView() {
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		table.addMouseListener(new PopupListener());

		JPanel view = new JPanel(new BorderLayout());
		view.add(scrollPane, BorderLayout.CENTER);
		return view;
	}

	public MappingRulesModel getModel() {
		return model;
	}

	public ListSelectionModel getSelectionModel() {
		return table.getSelectionModel();
	}

	public MappingRule getSelectedRule() {
		int row = table.getSelectedRow();
		return row != -1 ? model.getMappingRule(row) : null;
	}

	private static class RenderSetupCell implements TableCellRenderer {
		private final JButton renderButton = new JButton();

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			renderButton.setText((String) value);
			return renderButton;
		}
	}

	private class PopupListener extends MouseAdapter {
		final JPopupMenu popup = createPopupMenu();
		final JPopupMenu sdfPopup = createSdfPopupMenu();

		@Override
		public void mousePressed(MouseEvent e) {
			handlePopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			handlePopup(e);
		}
		
		private void handlePopup(MouseEvent e) {
			int row = table.rowAtPoint(e.getPoint());
			if (row == -1) {
				return;
			}
			if (e.isPopupTrigger() && table.getSelectedRow() != row) {
				table.setRowSelectionInterval(row, row);
			}
			
			int col = table.columnAtPoint(e.getPoint());
			if (e.isPopupTrigger() || col == 2) {
				
				MappingRule rule = model.getMappingRule(row);
				String srcId = rule.getSourceColumnId();
				if (srcId.equals(SDFile.COLUMN_MOLFILE)) {
					sdfPopup.show(e.getComponent(), e.getX(), e.getY());
				} else {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
		
		private JPopupMenu createPopupMenu() {
			JPopupMenu m = new JPopupMenu();
			EnumSet<MapTo> mappings = EnumSet.allOf(MapTo.class);
			mappings.remove(MapTo.MOLFILE);
			for (MapTo i: mappings) {
				m.add(i.createAction(qdbContext, table));
			}
			
			return m;
		}
		
		private JPopupMenu createSdfPopupMenu() {
			JPopupMenu m = new JPopupMenu();
			m.add(MapTo.IGNORE.createAction(qdbContext, table));
			m.add(MapTo.MOLFILE.createAction(qdbContext, table));
			return m;
		}
	}
}