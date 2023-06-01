/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer.chartData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.visualizer.VisualizerTab;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.EvaluatorFactory;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;

public class DataCollector {
	private Map<String, String> propertyMap;
	private String propertyId;
	ChartSeries resErrorChartSeries;
	private ChartSeries propertyChartSeries;
	private ChartSeries descriptorChartSeries;
	private List<Prediction> predicts;
	private List<Descriptor> descriptors = new ArrayList<>();

	public ChartSeries getPropertyChartSeries() {
		return propertyChartSeries;
	}

	public ChartSeries getResErrorChartSeries() {
		return resErrorChartSeries;
	}

	public ChartSeries getDescriptorChartSeries() {
		return descriptorChartSeries;
	}

	public DataCollector() {
		predicts = new ArrayList<Prediction>();
		descriptors = new ArrayList<Descriptor>();
		propertyMap = null;
	}

	public void loadData(Model m, QdbContext c) {
		descriptorChartSeries = new ChartSeries();
		propertyChartSeries = new ChartSeries();
		resErrorChartSeries = null;

		propertyId = m.getProperty().getId().concat(" " + m.getProperty().getName());
		Collection<Prediction> dep = c.getQdb().getPredictionRegistry().getByModel(m);
		predicts.clear();
		for (Prediction p2 : dep) {
			if (!p2.getType().equals(Prediction.Type.TESTING)) {
				predicts.add(p2);
			}
		}

		Evaluator eval = null;
		try {
			eval = EvaluatorFactory.getInstance().getEvaluator(m);
			eval.init();
			descriptors = eval.getDescriptors();
		} catch (Exception ex) {
			Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, "Can't find descriptors for model: "+m.getId());
		} finally {
			if (eval != null) {
				try {
					eval.destroy();
				} catch (Exception ex) {
					Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		ValuesCargo vcProp = m.getProperty().getCargo(ValuesCargo.class);
		try {
			propertyMap = vcProp.loadStringMap();
		} catch (IOException ex) {
			Utils.showExceptionPanel(ex.getMessage(), ex);
			Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
		}

		loadPropertyAnalysisData(m);
		loadResErrorData();
		loadDescriptorAnalysisData(m);
	}

	public void loadPropertyAnalysisData(Model m) {

		propertyChartSeries = new ChartSeries();
		Data data = null;
		DataSeries propertySeries = new DataSeries(propertyId);
		int ind = 0;
		for (Prediction p : predicts) {
			if (p.getType().equals(Prediction.Type.TESTING)) {
				continue;
			}
			data = new Data(p.getName(), p.getId());

			Map<String, String> predictionMap = null;
			if (!p.hasCargo(ValuesCargo.class) || p.getType().equals(Prediction.Type.TESTING)) {
				continue;
			}
			ValuesCargo vcPred = p.getCargo(ValuesCargo.class);
			try {
				predictionMap = vcPred.loadStringMap();
			} catch (IOException ex) {
				Utils.showExceptionPanel(ex.getMessage(), ex);
				Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
			}
			for (Object key : propertyMap.keySet()) {
				if (predictionMap.containsKey(key)) {
					double propValue;
					double predValue;
					try {
						propValue = Double.parseDouble(propertyMap.get(key));
						predValue = Double.parseDouble(predictionMap.get(key));
					} catch (NumberFormatException nfe) {
						continue;
					} catch (NullPointerException npe) {
						continue;
					}
					data.add(key, propValue, predValue);
				}
			}
			data.setColIndex(ind);
			ind++;
			propertySeries.addData(data);
		}
		if (propertySeries.getSeriesCount() > 0) {
			propertyChartSeries.addDataSeries(propertySeries);
		}
	}

	public void loadResErrorData() {
		resErrorChartSeries = new ChartSeries();
		if (propertyMap.isEmpty() || propertyChartSeries.getDataSeriesCount() == 0) {
			return;
		}
		resErrorChartSeries = new ChartSeries();
		int ind = 0;
		DataSeries resError = new DataSeries("resErrorDataSeries");
		DataSeries dseries = propertyChartSeries.getDataSeries(0);
		for (Data d : dseries.getAsArrayList()) {
			Data data = new Data(d.getName(), d.getId());
			for (int i = 0; i < d.getSize(); i++) {
				data.add(d.getCompoundId(i), d.getX(i), d.getY(i) - d.getX(i));
			}
			data.setColIndex(ind);
			resError.addData(data);
			ind++;
		}
		resErrorChartSeries.addDataSeries(resError);
	}

	public void loadDescriptorAnalysisData(Model m) {
		DataSeries descriptorSeries = null;

		for (Descriptor descriptor : descriptors) {
			int ind = 0;
			descriptorSeries = new DataSeries(descriptor.getId());
			Map<String, String> descriptorMap = null;
			ValuesCargo vcPred = descriptor.getCargo(ValuesCargo.class);
			try {
				descriptorMap = vcPred.loadStringMap();
			} catch (IOException ex) {
				Utils.showExceptionPanel(ex.getMessage(), ex);
				Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
			}
			for (Prediction p : predicts) {
				if (p.getType().equals(Prediction.Type.TESTING) || !p.hasCargo(ValuesCargo.class)) {
					continue;
				}
				Data data = new Data(descriptor.getName(), descriptor.getId() + p.getId());
				Map<String, String> predictionMap = null;
				vcPred = p.getCargo(ValuesCargo.class);
				try {
					predictionMap = vcPred.loadStringMap();
				} catch (IOException ex) {
					Utils.showExceptionPanel(ex.getMessage(), ex);
					Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
				}
				for (Object key : descriptorMap.keySet()) {
					if (propertyMap.containsKey(key)) {
						if (!predictionMap.containsKey(key)) {
							continue;
						}
						double descValue;
						double propValue;
						try {
							descValue = Double.parseDouble(descriptorMap.get(key));
							propValue = Double.parseDouble(propertyMap.get(key));
						} catch (NumberFormatException nfe) {
							continue;
						} catch (NullPointerException npe) {
							continue;
						}
						data.add(key, descValue, propValue);
					}
				}
				data.setColIndex(ind);
				descriptorSeries.addData(data);
				ind++;
			}
			descriptorChartSeries.addDataSeries(descriptorSeries);

		}
	}

	public void loadDescriptorAnalysisDataClassification(Model m) {
		descriptorChartSeries = new ChartSeries();
		Collection<Prediction> dep = m.getQdb().getPredictionRegistry().getByModel(m);
		Map<String, String> propertyStringMap = null;
		ValuesCargo vcProp = m.getProperty().getCargo(ValuesCargo.class);
		List<Descriptor> descriptors = null;
		Evaluator eval = null;
		try {
			eval = EvaluatorFactory.getInstance().getEvaluator(m);
			eval.init();
			descriptors = eval.getDescriptors();
		} catch (Exception ex) {
			Utils.showExceptionPanel(ex.getMessage(), ex);
			Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			if (eval != null) {
				try {
					eval.destroy();
				} catch (Exception ex) {
					Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		try {
			propertyStringMap = vcProp.loadStringMap();
		} catch (IOException ex) {
			Utils.showExceptionPanel(ex.getMessage(), ex);
			Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
		}

		DataSeries descriptorSeries = null;

		for (Descriptor descriptor : descriptors) {
			int ind = 0;
			descriptorSeries = new DataSeries(descriptor.getId());
			Map<String, String> descriptorMap = null;
			ValuesCargo vcPred = descriptor.getCargo(ValuesCargo.class);
			try {
				descriptorMap = vcPred.loadStringMap();
			} catch (IOException ex) {
				Utils.showExceptionPanel(ex.getMessage(), ex);
				Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
			}
			for (Prediction p : dep) {
				if (p.getType().equals(Prediction.Type.TESTING)) {
					continue;
				}
				Data data = new Data(descriptor.getName(), descriptor.getId() + p.getId());
				Map<String, String> predictionMap = null;
				vcPred = p.getCargo(ValuesCargo.class);
				try {
					predictionMap = vcPred.loadStringMap();
				} catch (IOException ex) {
					Utils.showExceptionPanel(ex.getMessage(), ex);
					Logger.getLogger(VisualizerTab.class.getName()).log(Level.SEVERE, null, ex);
				}
				for (Object key : descriptorMap.keySet()) {
					if (propertyStringMap.containsKey(key)) {
						if (!predictionMap.containsKey(key)) {
							continue;
						}
						double descValue;
						try {
							descValue = Double.parseDouble(descriptorMap.get(key));
						} catch (NumberFormatException nfe) {
							continue;
						} catch (NullPointerException npe) {
							continue;
						}
						data.add(key, descValue, null);
					}
				}
				data.setColIndex(ind);
				descriptorSeries.addData(data);
				ind++;
			}
			descriptorChartSeries.addDataSeries(descriptorSeries);
		}
	}

	public void setDataActive(int k) {
		if (propertyChartSeries != null && propertyChartSeries.getDataSeriesCount() >= 1) {
			propertyChartSeries.getDataSeries(0).getSeries(k).setColIndex(k);
		}
		if (resErrorChartSeries != null && resErrorChartSeries.getDataSeriesCount() >= 1) {
			resErrorChartSeries.getDataSeries(0).getSeries(k).setColIndex(k);
		}
		for (DataSeries d : descriptorChartSeries.getAsArrayList()) {
			d.getSeries(k).setColIndex(k);
		}
	}

	public void setDataDeactive(int k) {
		if (propertyChartSeries != null && propertyChartSeries.getDataSeriesCount() >= 1) {
			propertyChartSeries.getDataSeries(0).getSeries(k).setColIndex(-1);
		}
		if (resErrorChartSeries != null && resErrorChartSeries.getDataSeriesCount() >= 1) {
			resErrorChartSeries.getDataSeries(0).getSeries(k).setColIndex(-1);
		}
		for (DataSeries d : descriptorChartSeries.getAsArrayList()) {
			d.getSeries(k).setColIndex(-1);
		}
	}
}
