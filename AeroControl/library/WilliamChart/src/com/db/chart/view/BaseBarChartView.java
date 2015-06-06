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

package com.db.chart.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.db.chart.model.ChartSet;
import com.db.williamchart.R;



/**
 * Implements a bar chart extending {@link com.db.chart.view.ChartView}
 */
public abstract class BaseBarChartView extends ChartView {


	/** Offset to control bar positions. Added due to multiset charts. */
	protected float drawingOffset;


	/** Style applied to Graph */
	protected Style style;


	/** Bar width */
	protected float barWidth;



	public BaseBarChartView(Context context, AttributeSet attrs) {
		super(context, attrs);

		style = new Style(context.getTheme()
				.obtainStyledAttributes(attrs, R.styleable.ChartAttrs, 0, 0));
	}


	public BaseBarChartView(Context context) {
		super(context);

		style = new Style();
	}
	
	
	
	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		style.init();
	}
	
	
	@Override
	public void onDetachedFromWindow(){
		super.onDetachedFromWindow();
		style.clean();
	}



    /**
     * Method responsible to draw bars with the parsed screen points.
     *
     * @param canvas   The canvas to draw on.
     * @param data   {@link java.util.ArrayList} of {@link com.db.chart.model.ChartSet}
     *             to use while drawing the Chart
     */
	@Override
	protected void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {}



    /**
     * Draws a bar (a chart bar btw :)).
     *
     * @param canvas {@link android.graphics.Canvas} used to draw the background
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    protected void drawBar(Canvas canvas, float left, float top, float right, float bottom) {

        canvas.drawRoundRect(new RectF((int) left, (int) top, (int) right, (int) bottom),
                style.cornerRadius, style.cornerRadius,
                style.barPaint);
    }



    /**
     * Draws the background (not the fill) of a bar, the one behind the bar.
     *
     * @param canvas {@link android.graphics.Canvas} used to draw the background
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    protected void drawBarBackground(Canvas canvas, float left, float top, float right, float bottom) {

        canvas.drawRoundRect(new RectF(left, top, right, bottom),
                style.cornerRadius, style.cornerRadius,
                style.barBackgroundPaint);
    }



    /**
     * Calculates Bar width based on the distance of two horizontal labels.
     *
     * @param nSets  Number of sets
     * @param x0     Coordinate(n)
     * @param x1     Coordinate(n+1)
     */
	protected void calculateBarsWidth(int nSets, float x0, float x1) {
		barWidth = ((x1 - x0) - style.barSpacing/2 - style.setSpacing * (nSets - 1)) / nSets;
	}

	

	/**
	 * Having calculated previously the barWidth gives the offset to know 
	 * where to start drawing the first bar of each group.
     *
	 * @param size   Size of sets
	 */
	protected void calculatePositionOffset(int size){
		
		if(size % 2 == 0)
			drawingOffset = size * barWidth/2 + (size - 1) * (style.setSpacing / 2);
		else
			drawingOffset = size * barWidth/2 + ((size - 1) / 2) * style.setSpacing;
	}
	
	
	
	
    /*
	 * --------
	 * Setters
	 * --------
	 */
	
	
	/**
	 * Define the space to use between bars.
     *
	 * @param spacing   Spacing between {@link com.db.chart.model.Bar}
	 */
	public void setBarSpacing(float spacing){
		style.barSpacing = spacing;
	}
	
	
	/**
	 * When multiset, it defines the space to use set.
     *
	 * @param spacing   Spacing between {@link com.db.chart.model.BarSet}
	 */
	public void setSetSpacing(float spacing){
		style.setSpacing = spacing;
	}
	
	
	/**
	 * Background in bars place.
     *
	 * @param bool   True in case {@link com.db.chart.model.Bar} must display a background
	 */
	public void setBarBackground(boolean bool){
		style.hasBarBackground = bool;
	}
	
	
	/**
	 * Color to use in bars background.
     *
	 * @param color   Color of background in case setBarBackground has been set to True
	 */
	public void setBarBackgroundColor(int color){
		style.mBarBackgroundColor = color;
	}
	
	
	/**
	 * Round corners of bars.
     *
	 * @param radius   Radius applied to the corners of {@link com.db.chart.model.Bar}
	 */
	public void setRoundCorners(float radius){
		style.cornerRadius = radius;
	}
	
	


	/*
	 * ----------
	 *    Style
	 * ----------
	 */

	public class Style{
		
		
		private static final int DEFAULT_COLOR = -16777216;
		
		
		/** Bars fill variables */
		protected Paint barPaint;
		
		
		/** Spacing between bars */
		protected float barSpacing;
		protected float setSpacing;


        /** Bar background variables */
        protected Paint barBackgroundPaint;
        private int mBarBackgroundColor;
        protected boolean hasBarBackground;


		/** Radius to round corners **/
		protected float cornerRadius;
		
		
		/** Shadow related variables */
		private final float mShadowRadius;
		private final float mShadowDx;
		private final float mShadowDy;
		private final int mShadowColor;
		
		
		/** Shadow color */
		private int mAlpha;
		private int mRed;
		private int mBlue;
		private int mGreen;


		
	    protected Style() {
	    	
	    	mBarBackgroundColor = DEFAULT_COLOR;
	    	hasBarBackground = false;
	    	
	    	barSpacing = getResources().getDimension(R.dimen.bar_spacing);
	    	setSpacing = getResources().getDimension(R.dimen.set_spacing);
	    	
	    	mShadowRadius = 0;
	    	mShadowDx = 0;
	    	mShadowDy = 0;
	    	mShadowColor = DEFAULT_COLOR;
	    }
	    
	    
	    protected Style(TypedArray attrs) {
	    	
	    	mBarBackgroundColor = DEFAULT_COLOR;
	    	hasBarBackground = false;
	    	
	    	barSpacing = attrs.getDimension(
	    			R.styleable.BarChartAttrs_chart_barSpacing, 
	    				getResources().getDimension(R.dimen.bar_spacing));
	    	setSpacing = attrs.getDimension(
	    			R.styleable.BarChartAttrs_chart_barSpacing, 
	    				getResources().getDimension(R.dimen.set_spacing));
	    	
	    	mShadowRadius = attrs.getDimension(
	    			R.styleable.ChartAttrs_chart_shadowRadius, 0);
	    	mShadowDx = attrs.getDimension(
	    			R.styleable.ChartAttrs_chart_shadowDx, 0);
	    	mShadowDy = attrs.getDimension(
	    			R.styleable.ChartAttrs_chart_shadowDy, 0);
	    	mShadowColor = attrs.getColor(
	    			R.styleable.ChartAttrs_chart_shadowColor, 0);
	    }	
	    
	    
	    
		private void init(){
	    	
			mAlpha = Color.alpha(mShadowColor);
			mRed = Color.red(mShadowColor);
			mBlue = Color.blue(mShadowColor);
			mGreen = Color.green(mShadowColor);
			
			
	    	barPaint = new Paint();
	    	barPaint.setStyle(Paint.Style.FILL);
	    	barPaint.setShadowLayer(mShadowRadius, mShadowDx, 
	    								mShadowDy, mShadowColor);
	    	
	    	barBackgroundPaint = new Paint();
	    	barBackgroundPaint.setColor(mBarBackgroundColor);
	    	barBackgroundPaint.setStyle(Paint.Style.FILL);
	    }

		
	    private void clean(){
	    	
	    	barPaint = null;
	    	barBackgroundPaint = null;
	    }


        /**
         * Applies an alpha to the paint object.
         *
         * @param paint   {@link android.graphics.Paint} object to apply alpha.
         * @param alpha   Alpha value (opacity).
         */
        protected void applyAlpha(Paint paint, float alpha){

            paint.setAlpha((int)(alpha * 255));
            paint.setShadowLayer( style.mShadowRadius, style.mShadowDx, style.mShadowDy,
                    Color.argb(((int)(alpha * 255) < style.mAlpha)
                                    ? (int)(alpha * 255)
                                    : style.mAlpha,
                            style.mRed,
                            style.mGreen,
                            style.mBlue));
        }

	}

}