package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.adapter.AeroAdapter;
import com.aero.control.adapter.AeroData;
import com.aero.control.helpers.FilePath;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.nicolaspomepuy.discreetapprate.AppRate;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Overview Fragment
 *
 */
public class AeroFragment extends Fragment {

    private ListView mOverView;
    private ViewGroup root;
    private AeroAdapter mAdapter;
    private List<AeroData> mOverviewData = new ArrayList<AeroData>();
    private ShowcaseView mShowCase;
    private int mActionBarHeight = 0;
    private boolean mVisible = true;
    private boolean mExecuted = false;

    private final static String FILENAME = "firstrun";
    private final static String NO_DATA_FOUND = "Unavailable";

    private final static String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu";
    private final static String SCALE_PATH_NAME = "/cpufreq/scaling_cur_freq";
    private final static String SCALE_CPU_UTIL = "/cpufreq/cpu_utilization";
    private final static String CPU_TEMP_FILE = "/sys/devices/virtual/thermal/thermal_zone4/temp";

    private String gpu_file;

    private class RefreshThread extends Thread {

        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }
            @Override
            public void run() {
                try {
                    while (!mInterrupt) {
                        sleep(1000);
                        mRefreshHandler.sendEmptyMessage(1);
                    }
                } catch (InterruptedException e) {}
            }
        }

        private RefreshThread mRefreshThread = new RefreshThread();

        private Handler mRefreshHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (msg.what >= 1) {

                    if (isVisible() && mVisible) {
                        createList();
                        mVisible = true;
                    }
                }
            }
        };

    @Override
    public void onPause() {
        super.onPause();

        mVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        mVisible = true;
        // onPause we need to reset our adapter;
        mAdapter = null;
    }

    // Override for custom view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.overviewlist_item, null);

        /*
         * Start the refresh Thread at startup;
         */

        mOverView = (ListView) root.findViewById(R.id.listView1);

        /* Find correct gpu path */
        for (String a : FilePath.GPU_FILES_RATE) {
            if (AeroActivity.genHelper.doesExist(a)) {
                gpu_file = a;
                break;
            }
        }

        if (!mRefreshThread.isAlive()) {
            mRefreshThread.start();
            mRefreshThread.setPriority(Thread.MIN_PRIORITY);
        }

        // Generate our main ListView;
        createList();

        if (!mExecuted)
            setPermissions();

        AppRate.with(getActivity())
                .text(R.string.rateIt)
                .fromTop(false)
                .delay(2000)
                .autoHide(10000)
                .allowPlayLink(true)
                .checkAndShow();

        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // Set up our file;
        int output = 0;

        if (AeroActivity.genHelper.doesExist(getActivity().getFilesDir().getAbsolutePath() + "/" + FILENAME)) {
            output = 1;
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_aero_fragment, R.string.showcase_aero_fragment_sum);

    }


    // Get all frequencies for all cores;
    public final String getFreqPerCore() {
        String complete_path;
        String freq_string =  "";
        String cpu_util = "";
        final int i = Runtime.getRuntime().availableProcessors();

        // Get the cpu frequency for each cpu;
        for (int k = 0; k < i; k++) {
            complete_path = SCALE_CUR_FILE + k + SCALE_PATH_NAME;
            freq_string = freq_string + " " + AeroActivity.shell.toMHz(AeroActivity.shell.getInfo(complete_path));
        }
        freq_string = freq_string.replace(NO_DATA_FOUND, " Offline ");

        // There is no point in wasting cpu cycles if no file exists;
        if (!(AeroActivity.genHelper.doesExist(SCALE_CUR_FILE + 0 + SCALE_CPU_UTIL)))
            return freq_string;

        // Get the last reported load for each cpu (if available);
        for (int j = 0; j < i; j++)  {
            complete_path = SCALE_CUR_FILE + j + SCALE_CPU_UTIL;

            String tmp = AeroActivity.shell.getInfo(complete_path);
            if (!tmp.equals(NO_DATA_FOUND)) {
                if (Integer.parseInt(tmp) < 10) {
                    tmp = " " + tmp;
                }
            }

            cpu_util = cpu_util + "\t\t\t" + tmp + "%";
        }
        cpu_util = cpu_util.replace(NO_DATA_FOUND + "%", "--");
        freq_string = freq_string + "\n" + cpu_util;

        return freq_string;
    }

    private String getCPUTemp() {

        if (AeroActivity.genHelper.doesExist(CPU_TEMP_FILE))
            return AeroActivity.shell.getInfo(CPU_TEMP_FILE) + " °C";
        else
            return null;
    }

    public void createList() {

        String gpu_freq;

        /*
         * Cleanup all data, if there are any;
         */
        if (mOverviewData != null) {
            mOverviewData.clear();
        }
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        }

        gpu_freq = AeroActivity.shell.getInfo(gpu_file);

        if (gpu_freq.length() <= 3)
            gpu_freq = NO_DATA_FOUND;

        // Default Overview Menu
        mOverviewData.add(new AeroData(getString(R.string.kernel_version), AeroActivity.shell.getKernel(), null));
        mOverviewData.add(new AeroData(getString(R.string.current_governor), AeroActivity.shell.getInfo(FilePath.GOV_FILE), null));
        mOverviewData.add(new AeroData(getString(R.string.current_io_governor), AeroActivity.shell.getInfo(FilePath.GOV_IO_FILE), null));
        mOverviewData.add(new AeroData(getString(R.string.current_cpu_speed), getFreqPerCore(), getCPUTemp()));
        mOverviewData.add(new AeroData(getString(R.string.current_gpu_speed), AeroActivity.shell.toMHz((gpu_freq.substring(0, gpu_freq.length() - 3))), null));
        mOverviewData.add(new AeroData(getString(R.string.available_memory), AeroActivity.shell.getMemory(FilePath.FILENAME_PROC_MEMINFO), null));


        if (mAdapter == null) {
            /*
             * Create our ArrayAdapter and bound it to our listview.
             * Notice; we can only set our Adapter if it is freshly new,
             * otherwise we can just fall through and execute a
             * notifyDataSetChange() of our Adapter in the main UI Thread.
             */
            mAdapter = new AeroAdapter(getActivity(),
                    R.layout.overviewlist_item, mOverviewData);
            mOverView.setAdapter(mAdapter);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void DrawFirstStart(int header, int content) {

        try {
            final FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        Target homeTarget = new Target() {
            @Override
            public Point getPoint() {
                return new Point(100, mActionBarHeight);
            }
        };

        mShowCase = new ShowcaseView.Builder(getActivity())
                .setContentTitle(header)
                .setContentText(content)
                .setTarget(homeTarget)
                .build();
    }

    public void setPermissions() {

        final String[] commands = new String[] {
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
                };
        AeroActivity.shell.setRootInfo(commands);

        mExecuted = true;
    }

}