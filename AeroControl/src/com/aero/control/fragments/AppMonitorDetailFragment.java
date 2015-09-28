package com.aero.control.fragments;

import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.Material.CardBox;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.PerApp.AppMonitor.AppModule;
import com.aero.control.helpers.PerApp.AppMonitor.JobManager;
import com.aero.control.helpers.PerApp.AppMonitor.model.AppElement;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.cubic.CubicEaseOut;
import com.db.chart.view.animation.style.DashAnimation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 09.03.14.
 */
public class AppMonitorDetailFragment extends Fragment {

    private ViewGroup mRoot;
    private static LineChartView mLineChart;
    private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
    private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();
    private Paint mLineGridPaint;
    private String mAppName = null;
    private int mMaxValue = 0;
    private int mModule = 10;
    private TextView mHeader, mLineTooltip, mAverage, mModuleName;
    private List<CardBox> mCards;
    private int mPositionModule = 0;

    private final OnEntryClickListener lineEntryListener = new OnEntryClickListener(){
        @Override
        public void onClick(int setIndex, int entryIndex, Rect rect) {

            if(mLineTooltip == null)
                showLineTooltip(entryIndex, rect);
            else
                dismissLineTooltip(entryIndex, rect);
        }
    };

    private final View.OnClickListener lineClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(mLineTooltip != null)
                dismissLineTooltip(-1, null);
        }
    };

    private final CardBox.OnClickListener mCardListener = new CardBox.OnClickListener() {
        @Override
        public void onClick(View v) {

            int i = 0;
            CardBox cb = (CardBox)v;

            for (AppModule module : AeroActivity.mJobManager.getModules()) {
                if (module.getPrefix().equals(cb.getTitle())) {
                    mModule = module.getIdentifier();
                    mPositionModule = i;
                }
                i++;
            }

            clearUI();
            loadUI();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoot = (ViewGroup) inflater.inflate(R.layout.appmonitor_detail, null);
        this.mAppName = null;

        mHeader = (TextView) mRoot.findViewById(R.id.usageTimer);
        mAverage = (TextView) mRoot.findViewById(R.id.topValue);
        mModuleName = (TextView) mRoot.findViewById(R.id.topModuleName);
        mCards = new ArrayList<CardBox>();

        LinearLayout layoutHolder = (LinearLayout) mRoot.findViewById(R.id.layouthorizontal);

        for (AppModule module : AeroActivity.mJobManager.getModules()) {
            CardBox cardbox = new CardBox(getActivity());
            cardbox.setOnClickListener(mCardListener);
            mCards.add(cardbox);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 15, 0);


        // Add the views to our LinearLayout;
        for (CardBox c : mCards) {
            layoutHolder.addView(c, layoutParams);
        }

        clearUI();
        loadUI();

        return mRoot;
    }

    public final void setTitle(String title) {
        ((AeroActivity)getActivity()).setActionBarTitle(title);
    }

    private void clearUI() {
        this.mMaxValue = 0;
        this.mRoot.invalidate();

        // Turn everything normal;
        for (CardBox b : mCards) {
            b.setBackground(R.drawable.card);
        }

        // Select the one the user wanted to select;
        mCards.get(mPositionModule).setBackground(R.drawable.card_clicked);

        if (mLineTooltip != null) {
            mLineChart.dismissTooltip(mLineTooltip);
            this.mLineTooltip = null;
        }
        if (mLineChart != null)
            mLineChart.reset();
    }

    private void loadUI() {

        int i = 0;

        if (AeroActivity.mJobManager == null) {
            // Generate a new instance;
            AeroActivity.mJobManager = JobManager.instance(getActivity());
        }

        AeroActivity.mJobManager.wakeUp();

        AppElement data = null;
        String suffix = "";

        if (mAppName == null)
            data = getActivity().getIntent().getExtras().getParcelable("aero_data");
        else {
            final List<AppElement> AppData = AeroActivity.mJobManager.getParentChildData(getActivity());

            for (AppElement a : AppData) {
                if (a.getName().equals(mAppName))
                    data = a;
            }
        }

        if (data != null) {
            setTitle(data.getRealName());
            mAppName = data.getName();
        }

        // Set up our small adapter for all loaded modules;
        for (AppModule module : AeroActivity.mJobManager.getModules()) {
            if (mModule == module.getIdentifier()) {
                suffix = module.getSuffix();
                mAverage.setText(data.getChildData().get((i + 1)).getContent());
                mModuleName.setText(data.getChildData().get((i + 1)).getTitle());
            }

            //mCards.get(i).setContent(data.getChilData().get((i + 1)).getContent());
            mCards.get(i).setContent(module.getDrawable());
            mCards.get(i).setTitle(data.getChildData().get((i + 1)).getTitle());

            i++;
        }

        loadGraph(data, suffix);

        mHeader.setText(data.getChildData().get(0).getTitle() + " " + getText(R.string.usage_time));

        mHeader.setTypeface(FilePath.kitkatFont);
    }

    private void loadGraph(final AppElement data, final String suffix) {

        mLineChart = (LineChartView) mRoot.findViewById(R.id.graph);
        mLineChart.setOnEntryClickListener(lineEntryListener);
        mLineChart.setOnClickListener(lineClickListener);

        mLineGridPaint = new Paint();
        mLineGridPaint.setColor(this.getResources().getColor(R.color.grey));
        mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        mLineGridPaint.setStyle(Paint.Style.STROKE);
        mLineGridPaint.setAntiAlias(true);
        mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

        mLineChart.reset();

        loadLine(data);

        mLineChart.setBorderSpacing(Tools.fromDpToPx(10))
                .setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(0, mMaxValue, calculateSteps(0, mMaxValue))
                .setLabelsFormat(new DecimalFormat("##" + suffix))
                .show(new Animation()
                        .setAlpha(-1)
                        .setEasing(new CubicEaseOut())
                        .setOverlap(.5f)
                        .setDuration(750)
                        .setStartPoint(.5f, .5f))
        ;

        mLineChart.animateSet(1, new DashAnimation());
    }

    private void loadLine(final AppElement data) {

        final List<Integer> rawData = AeroActivity.mJobManager.getRawData(data.getName(), mModule);

        if (rawData == null) {
            return;
        }

        final int size = rawData.size();
        final int chunksize = 10;
        int position = 0;
        final int chunks = size / chunksize;
        int rest = size % chunksize;
        int realPart;
        int average = 0;
        final LineSet dataSet = new LineSet();

        // Reset the max value;
        mMaxValue = 0;

        // Find the highest value;
        for (Integer j : rawData) {
            if (j > mMaxValue)
                mMaxValue = j;
        }

        for (int i = 0; i < size; i += realPart) {

            int counter = 0;

            if (rest > 0) {
                rest--;
                realPart = chunks + 1;
            } else {
                realPart = chunks;
            }

            for (int n = i; n < (i + realPart); n++) {
                average += rawData.get(n);
                counter++;
            }
            average = average / Math.max(counter, 1);

            dataSet.addPoint(position + "", average);
            dataSet.setDots(true)
                    .setDotsColor(this.getResources().getColor(R.color.material_orange))
                    .setDotsRadius(Tools.fromDpToPx(5))
                    .setDotsStrokeThickness(Tools.fromDpToPx(2))
                    .setDotsStrokeColor(this.getResources().getColor(R.color.white))
                    .setLineColor(this.getResources().getColor(R.color.material_orange))
                    .setLineThickness(Tools.fromDpToPx(3))
                    .setSmooth(true);
            position++;
            average = 0;
        }
        mLineChart.addData(dataSet);
    }

    private int calculateSteps(final int minValue, final int maxValue) {

        int range = maxValue - minValue;

        return (int)Math.max(Math.ceil(range / 5), 1);
    }

    private void showLineTooltip(int entryIndex, Rect rect){

        mLineTooltip = (TextView) getActivity().getLayoutInflater().inflate(R.layout.circular_tooltip, null);
        mLineTooltip.setText((int)mLineChart.getData().get(0).getEntry(entryIndex).getValue() + "");

        LayoutParams layoutParams = new LayoutParams((int)Tools.fromDpToPx(64), (int)Tools.fromDpToPx(64));
        layoutParams.leftMargin = rect.centerX() - layoutParams.width/2;
        layoutParams.topMargin = rect.centerY() - layoutParams.height/2;
        mLineTooltip.setLayoutParams(layoutParams);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){
            mLineTooltip.setPivotX(layoutParams.width/2);
            mLineTooltip.setPivotY(layoutParams.height/2);
            mLineTooltip.setAlpha(0);
            mLineTooltip.setScaleX(0);
            mLineTooltip.setScaleY(0);
            mLineTooltip.animate()
                    .setDuration(150)
                    .alpha(1)
                    .scaleX(1).scaleY(1)
                    .rotation(360)
                    .setInterpolator(enterInterpolator);
        }

        mLineChart.showTooltip(mLineTooltip);
    }

    private void dismissLineTooltip(final int entryIndex, final Rect rect){

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            mLineTooltip.animate()
                    .setDuration(100)
                    .scaleX(0).scaleY(0)
                    .alpha(0)
                    .setInterpolator(exitInterpolator).withEndAction(new Runnable(){
                @Override
                public void run() {
                    mLineChart.removeView(mLineTooltip);
                    mLineTooltip = null;
                    if(entryIndex != -1)
                        showLineTooltip(entryIndex, rect);
                }
            });
        } else {
            mLineChart.dismissTooltip(mLineTooltip);
            mLineTooltip = null;
            if(entryIndex != -1)
                showLineTooltip(entryIndex, rect);
        }
    }

}