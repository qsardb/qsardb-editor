/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer.chartData;

import java.util.ArrayList;

public class ChartSeries {
	private ArrayList<DataSeries> dSeries;

	public ChartSeries() {
		dSeries = new ArrayList<DataSeries>();
	}
	public DataSeries getDataSeries(int k){
		if(k<dSeries.size())
			return dSeries.get(k);
		else return null;
	}
	public ArrayList<DataSeries> getAsArrayList(){
		return dSeries;
	}
	public int getDataSeriesCount(){
		return dSeries.size();
	}
	public void addDataSeries(DataSeries d){
		this.dSeries.add(d);
	}
}
