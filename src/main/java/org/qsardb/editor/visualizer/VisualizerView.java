/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer;

import org.qsardb.editor.visualizer.chartData.DataSeries;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.visualizer.chartData.DataCollector;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
import org.qsardb.statistics.ClassificationStatistics;
import org.qsardb.statistics.RegressionStatistics;
import org.qsardb.statistics.Statistics;
import org.qsardb.statistics.StatisticsUtil;

public class VisualizerView {
	private JPanel residualError;
	private QdbContext c;
	private JPanel propertyAnalysis, descriptorAnalys, statPanel;
	public static JPanel parentRight;
	private DataCollector vd;
	private TitledBorder title;
	public static ArrayList<Color> colList;
	private JPanel j2Cont;
	private HashSet<Integer> toHide;
	private ClassificationData cd;

	public VisualizerView(QdbContext c) {
		this.c = c;
		toHide = new HashSet<Integer>();
		colList = new ArrayList<Color>();
		colList.add(Color.BLUE);
		colList.add(Color.RED);
		colList.add(Color.ORANGE);
		colList.add(Color.MAGENTA);
		colList.add(Color.BLACK);
		colList.add(Color.PINK);
		colList.add(Color.GREEN);
		colList.add(Color.GRAY);
		colList.add(Color.CYAN);
		colList.add(Color.YELLOW);
	}

	public JPanel chartPanel() {
		parentRight = new JPanel();
		parentRight.setLayout(new BoxLayout(parentRight, BoxLayout.Y_AXIS));
		statPanel = new JPanel();
		statPanel.setLayout(new BorderLayout());
		statPanel.setMaximumSize(new Dimension(5000, 500));
		propertyAnalysis = new JPanel(new GridLayout(1, 1));
		residualError = new JPanel(new GridLayout(0, 2));
		descriptorAnalys = new JPanel();
		descriptorAnalys.setLayout(new BoxLayout(descriptorAnalys, BoxLayout.PAGE_AXIS));
		statPanel.setBorder(buildBorderStatpanel());

		Border paddingBorder = BorderFactory.createEmptyBorder(4, 10, 4, 10);

		JLabel j1 = new JLabel("- Property analysis");
		j1.setBorder(paddingBorder);
		j1.addMouseListener(getMouseAdapter(propertyAnalysis));

		JLabel j2 = new JLabel("- Residual Error");
		j2.setBorder(paddingBorder);
		j2.addMouseListener(getMouseAdapter(residualError));

		JLabel j3 = new JLabel("- Descriptor analysis");
		j3.setBorder(paddingBorder);
		j3.addMouseListener(getMouseAdapter(descriptorAnalys));

		JPanel j1Cont = new JPanel();
		j1Cont.setLayout(new BorderLayout());
		j1Cont.add(j1);
		j1Cont.setBackground(Color.lightGray);
		j1Cont.setMaximumSize(new Dimension(10000, 36));
		j2Cont = new JPanel();
		j2Cont.setLayout(new BorderLayout());
		j2Cont.add(j2);
		j2Cont.setBackground(Color.lightGray);
		j2Cont.setMaximumSize(new Dimension(10000, 36));
		JPanel j3Cont = new JPanel();
		j3Cont.setLayout(new BorderLayout());
		j3Cont.add(j3);
		j3Cont.setBackground(Color.lightGray);
		j3Cont.setMaximumSize(new Dimension(10000, 36));

		JPanel chartContainer = new JPanel();
		chartContainer.setLayout(new BoxLayout(chartContainer, BoxLayout.PAGE_AXIS));

		chartContainer.add(j1Cont);
		chartContainer.add(propertyAnalysis);
		chartContainer.add(j2Cont);
		chartContainer.add(residualError);
		chartContainer.add(j3Cont);
		chartContainer.add(descriptorAnalys);

		JPanel mjp = new JPanel();
		chartContainer.add(mjp, BorderLayout.PAGE_END);

		JScrollPane scrollPane = new JScrollPane(chartContainer);
		parentRight.add(statPanel);
		parentRight.add(scrollPane);
		parentRight.setVisible(false);
		return parentRight;

	}

	public JPanel loadStatPanel(Model m) {
		statPanel.removeAll();
		JPanel panel = new JPanel() {
			@Override
			public String getToolTipText(MouseEvent e) {
				Border border = getBorder();

				if (border instanceof TitledBorder) {
					TitledBorder tb = (TitledBorder) border;
					FontMetrics fm = getFontMetrics(tb.getTitleFont());
					int titleWidth = fm.stringWidth(tb.getTitle()) + 20;
					Rectangle bounds = new Rectangle(0, 0, titleWidth, fm.getHeight());
					return bounds.contains(e.getPoint()) ? super.getToolTipText() : null;
				}
				return super.getToolTipText(e);
			}
		};
		panel.setToolTipText("");
		panel.setMaximumSize(new Dimension(5000, 500));
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.7;
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("name"), c);
		c.weightx = 0.3;
		c.gridx = 1;
		panel.add(new JLabel("type"), c);
		c.gridx = 2;
		panel.add(new JLabel("n"), c);
		c.gridx = 3;
		panel.add(new JLabel("R²"), c);
		c.gridx = 4;
		panel.add(new JLabel("σ"), c);
		Collection<Prediction> dep = this.c.getQdb().getPredictionRegistry().getByModel(m);
		Object[] p = dep.toArray();
		for (int i = 0; i < dep.size(); i++) {
			if (((Prediction) p[i]).getType().equals(Prediction.Type.TESTING)) {
				continue;
			}
			RegressionStatistics rs = (RegressionStatistics) StatisticsUtil.evaluate(m, (Prediction) p[i]);
			String id = ((Prediction) p[i]).getId();
			String type = ((Prediction) p[i]).getType().name().toLowerCase();
			final int fin = i;
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i + 1;
			gbc.weightx = 0.7;
			gbc.anchor = GridBagConstraints.WEST;
			JCheckBox j1;
			panel.add(j1 = new JCheckBox(id, null, true), gbc);
			j1.setToolTipText(id);
			j1.setEnabled(true);
			j1.addItemListener(new ItemListener() {
				int loc = fin;

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == 1) {
						setDataActive(loc);
					} else {
						setDataDeactive(loc);
					}
				}
			});
			j1.setForeground(VisualizerView.colList.get(i));
			gbc.weightx = 0.3;
			gbc.gridx = 1;
			panel.add(new JLabel(type), gbc);
			gbc.gridx = 2;
			panel.add(new JLabel(Integer.toString(rs.size())), gbc);
			gbc.gridx = 3;
			panel.add(new JLabel(String.format("%.3f", rs.rsq())), gbc);
			gbc.gridx = 4;
			panel.add(new JLabel(String.format("%.3f", rs.stdev())), gbc);
		}

		return panel;
	}

	public void clear() {
		residualError.removeAll();
		propertyAnalysis.removeAll();
		descriptorAnalys.removeAll();
		parentRight.updateUI();
	}

	public void modelSelected(Model m) {
		toHide = new HashSet<Integer>();
		parentRight.setVisible(true);
		clear();
		Statistics s = StatisticsUtil.evaluate(m, new Prediction(null, null, null));
		statPanel.removeAll();
		if (s.getClass().equals(RegressionStatistics.class)) {
			statPanel.add(loadStatPanel(m));
			loadRegressionData(m);
		} else if (s.getClass().equals(ClassificationStatistics.class)) {
			cd = new ClassificationData(m);
			statPanel.add(loadClassificationStatPanel(m));
			loadClassificationData(m);
		}

		parentRight.updateUI();

	}

	private void loadClassificationData(Model m) {
		vd = new DataCollector();
		title.setTitle(cd.getTitle());
		loadPropAnalysisClassification();
		vd.loadDescriptorAnalysisDataClassification(m);
		loadDesAnalysis();
	}

	private void loadPropAnalysisClassification() {
		propertyAnalysis.removeAll();
		JPanel p1 = new JPanel();
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder tb = BorderFactory.createTitledBorder(loweredetched, cd.getPropertyTitle());
		p1.setBorder(tb);
		j2Cont.setVisible(false);
		p1.add(loadClassificationPropPanel());
		propertyAnalysis.add(p1);
		parentRight.updateUI();
	}

	private void loadRegressionData(Model m) {
		vd = new DataCollector();
		j2Cont.setVisible(true);
		title.setTitle(m.getId().concat(" " + m.getName()));
		vd.loadData(m, c);

		loadPropAnalysis();
		loadResError();
		loadDesAnalysis();
	}

	public void redrawRegressionCharts() {
		if (vd.getPropertyChartSeries() != null) {
			loadPropAnalysis();
		}
		if (vd.getResErrorChartSeries() != null) {
			loadResError();
		}

		loadDesAnalysis();

		parentRight.updateUI();
	}

	public void redrawClassificationCharts() {
		loadDesAnalysis();
		loadPropAnalysisClassification();

		parentRight.updateUI();
	}

	protected void loadResError() {
		residualError.removeAll();
		if (vd.getResErrorChartSeries().getDataSeriesCount() != 0) {
			JPanel panel = new JPanel();

			ChartGenerator chart = new ChartGenerator(null, c);
			panel = chart.getGraph(((DataCollector) vd).getResErrorChartSeries().getDataSeries(0), "Experimental", "Residual Error");
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder tb = BorderFactory.createTitledBorder(loweredetched, "Residual error");
			panel.setBorder(tb);

			residualError.add(panel);
		}
	}

	protected void loadPropAnalysis() {
		propertyAnalysis.removeAll();
		if (vd.getPropertyChartSeries().getDataSeriesCount() != 0) {
			ChartGenerator chart = new ChartGenerator(null, c);
			JPanel jp = new JPanel(new GridLayout(1, 2));
			JPanel histoPanel = chart.getHistogram(vd.getPropertyChartSeries().getDataSeries(0), "Experimental", "Frequency");
			JPanel panel = chart.getGraph(vd.getPropertyChartSeries().getDataSeries(0), "Experimental", "Calculated");
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder tb = BorderFactory.createTitledBorder(loweredetched, vd.getPropertyChartSeries().getDataSeries(0).getName());
			jp.setBorder(tb);
			jp.add(panel);
			jp.add(histoPanel);
			propertyAnalysis.add(jp);
		}
	}

	protected void loadDesAnalysis() {
		descriptorAnalys.removeAll();

		ChartGenerator chart = null;
		for (DataSeries ds : vd.getDescriptorChartSeries().getAsArrayList()) {
			JPanel jp = new JPanel(new GridLayout(1, 2));
			chart = new ChartGenerator(null, c);
			if (vd.getPropertyChartSeries() != null) {
				//chart = new ChartGenerator(null, c);
				JPanel panel = chart.getGraph(ds, "Descriptor", "Experimental property");
				jp.add(panel);
			}
			JPanel histoPanel = chart.getHistogram(ds, "Experimental", "Frequency");
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder tb = BorderFactory.createTitledBorder(loweredetched, ds.getName());
			jp.setBorder(tb);

			jp.add(histoPanel);
			descriptorAnalys.add(jp);
		}
	}

	protected Border buildBorderStatpanel() {
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		title = BorderFactory.createTitledBorder(loweredetched, "title");
		title.setTitlePosition(TitledBorder.TOP);
		Font titleFont = UIManager.getFont("TitledBorder.font");
		title.setTitleFont(titleFont.deriveFont(Font.ITALIC + Font.BOLD));
		return title;
	}

	protected void setDataActive(int k) {
		vd.setDataActive(k);
		redrawRegressionCharts();
	}

	protected void setDataDeactive(int k) {
		vd.setDataDeactive(k);
		redrawRegressionCharts();
	}

	protected MouseAdapter getMouseAdapter(final JPanel jp) {
		MouseAdapter mAdap = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (jp.isVisible()) {
					jp.setVisible(false);
					JLabel a = (JLabel) e.getComponent();
					a.setText(a.getText().replace("- ", "+ "));
				} else {
					JLabel a = (JLabel) e.getComponent();
					a.setText(a.getText().replace("+ ", "- "));
					jp.setVisible(true);
				}
			}
		};
		return mAdap;
	}

	private JPanel loadClassificationPropPanel() {
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		for (int i = 0; i < cd.getCount(); i++) {
			if (toHide.contains(i)) {
				continue;
			}
			JPanel p1 = new JPanel();

			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder tb = BorderFactory.createTitledBorder(loweredetched, cd.getName(i));
			p1.setBorder(tb);

			ClassificationStatistics s = cd.getStatistics(i);
			int cSize = s.categories().size();
			JPanel panel = new JPanel(new GridBagLayout());
			Border blackline = BorderFactory.createLineBorder(Color.black);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridheight = 2;
			gbc.gridwidth = 2;
			gbc.gridx = 0;
			gbc.gridy = 0;
			panel.add(label(""), gbc);
			gbc.gridheight = 1;
			gbc.gridwidth = cSize;
			gbc.gridx = 2;
			panel.add(label("Predicted class"), gbc);
			gbc.gridx = cSize + 2;
			gbc.gridwidth = 3;
			panel.add(label("Classification parameterrs"), gbc);
			//
			gbc.gridwidth = 1;
			gbc.gridx = 2;
			gbc.gridy = 1;

			for (int j = 0; j < cSize; j++) {
				gbc.gridx = 2 + j;
				panel.add(label(s.categories().get(j)), gbc);
			}

			gbc.gridx = 2 + cSize;
			panel.add(label("Total"), gbc);
			gbc.gridx = 3 + cSize;
			panel.add(label("Sensitivity"), gbc);
			gbc.gridx = 4 + cSize;
			panel.add(label("Specificity"), gbc);
			//
			gbc.gridy = 2;
			gbc.gridx = 0;
			gbc.gridheight = cSize;
			panel.add(label("Actual Class"), gbc);
			gbc.gridheight = 1;

			for (int j = 0; j < cSize; j++) {
				gbc.gridx = 1;
				gbc.gridy = 2 + j;
				panel.add(label(s.categories().get(j)), gbc);
				for (int k = 0; k < cSize; k++) {
					gbc.gridx = 2 + k;
					panel.add(label(Integer.toString(s.confusionMatrix(j, k))), gbc);
				}
				gbc.gridx = 2 + cSize;
				int value = 0;
				for (int n = 0; n < cSize; n++) {
					value += s.confusionMatrix(j, n);
				}

				panel.add(label(Integer.toString(value)), gbc);
				gbc.gridx = 3 + cSize;
				panel.add(label(String.format("%.3f", s.sensitivity(j))), gbc);
				gbc.gridx = 4 + cSize;
				panel.add(label(String.format("%.3f", s.specificity(j))), gbc);
			}
			gbc.gridy = cSize + 2;
			gbc.gridx = 0;
			panel.add(label(""), gbc);
			gbc.gridx = 1;
			panel.add(label("Total"), gbc);
			gbc.gridx = 2;
			for (int j = 0; j < cSize; j++) {
				gbc.gridx = 2 + j;
				int value = 0;
				for (int n = 0; n < cSize; n++) {
					value += s.confusionMatrix(n, j);
				}
				panel.add(label(Integer.toString(value)), gbc);
			}
			gbc.gridx = 2 + cSize;
			gbc.gridwidth = 3;
			panel.add(label(""), gbc);

			panel.setBorder(blackline);
			p1.add(panel);
			container.add(p1);
		}
		return container;
	}

	public JLabel label(String s) {
		JLabel p = new JLabel(s);
		Border paddingBorder = BorderFactory.createEmptyBorder(6, 6, 6, 6);
		Border border = BorderFactory.createLineBorder(Color.GRAY);
		p.setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
		return p;
	}

	public JPanel loadClassificationStatPanel(Model m) {
		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(5000, 500));
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.7;
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("name"), c);
		c.weightx = 0.3;
		c.gridx = 1;
		panel.add(new JLabel("type"), c);
		c.gridx = 2;
		panel.add(new JLabel("n"), c);
		c.gridx = 3;
		panel.add(new JLabel("Accuracy"), c);

		Collection<Prediction> dep = this.c.getQdb().getPredictionRegistry().getByModel(m);
		Object[] p = dep.toArray();
		for (int i = 0; i < dep.size(); i++) {
			ClassificationStatistics cs = (ClassificationStatistics) StatisticsUtil.evaluate(m, (Prediction) p[i]);
			String id = ((Prediction) p[i]).getId();
			String type = ((Prediction) p[i]).getType().name();
			int N = cs.size();
			double Accuracy = cs.accuracy();

			final int fin = i;
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i + 1;
			gbc.weightx = 0.7;
			gbc.anchor = GridBagConstraints.WEST;
			JCheckBox j1;
			panel.add(j1 = new JCheckBox(id, null, true), gbc);
			j1.setToolTipText(type);
			j1.setEnabled(true);
			j1.addItemListener(new ItemListener() {
				int loc = fin;

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == 1) {
						toHide.remove(loc);
						vd.setDataActive(loc);
						redrawClassificationCharts();
					} else {
						toHide.add(loc);
						vd.setDataDeactive(loc);
						redrawClassificationCharts();
					}
				}
			});
			j1.setForeground(colList.get(i));
			gbc.weightx = 0.3;
			gbc.gridx = 1;
			panel.add(new JLabel(type), gbc);
			gbc.gridx = 2;
			panel.add(new JLabel(Integer.toString(N)), gbc);
			gbc.gridx = 3;
			panel.add(new JLabel(String.format("%.3f", Accuracy)), gbc);
		}
		return panel;
	}

	public class ClassificationData {
		private int count;
		private String title;
		private String propertyTitle;
		private LinkedList<ClassificationStatistics> dataList;
		private LinkedList<String> names;

		public ClassificationData(Model m) {
			dataList = new LinkedList<ClassificationStatistics>();
			names = new LinkedList<String>();
			Collection<Prediction> dep = c.getQdb().getPredictionRegistry().getByModel(m);
			for (Prediction p : dep) {
				ClassificationStatistics s = (ClassificationStatistics) StatisticsUtil.evaluate(m, p);
				dataList.add(s);
				names.add(p.getId() + ": " + p.getName());
			}
			count = dep.size();
			title = m.getId().concat(" " + m.getName());
			propertyTitle = m.getProperty().getId() + ": " + m.getProperty().getName();
		}

		public int getCount() {
			return this.count;
		}

		public ClassificationStatistics getStatistics(int i) {
			return this.dataList.get(i);
		}

		public String getName(int i) {
			return this.names.get(i);
		}

		public String getTitle() {
			return this.title;
		}

		public String getPropertyTitle() {
			return this.propertyTitle;
		}
	}
}
