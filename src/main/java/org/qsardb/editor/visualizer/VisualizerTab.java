/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.qsardb.editor.common.ManagedJPanel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.events.ContainerEvent;
import org.qsardb.editor.events.ImportEvent;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.editor.registry.ContainerRenderer;
import org.qsardb.editor.registry.RegistryModel;
import org.qsardb.model.Model;

public class VisualizerTab {
	protected final QdbContext c;
	private JList list;
	private RegistryModel rm_model;
	private boolean updateNeeded;
	VisualizerView vis;
	private JPanel parentRight;

	public VisualizerTab(QdbContext context) {
		this.c = context;
		updateNeeded = false;
		rm_model = new RegistryModel(context, context.getQdb().getModelRegistry());
		list = new JList(rm_model.getListModel());
	}

	public JPanel make() {
		JPanel parentLeft = new JPanel();
		parentLeft.setLayout(new BorderLayout(6, 6));
		JPanel child1 = new JPanel(new BorderLayout(6, 6));
		child1.setBorder(BorderFactory.createEmptyBorder(10, 6, 6, 6));
		child1.add(new JLabel("Models:"), BorderLayout.LINE_START);
		child1.add(Box.createHorizontalGlue());
		parentLeft.add(child1, BorderLayout.NORTH);
		list.setCellRenderer(new ContainerRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(list);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting()) {
					return;
				}
				modelSelected();
			}
		});
		parentLeft.add(sp, BorderLayout.CENTER);

		vis = new VisualizerView(c);
		parentRight = vis.chartPanel();

		JPanel panel = new JPanel(new BorderLayout());
		ManagedJPanel mang = new ManagedJPanel(this.c, this);
		mang.setLayout(new BorderLayout());
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		panel.add(splitter, BorderLayout.CENTER);
		splitter.setDividerSize(6);
		splitter.setDividerLocation(400);

		JPanel hider = new JPanel(new BorderLayout());
		hider.add(parentRight);

		splitter.setLeftComponent(parentLeft);
		splitter.setRightComponent(hider);

		Dimension minWidth = new Dimension(0, 0);
		splitter.getLeftComponent().setMinimumSize(minWidth);
		splitter.getRightComponent().setMinimumSize(minWidth);
		mang.add(panel);
		return mang;
	}

	private void modelSelected() {
		int index = list.getSelectedIndex();
		if (index == -1) {
			return;
		}
		if (rm_model.getListModel().getSize() > index) {
			Model m = c.getQdb().getModel(rm_model.getListModel().getElementAt(index).getId());
			vis.modelSelected(m);
		} else {
			return;
		}
	}

	private void reDrawChart() {
		modelSelected();
	}

	@Subscribe
	public void handle(ModelEvent e) {
		rm_model.refresh();
	}

	@Subscribe
	public void handle(ImportEvent e) {
		rm_model.refresh();
		reDrawChart();
	}

	@Subscribe
	public void handle(ContainerEvent e) {
		setUpdateNeeded(true);
	}

	public boolean isUpdateNeeded() {
		return updateNeeded;
	}

	public void setUpdateNeeded(boolean update) {
		this.updateNeeded = update;
	}

	public void refreshView() {
		reDrawChart();
	}
}
