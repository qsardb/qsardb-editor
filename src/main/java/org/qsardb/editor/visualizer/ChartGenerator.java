/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer;

import org.qsardb.editor.visualizer.chartData.Data;
import org.qsardb.editor.visualizer.chartData.DataSeries;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Compound;

public class ChartGenerator {
	protected QdbContext c;
	protected XYSeriesCollection dataset;

	public ChartGenerator(String title, QdbContext context) {
		this.c = context;
	}

	public JPanel getGraph(DataSeries series, String x, String y) {
		dataset = new XYSeriesCollection();
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (int i = 0; i < series.getSeriesCount(); i++) {
			if (series.getSeries(i).getColIndex() != -1) {
				colors.add(series.getSeries(i).getColIndex());
			}
		}
		dataset = getxySeries(series);
		JFreeChart chart = ChartFactory.createScatterPlot("", x, y, dataset);
		chart.removeLegend();
		XYPlot plotCh1 = chart.getXYPlot();
		plotCh1.setBackgroundPaint(Color.white);
		plotCh1.setRangeGridlinePaint(Color.black);
		plotCh1.setDomainGridlinePaint(Color.black);

		AbstractXYItemRenderer renderer = new XYLineAndShapeRenderer(false, true) {
			@Override
			protected void addEntity(EntityCollection entities, Shape area,
					final XYDataset dataset, final int series, final int item, double entityX, double entityY) {
				if (!getItemCreateEntity(series, item)) {
					return;
				}

				if (area == null) {
					area = new Ellipse2D.Double(entityX - getDefaultEntityRadius(),
							entityY - getDefaultEntityRadius(),
							getDefaultEntityRadius() * 7, getDefaultEntityRadius() * 7);
				}

				final XYToolTipGenerator generator = getToolTipGenerator(series, item);
				XYItemEntity entity = new XYItemEntity(area, dataset, series, item, null, null) {
					public @Override
						String getToolTipText() {
							return generator.generateToolTip(dataset, series, item);
						}
				};
				entities.add(entity);
			}
		};
		plotCh1.setRenderer(renderer);

		Shape cross = new Ellipse2D.Double(-2.5, -2.5, 5, 5);
		for (int k = 0; k < dataset.getSeriesCount(); k++) {
			renderer.setSeriesShape(k, cross);
			renderer.setSeriesPaint(k, VisualizerView.colList.get(colors.get(k)));
		}
		renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset xyd, int i, int i1) {
				double a = xyd.getX(i, i1).doubleValue();
				double b = xyd.getY(i, i1).doubleValue();
				XYDataItemx aks = (XYDataItemx) dataset.getSeries(i).getDataItem(i1);
				String temp = (String) aks.getId();
				Compound comp = c.getQdb().getCompound(temp);
				paint(comp.getInChI(), comp.getId());

				String file = System.getProperty("user.dir").concat("//resources");
				File f = new File(file, comp.getId().concat(".png"));
				String compName = comp.getName();

				String namehtml = "<p> Compound Name: ";

				if (compName.length() > 31) {
					namehtml = namehtml + compName.substring(0, 31) + "</p>";
				} else {
					namehtml = namehtml + compName + "</p>";
				}
				return "<html>" + "<img src=\"file:" + f.toString() + "\"><br>"
					+ "[InChI]<br>"
					+ "(" + a + " " + b + ")<br>"
					+ "Compound ID: " + aks.getId() + "<br>"
					+ namehtml + "</html>";
			}
		});

		Font font3 = new Font("Dialog", Font.PLAIN, 14);
		plotCh1.getDomainAxis().setLabelFont(font3);
		plotCh1.getRangeAxis().setLabelFont(font3);

		JPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new java.awt.Dimension(200, 300));
		((ChartPanel) panel).setInitialDelay(0);
		return panel;
	}

	public void paint(String inchi, String name) {
		String folder = System.getProperty("user.dir").concat("//resources");
		name = name.concat(".png");

		CompoundVisualizer cv = new CompoundVisualizer();
		if (inchi != null) {
			BufferedImage lab = (BufferedImage) cv.drawInchiMolecule(inchi);

			File f2 = new File(folder);
			f2.mkdir();

			File f = new File(folder, name);
			if (f.exists()) {
				return;
			}
			if (lab != null) {
				try {
					ImageIO.write(lab, "png", f);
				} catch (IOException ex) {
					Logger.getLogger(ChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	public JPanel getHistogram(DataSeries series, String x, String y) {
		HistogramDataset dataset = new HistogramDataset();
		ArrayList daList = new ArrayList();
		int N = 0;
		double uBound = 0;
		double lBound = 0;
		double[] v1d;
		v1d = null;
		for (Data d : series.getAsArrayList()) {
			if (d.getColIndex() == -1) {
				continue;
			}
			if (N < d.getSize()) {
				N = d.getSize();
			}
			v1d = new double[d.getSize()];
			for (int i = 0; i < d.getSize(); i++) {
				v1d[i] = d.getX(i);

				if (uBound < v1d[i]) {
					uBound = v1d[i];
				}
				if (lBound > v1d[i]) {
					lBound = v1d[i];
				}
			}
			daList.add(v1d.clone());
		}
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (int i = 0; i < series.getSeriesCount(); i++) {
			if (series.getSeries(i).getColIndex() != -1) {
				colors.add(series.getSeries(i).getColIndex());
			}
		}
		for (Object o : daList) {
			dataset.addSeries(x, (double[]) o, 10, lBound, uBound);
		}
		CategoryTableXYDataset dataset2 = new CategoryTableXYDataset();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			for (int j = 0; j < dataset.getItemCount(i); j++) {
				dataset2.add(dataset.getXValue(i, j), dataset.getYValue(i, j), Integer.toString(i));
			}
		}
		XYPlot plot = new XYPlot(dataset2, new NumberAxis(x), new NumberAxis(y), new StackedXYBarRenderer());
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
		JFreeChart chart = new JFreeChart(plot);

		Font font3 = new Font("Dialog", Font.PLAIN, 14);
		plot.getDomainAxis().setLabelFont(font3);
		plot.getRangeAxis().setLabelFont(font3);
		chart.setBackgroundPaint(Color.WHITE);
		plot.setForegroundAlpha(0.7F);
		chart.removeLegend();
		StackedXYBarRenderer xybarrenderer = (StackedXYBarRenderer) plot.getRenderer();
		xybarrenderer.setShadowVisible(false);
		xybarrenderer.setBarPainter(new StandardXYBarPainter());

		for (int j = 0; j < dataset.getSeriesCount(); j++) {

			xybarrenderer.setSeriesPaint(j, VisualizerView.colList.get(colors.get(j)));
		}

		JPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(200, 300));
		return panel;
	}

	public XYSeriesCollection getxySeries(DataSeries ds) {
		XYSeries xys = null;
		XYSeriesCollection sCollection = new XYSeriesCollection();
		for (Data d : ds.getAsArrayList()) {
			if (d.getColIndex() == -1) {
				continue;
			}
			xys = new XYSeries(d.getId());
			for (int i = 0; i < d.getSize(); i++) {
				xys.add(new XYDataItemx(d.getCompoundId(i), d.getX(i), d.getY(i)));
			}
			sCollection.addSeries(xys);
		}
		return sCollection;
	}

	public class XYDataItemx extends XYDataItem {
		private Object Id;

		public XYDataItemx(Number id, Number x, Number y) {
			super(x, y);
			this.Id = id;
		}

		public XYDataItemx(Object id, Number x, Number y) {
			super(x, y);
			this.Id = id;
		}

		public Object getId() {
			return this.Id;
		}
	}
}
