/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.validator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import static javax.swing.Action.NAME;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Container;
import org.qsardb.validation.Message;

public class ValidateArchiveView {
	private final JButton validate = new JButton("validate");
	private JButton cancelValidate = new JButton("cancel");
	public static JProgressBar pbar;
	private JPanel view;
	private JPanel validationViewContainer;
	private JLabel errorCount;
	private final ImageIcon errorIcon;
	private final ImageIcon loadingIcon;
	private final ImageIcon warningIcon;
	private final QdbContext c;
	private JComboBox valLevel;
	private ArrayList<ValidationAction> s;
	private SwingWorker sw;

	public ValidateArchiveView(QdbContext context) {
		errorIcon = loadIcon("error.png");
		warningIcon = loadIcon("warning.png");
		loadingIcon = loadIcon("loading.gif");
		c = context;
		s = new ArrayList<ValidationAction>();
	}

	public JComponent createView() {
		view = new JPanel(new BorderLayout());
		validationViewContainer = new JPanel();
		validationViewContainer.setLayout(new BorderLayout());
		view.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		errorCount = new JLabel();

		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		view.add(controls, BorderLayout.NORTH);

		pbar = new JProgressBar();
		controls.add(new JLabel("QsarDB validation level:"));
		valLevel = new JComboBox();
		for (ValidationLevel vl : ValidationLevel.values()) {
			valLevel.addItem(vl);
			s.add(new ValidationAction(vl));
		}
		valLevel.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				validationViewContainer.removeAll();
				validationViewContainer.add(s.get(valLevel.getSelectedIndex()).getValidationPanel());
				validate.setAction(s.get(valLevel.getSelectedIndex()));
				validate.setText("validate");
				validate.setEnabled(true);
				validationViewContainer.updateUI();
				int errornum = s.get(valLevel.getSelectedIndex()).numOfLeafs;
				if (errornum == -1) {
					errorCount.setText(" errors: N/A");
				} else {
					errorCount.setText(" errors: " + s.get(valLevel.getSelectedIndex()).numOfLeafs);
				}
			}
		});

		controls.add(valLevel);
		validate.setHideActionText(true);
		controls.add(validate);
		controls.add(cancelValidate);
		controls.add(errorCount);
		controls.add(Box.createHorizontalGlue());
		controls.add(pbar, Box.RIGHT_ALIGNMENT);

		cancelValidate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sw.cancel(false);
				pbar.setValue(0);
				cancelValidate.setEnabled(false);
			}
		});
		cancelValidate.setEnabled(false);

		view.add(validationViewContainer, BorderLayout.CENTER);
		errorCount.setText(" errors: ");
		valLevel.setSelectedIndex(1);
		valLevel.setSelectedIndex(0);

		view.updateUI();
		return view;
	}

	private class ValidationAction extends AbstractAction {

		public JTree t;
		private JPanel options;
		private final JScrollPane treePane;
		private final JPanel validationView;
		public int numOfLeafs;
		private final ValidateArchiveModelTree vt2;
		private String[] values;

		public JTree getValidationTree() {
			return t;
		}

		public JPanel getValidationPanel() {
			return validationView;
		}

		public ValidationAction(ValidationLevel level) {
			super(level.name().toLowerCase());

			validationView = new JPanel();
			validationView.setLayout(new BorderLayout());
			treePane = new JScrollPane();
			treePane.setVerticalScrollBarPolicy(ScrollPaneLayout.VERTICAL_SCROLLBAR_AS_NEEDED);
			vt2 = new ValidateArchiveModelTree(new DefaultMutableTreeNode("root"), c);
			t = new JTree(vt2);
			t.setRootVisible(false);
			t.setCellRenderer(getRenderer());
			t.addMouseListener(getMouseListener(t));
			numOfLeafs = -1;
			if (level.name() == null ? ValidationLevel.STRUCTURE.name() == null : level.name().equals(ValidationLevel.STRUCTURE.name())) {
				options = new JPanel();
				options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
				options.add(new JCheckBox("InChI"));
				options.add(new JCheckBox("SMILES"));
				options.add(new JCheckBox("MDL"));
				options.add(new JCheckBox("Name"));
				for (Component cb : options.getComponents()) {
					((JCheckBox) cb).setSelected(true);
				}
				validationView.add(options, BorderLayout.WEST);
			}
			validationView.add(new JScrollPane(t));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			pbar.setMaximum(100);
			pbar.setMinimum(0);
			view.setOpaque(true);
			values = new String[4];
			final String level = (String) getValue(NAME);
			if (level.equals(ValidationLevel.STRUCTURE.name().toLowerCase())) {
				for (int i = 0; i < 4; i++) {
					if (((JCheckBox) options.getComponent(i)).isSelected()) {
						values[i] = ((JCheckBox) options.getComponent(i)).getText();
					} else {
						values[i] = null;
					}
				}
			}

			sw = new SwingWorker() {
				@Override
				protected Object doInBackground() throws Exception {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							JTabbedPane tab = (JTabbedPane) view.getParent();
							tab.setIconAt(6, loadingIcon);
						}
					});
					validate.setEnabled(false);
					cancelValidate.setEnabled(true);

					errorCount.setText("");

					vt2.validate(level.toUpperCase(), values, sw);

					errorCount.setText("\t errors: " + vt2.getLeafCount());
					t.setVisible(false);
					for (int i = 0; i < t.getRowCount(); i++) {
						t.expandRow(i);
					}
					numOfLeafs = vt2.getLeafCount();
					validationViewContainer.add(validationView);
					treePane.updateUI();
					view.updateUI();
					validate.setEnabled(true);
					cancelValidate.setEnabled(false);
					t.setVisible(true);
					pbar.setValue(0);
					JTabbedPane tab = (JTabbedPane) view.getParent();
					tab.setIconAt(6, null);
					return true;
				}
			};
			sw.execute();
		}

		private DefaultTreeCellRenderer getRenderer() {
			DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer() {
				private JLabel label = new JLabel();

				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					Object o = ((DefaultMutableTreeNode) value).getUserObject();
					if (o instanceof Message) {
						Message c = (Message) o;

						if (((Message) o).getLevel().equals(Message.Level.WARNING)) {
							label.setIcon(warningIcon);
						}
						if (((Message) o).getLevel().equals(Message.Level.ERROR)) {
							label.setIcon(errorIcon);
						}

						label.setText(c.getContent());
						if (hasFocus) {
							label.setOpaque(true);
							label.setBackground(getBackgroundSelectionColor());
							label.setForeground(getTextSelectionColor());
						} else {
							label.setBackground(getBackgroundNonSelectionColor());
							label.setForeground(getTextNonSelectionColor());
						}
					} else {
						return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					}
					label.setLayout(new BorderLayout());

					return label;
				}

			};
			return dtcr;
		}

		private MouseListener getMouseListener(final JTree tree) {
			MouseListener ml = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					if (selRow != -1) {
						if (e.getClickCount() == 1) {
						} else if (e.getClickCount() == 2) {
							String tab = selPath.getPathComponent(1).toString();
							if (selPath.getPathCount() <= 3) {
								return;
							}

							JTabbedPane t = (JTabbedPane) view.getParent();
							JComponent p1 = null;
							for (int i = 0; i < t.getTabCount(); i++) {
								if (t.getTitleAt(i).toLowerCase() == null ? tab.toLowerCase() == null : t.getTitleAt(i).toLowerCase().equals(tab.toLowerCase())) {
									t.setSelectedIndex(i);
									p1 = (JComponent) t.getComponentAt(i);
								}
							}

							JSplitPane a = (JSplitPane) p1.getComponent(0);
							JPanel p = (JPanel) a.getLeftComponent();
							JScrollPane sp = (JScrollPane) p.getComponent(1);
							JViewport l = (JViewport) sp.getComponent(0);
							JList jl = (JList) l.getComponent(0);

							Container c;
							for (int i = 0; i < jl.getModel().getSize(); i++) {
								c = (Container) jl.getModel().getElementAt(i);
								if (c.getId() == null ? selPath.getPathComponent(2).toString() == null : c.getId().equals(selPath.getPathComponent(2).toString())) {
									jl.setSelectedIndex(i);
								}
							}
							jl.ensureIndexIsVisible(jl.getSelectedIndex());
						}
					}
				}
			};
			return ml;
		}
	;

	}

	class MessageCellRenderer implements TreeCellRenderer {

		private JLabel label;

		MessageCellRenderer() {
			label = new JLabel();
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Object o = ((DefaultMutableTreeNode) value).getUserObject();
			if (o instanceof Message) {
				Message country = (Message) o;

				if (((Message) o).getLevel().equals(Message.Level.WARNING)) {
					label.setIcon(warningIcon);
				}
				if (((Message) o).getLevel().equals(Message.Level.ERROR)) {
					label.setIcon(errorIcon);
				}

				label.setText(country.getContent());
			} else {
				label.setIcon(null);
				label.setText("" + value);
			}
			return label;
		}
	}

	private ImageIcon loadIcon(String path) {
		URL url = getClass().getResource(path);
		if (url != null) {
			return new ImageIcon(url);
		}
		return null;
	}
}