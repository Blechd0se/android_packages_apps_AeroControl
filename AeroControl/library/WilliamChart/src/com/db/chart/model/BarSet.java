/*
 * Copyright 2014 Diogo Bernardino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.db.chart.model;

import android.util.Log;

/**
 * Data model containing a set of {@link Bar} to be used by {@link com.db.chart.view.BaseBarChartView}.
 */
public class BarSet extends ChartSet{
	
	
	private static final String TAG = "com.db.chart.model.BarSet";
	
	
	public BarSet(){
		super();
	}
	
	
	
	public void addBar(String label, float value){
		this.addBar(new Bar(label, value));
	}
	
	
	public void addBar(Bar point){
		this.addEntry(point);
	}
	
	
	public void addBars(String[] labels, float[] values){
		
		if(labels.length != values.length)
			Log.e(TAG, "Arrays size doesn't match.", new IllegalArgumentException());

		int nEntries = labels.length;
		for(int i = 0; i < nEntries; i++)
			addBar(labels[i], values[i]);
	}
	
	
	
	/*
	 * --------
	 * Getters
	 * --------
	 */
	
	public int getColor(){
		return ((Bar) this.getEntry(0)).getColor();
	}

	
	
	/*
	 * -------------
	 * Setters
	 * -------------
	 */
	
	public BarSet setColor(int color){
		
		for(ChartEntry e : getEntries())
			((Bar) e).setColor(color);
		return this;
	}
	
}
