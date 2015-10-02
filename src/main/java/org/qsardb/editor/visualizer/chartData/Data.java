/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer.chartData;

import java.util.ArrayList;

public class Data {
	private String name;
	private String id;
	private ArrayList<Object> CompoundId;
	private ArrayList<Double> x;
	private ArrayList<Double> y;
	private int colIndex;
	double rsq;
	double Stdev;
	int size;
	public Data(String name, String id) {
		this.CompoundId = new ArrayList();
		this.x = new ArrayList();
		this.y = new ArrayList();
		this.name = name;
		this.id = id;
	}
	public int getColIndex(){
		return colIndex;
	}
	public void setColIndex(int index){
		this.colIndex = index;
	}
	public void add(Object CompoundId, Double x, Double y){
		this.CompoundId.add(CompoundId);
		this.x.add(x);
		this.y.add(y);
	}
	public int getSize(){
		return x.size();
	}
	public ArrayList<Double> getXArrayList(){
		return x;
	}
	public ArrayList<Double> getYArrayList(){
		return y;
	}
	public Double getX(int i){
		return x.get(i);
	}
	public Double getY(int i){
		return y.get(i);
	}
	public Object getCompoundId(int i){
		return CompoundId.get(i);
	}
	public String getId(){
		return id;
	}
	public String getName(){
		return name;
	}
}
