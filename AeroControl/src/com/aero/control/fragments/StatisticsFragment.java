package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.adapter.StatisticAdapter;
import com.aero.control.adapter.statisticInit;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.espian.showcaseview.ShowcaseView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Christ on 10.01.14.
 * CPU Statistics Fragment
 */
public class StatisticsFragment extends Fragment {
    /*
    TODO: - Take time from other cores in account as well (not critical, governors keep freqs mostly synced)
          - Unify listview and holograph data (?)
     */
    public StatisticsFragment mStatisticsFragment;
    public int mIndex = 0;
    private int mColorIndex = 0;
    public ViewGroup root;
    public String[] data;
    public ListView statisticView;
    public PieGraph pg;
    public TextView txtFreq;
    public TextView txtPercentage;
    public TextView txtTime;
    private double mCompleteTime = 0;
    public ShowcaseView.ConfigOptions mConfigOptions;
    public ShowcaseView mShowCase;
    public static final String FILENAME_STATISTICS = "firstrun_statistics";

    public ArrayList<Long> cpuTime = new ArrayList<Long>();
    public ArrayList<Long> cpuFreq = new ArrayList<Long>();
    public ArrayList<Long> cpuPercentage = new ArrayList<Long>();

    public statisticInit[] mResult = new statisticInit[0];

    public static final String[] color_code = {
            "#1abc9c", /* Turquoise */
            "#FF8800", /* Orange */
            "#2c3e50", /* Midnight Blue */
            "#2980b9", /* Nephritis */
            "#CF000F", /* Monza */
            "#8e44ad", /* Wisteria */
            "#3498db", /* Peter River */
            "#e74c3c", /* Pomegrante */
    };

    public static final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    private final static Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public Fragment newInstance(Context context) {
        mStatisticsFragment = new StatisticsFragment();

        return mStatisticsFragment;
    }

    // Override for custom view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        root = (ViewGroup) inflater.inflate(R.layout.statistics, null);

        // Clear UI:
        clearUI();
        mIndex = 0;

        loadUI(true);

        return root;

    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String a = prefs.getString("app_theme", null);

        if (a == null)
            a = "";

        if (a.equals("red"))
            inflater.inflate(R.menu.statistic_menu, menu);
        else if (a.equals("light"))
            inflater.inflate(R.menu.statistic_menu, menu);
        else if (a.equals("dark"))
            inflater.inflate(R.menu.statistic_menu_light, menu);
        else
            inflater.inflate(R.menu.statistic_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:

                clearUI();
                loadUI(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        // Prepare Showcase;

        // Prepare Showcase;
        mConfigOptions = new ShowcaseView.ConfigOptions();
        mConfigOptions.hideOnClickOutside = false;
        mConfigOptions.shotType = ShowcaseView.TYPE_ONE_SHOT;

        // Set up our file;
        int output = 0;
        final byte[] buffer = new byte[1024];

        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME_STATISTICS);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_statistics_fragment, R.string.showcase_statistics_fragment_sum);
    }

    public void DrawFirstStart(int header, int content) {

        try {
            final FileOutputStream fos = getActivity().openFileOutput(FILENAME_STATISTICS, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        mShowCase = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, R.id.action_refresh, getActivity(), header, content, mConfigOptions);
    }

    /*
     * Will load all data into different arrays. Some error checks are
     * also calculated here. HoloSlices will be added according to found
     * data.
     */
    private final void loadUI(boolean firstView) {

        final ArrayList<String> cpuGraphValues = new ArrayList<String>();
        Long[] cpuFreqArray;
        double a;
        int cpuData = getCpuData();
        mCompleteTime = 0;
        pg = (PieGraph) root.findViewById(R.id.graph);

        // Handle no cpu data found;
        if (cpuData == 0) {
            root.findViewById(R.id.noCpuData).setVisibility(View.VISIBLE);
        } else
            root.findViewById(R.id.noCpuData).setVisibility(View.GONE);

        for (int k = 0; k < cpuData; k++) {
            String b = data[k];
            String[] c = b.split(" ");
            if(k == 0) {
                a = Integer.parseInt(c[0]);
            } else {
                a = Integer.parseInt(c[1]);
            }

            mCompleteTime = mCompleteTime + a;
        }

        for (int i = 0, j = 0; i < cpuData; i++) {

            String b = data[i];
            String[] c = b.split(" ");

            // Color change;
            if (j == 8)
                j = 0;

            /*
             * Handle deepsleep;
             */
            if(i == 0) {
                cpuFreq.add((long)0);
                cpuTime.add((long)Integer.parseInt(c[0]));
            } else {
                cpuFreq.add((long)Integer.parseInt(c[0]));
                cpuTime.add((long)Integer.parseInt(c[1]));

            }

        }

        cpuFreqArray = cpuFreq.toArray(new Long[0]);

        int i = 0;
        int j = 0;

        for(long g: cpuTime) {

            PieSlice slice;
            String frequency, time_in_state;
            int percentage;

            // Color change;
            if (j == 8)
                j = 0;

            if(cpuFreqArray[i] == 0)
                frequency = "DeepSleep";
            else
                frequency = AeroActivity.shell.toMHz(cpuFreqArray[i].toString());

            time_in_state = convertTime(g);
            percentage = (int)Math.round((g / mCompleteTime) * 100);
            // Safe all percentages in our array;
            cpuPercentage.add((long)percentage);

            if (g != 0 && percentage >= 1) {

                slice = new PieSlice();
                cpuGraphValues.add(frequency + " " + time_in_state + " " + percentage + "%");

                slice.setValue(percentage);
                slice.setColor(Color.parseColor(color_code[j]));
                pg.setThickness(30);
                pg.addSlice(slice);

                j++;
            }
            i++;

        }

        // Fill our listview with final values and load TextViews;
        createList(cpuFreq, cpuTime, cpuPercentage);
        if (firstView)
            handleOnClick(cpuGraphValues);

        pg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    handleOnClick(cpuGraphValues);
                    return true;
                }

                return false;
            }
        });

    }

    private final void clearUI() {

        /*
         * Cleanup the whole UI.
         * Notice: PieGraph and data might be cleaned anyway,
         * clearing cpuTime/cpuFreq/cpuPercentage AND mResult
         * is _really_ necessary:
         */

        if(pg != null)
            pg.removeSlices();

        if(data != null)
            data = new String[0];

        if(cpuTime != null)
            cpuTime.clear();

        if(cpuFreq != null)
            cpuFreq.clear();

        if(cpuPercentage != null)
            cpuPercentage.clear();

        if (statisticView != null) {
            mResult = new statisticInit[0];
        }
    }

    public final void handleOnClick(ArrayList<String> list) {

        final String[] valueArray = list.toArray(new String[0]);

        for (String a: valueArray) {

            int arrayLength = valueArray.length;

            if(mIndex == arrayLength) {
                mIndex = 0;
                mColorIndex = 0;
            }

            /*
             * Fix exception;
             */
            if (mColorIndex >= 8)
                mColorIndex = 0;

            String currentRow = valueArray[mIndex];
            String[] tmp = currentRow.split(" ");

            txtFreq = (TextView)root.findViewById(R.id.statisticFreq);
            txtTime = (TextView)root.findViewById(R.id.statisticTime);
            txtPercentage = (TextView)root.findViewById(R.id.statisticPercentage);

            if (tmp[1].contains("MHz")) {
                tmp[0] = tmp[0] + " MHz";
                tmp[1] = tmp[2];
                tmp[2] = tmp[3];
            }
            txtFreq.setText(tmp[0]);
            txtTime.setText(tmp[1]);
            txtPercentage.setText(tmp[2]);

            txtFreq.setTypeface(font);
            txtTime.setTypeface(font);
            txtPercentage.setTypeface(font);

            txtFreq.setTextColor(Color.parseColor(color_code[mColorIndex]));
            txtTime.setTextColor(Color.parseColor(color_code[mColorIndex]));
            txtPercentage.setTextColor(Color.parseColor(color_code[mColorIndex]));
        }
        mColorIndex++;
        mIndex++;
    }

    /*
     * Convert usertime in human readable values;
     */
    public final String convertTime(long msTime) {

        msTime = msTime * 10;

        return String.format("%02dh:%02dm:%02ds",
                TimeUnit.MILLISECONDS.toHours(msTime),
                TimeUnit.MILLISECONDS.toMinutes(msTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(msTime)),
                TimeUnit.MILLISECONDS.toSeconds(msTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(msTime))
        );
    }

    /*
     * Finally creates our list from three array sources
     */

    public final void createList(ArrayList<Long> cpuFreq, ArrayList<Long> cpuTime, ArrayList<Long> cpuPercentage) {

        // Add Complete Uptime;
        cpuFreq.add((long)1);
        cpuTime.add((long)mCompleteTime);
        cpuPercentage.add((long)100);

        // Get Data;
        Long[] freq = cpuFreq.toArray(new Long[0]);
        Long[] time = cpuTime.toArray(new Long[0]);
        Long[] percentage = cpuPercentage.toArray(new Long[0]);

        ArrayDataLoader adl = new ArrayDataLoader();
        adl.loadSingleEntry(freq, time, percentage);

        statisticView = (ListView) root.findViewById(R.id.statisticListView);

        StatisticAdapter adapter = new StatisticAdapter(getActivity(),
                R.layout.statistic_layout, mResult);

        statisticView.setAdapter(adapter);

    }


    public final int getCpuData() {

        File cpu_stats = new File(TIME_IN_STATE_PATH);

        if (!cpu_stats.exists())
            return 0;

        data = AeroActivity.shell.getInfo(TIME_IN_STATE_PATH, true);

        return data.length;
    }

    /*
     * Loads our preloaded data into our listview;
     */
    private final class ArrayDataLoader {

        public final void loadSingleEntry(Long[] freq, Long[] time, Long[] percentage) {

            int length = freq.length;

            for(int j = 0; j < length; j++) {

                // Doing the percentage check here again;
                if (percentage[j] != 0 && percentage[j] >= 1) {
                    String convertedFreq = AeroActivity.shell.toMHz(freq[j] + "");

                    // Small UI-Tweak;
                    if(convertedFreq.length() < 8)
                        convertedFreq = convertedFreq + "\t";
                    else if (convertedFreq.length() < 7)
                        convertedFreq = convertedFreq + "\t\t";

                    // Handle Deepsleep
                    if(j == 0)
                        loadArray(mResult, new statisticInit("Deepsleep", convertTime(time[j]) + "", percentage[j] + "%"));
                    else if (j == length - 1)
                        loadArray(mResult, new statisticInit("Uptime   ", convertTime(time[j]) + "", percentage[j] + "%"));
                    else
                        loadArray(mResult, new statisticInit(convertedFreq, convertTime(time[j]) + "", percentage[j] + "%"));
                }
            }

        }

        /*
         * Just a wrapper;
         */
        public final void loadArray (statisticInit[] resultSet, statisticInit data) {

            mResult = fillArray(resultSet, data);
        }

        public final statisticInit[] fillArray (statisticInit[] resultSet, statisticInit data) {

            statisticInit[] result = Arrays.copyOf(resultSet, resultSet.length + 1);
            result[resultSet.length] = data;

            return result;
        }
    }
}