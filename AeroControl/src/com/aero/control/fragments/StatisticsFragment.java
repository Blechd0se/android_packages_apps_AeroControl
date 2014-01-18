package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aero.control.MainActivity;
import com.aero.control.R;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Christ on 10.01.14.
 * CPU Statistics Fragment
 */
public class StatisticsFragment extends Fragment {
    /*
    TODO: - Take time from other cores in account as well (not critical, governors keep freqs mostly synced)
          - Change StableArrayAdapter to own one
     */
    public StatisticsFragment mStatisticsFragment;
    public int mIndex = 0;
    public ViewGroup root;
    public String[] data;
    public ListView statisticView;
    public PieGraph pg;
    public TextView txtFreq;
    public TextView txtPercentage;
    public TextView txtTime;

    public ArrayList<Long> cpuTime = new ArrayList<Long>();
    public ArrayList<Long> cpuFreq = new ArrayList<Long>();

    public static final String[] color_code = {
        "#1abc9c", /* Turquoise */
        "#FF8800", /* Orange */
        "#2c3e50", /* Midnight Blue */
        "#2980b9", /* Nephritis */
        "#f1c40f", /* Sunflower */
        "#8e44ad", /* Wisteria */
        "#3498db", /* Peter River */
        "#e74c3c", /* Pomegrante */
    };

    public static final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";

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

    private void loadUI(boolean firstView) {

        final ArrayList<String> cpuValues = new ArrayList<String>();
        Long[] cpuFreqArray;
        double completeTime = 0;
        double a;
        pg = (PieGraph) root.findViewById(R.id.graph);

        for (int k = 0; k < getCpuData(); k++) {
            String b = data[k];
            String[] c = b.split(" ");
            if(k == 0) {
                a = Integer.parseInt(c[0]);
            } else {
                a = Integer.parseInt(c[1]);
            }

            completeTime = completeTime + a;
        }

        for (int i = 0, j = 0; i < getCpuData(); i++) {

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

            PieSlice slice = new PieSlice();
            String frequency, time_in_state;
            int percentage;

            // Color change;
            if (j == 8)
                j = 0;

            if (g != 0 && ((g / completeTime) * 100) >= 1) {

                if(cpuFreqArray[i] == 0)
                    frequency = "DeepSleep";
                else
                    frequency = MainActivity.shell.toMHz(cpuFreqArray[i].toString());

                time_in_state = convertTime(g);
                percentage = (int)((g / completeTime) * 100);

                cpuValues.add(frequency + " " + time_in_state + " " + percentage + "%");

                slice.setValue(percentage);
                slice.setColor(Color.parseColor(color_code[j]));
                pg.setThickness(30);
                pg.addSlice(slice);

                j++;
            }
            i++;

        }

        // Fill our listview with final values and load TextViews;
        createList(cpuValues);
        if (firstView)
            handleOnClick(cpuValues);

        pg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    handleOnClick(cpuValues);
                    return true;
                }

                return false;
            }
        });

    }

    private void clearUI() {

        /*
         * Cleanup the whole UI.
         * Notice: PieGraph and data might be cleaned anyway,
         * clearing cpuTime/cpuFreq is _really_ necessary:
         */

        if(pg != null)
            pg.removeSlices();

        if(data != null)
            data = new String[0];

        if(cpuTime != null)
            cpuTime.clear();

        if(cpuFreq != null)
            cpuFreq.clear();

    }

    public void handleOnClick(ArrayList<String> list) {

        final String[] valueArray = list.toArray(new String[0]);
        int i;

        for (String a: valueArray) {

            int arrayLength = valueArray.length;

            if(mIndex == arrayLength)
                mIndex = 0;

            /*
             * Fix exception;
             */
            i = mIndex;
            if (i == 8)
                i = 0;

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

            txtFreq.setTextColor(Color.parseColor(color_code[i]));
            txtTime.setTextColor(Color.parseColor(color_code[i]));
            txtPercentage.setTextColor(Color.parseColor(color_code[i]));
        }
        mIndex++;
    }

    /*
     * Convert usertime in human readable values;
     */
    public String convertTime(long msTime) {

        msTime = msTime * 10;

        return String.format("%02dh:%02dm:%02ds",
                TimeUnit.MILLISECONDS.toHours(msTime),
                TimeUnit.MILLISECONDS.toMinutes(msTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(msTime)),
                TimeUnit.MILLISECONDS.toSeconds(msTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(msTime))
        );
    }

    public void createList(ArrayList<String> list) {

        statisticView = (ListView)root.findViewById(R.id.statisticListView);

        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, list);

        statisticView.setAdapter(adapter);

    }


    public int getCpuData() {

        File cpu_stats = new File(TIME_IN_STATE_PATH);

        if (!cpu_stats.exists())
            return 0;

        data = MainActivity.shell.getInfo(TIME_IN_STATE_PATH, true);


        return data.length;
    }


    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
