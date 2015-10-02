/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer.chartData;

import java.util.ArrayList;

public class DataSeries {
	private String name;
	private ArrayList<Data> dataSeries;

	public void addData(Data data){
		dataSeries.add(data);
	}
	public int getSeriesCount(){
		return dataSeries.size();
	}
	public Data getSeries(int k){
		if(dataSeries.size()>k)
			return dataSeries.get(k);
		else return null;
	}
	public String getName(){
		return name;
	}
	public ArrayList<Data> getAsArrayList(){
		return dataSeries;
	}
	public DataSeries(String name){
		this.name = name;
		this.dataSeries = new ArrayList<Data>();
	}
}
