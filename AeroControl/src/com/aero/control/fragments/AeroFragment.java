package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
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
import com.aero.control.adapter.adapterInit;
import com.espian.showcaseview.ShowcaseView;

import java.io.File;
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

    // Values to read from;
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String[] GPU_FILES = {"/sys/class/kgsl/kgsl-3d0/gpuclk", /* Adreno GPUs */
                                                "/sys/devices/platform/omap/pvrsrvkm.0/sgx_fck_rate" /* Defy 3.0 Kernel */,
                                                "/proc/gpu/cur_rate" /* Defy 2.6 Kernel */};
    public static final String FILENAME_PROC_MEMINFO = "/proc/meminfo";

    public ListView listView1;
    public ViewGroup root;
    public AeroAdapter adapter;
    public List<adapterInit> mOverviewData= new ArrayList<adapterInit>();
    public AeroFragment mAeroFragment;
    public ShowcaseView.ConfigOptions mConfigOptions;
    public ShowcaseView mShowCase;
    public boolean mVisible = true;

    public final static String FILENAME = "firstrun";

    public String gpu_file;

    public Fragment newInstance(Context context) {
        mAeroFragment = new AeroFragment();

        return mAeroFragment;
    }

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
                } catch (InterruptedException e) {

                }
            }
        };

        private RefreshThread mRefreshThread = new RefreshThread();

        private Handler mRefreshHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (msg.what >= 1) {


                    if (isVisible() && mVisible) {
                        createList();
                        mVisible = true;
                    } else {
                        // Do nothing
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
        adapter = null;
    }

    // Override for custom view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.overviewlist_item, null);

        /*
         * Start the refresh Thread at startup;
         */

        listView1 = (ListView) root.findViewById(R.id.listView1);

        /* Find correct gpu path */
        for (String a : GPU_FILES) {
            if (new File(a).exists()) {
                gpu_file = a;
                break;
            }
        }

        try {
            mRefreshThread.start();
            mRefreshThread.setPriority(Thread.MIN_PRIORITY);
        } catch (Exception e) {
        }

        // Generate our main ListView;
        createList();
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
        // Prepare Showcase;
        mConfigOptions = new ShowcaseView.ConfigOptions();
        mConfigOptions.hideOnClickOutside = false;
        mConfigOptions.shotType = ShowcaseView.TYPE_ONE_SHOT;

        // Set up our file;
        int output = 0;
        byte[] buffer = new byte[1024];

        try {
            final FileInputStream fis = getActivity().openFileInput(FILENAME);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_aero_fragment, R.string.showcase_aero_fragment_sum);

    }


    // Get all frequencies for all cores;
    public final String getFreqPerCore() {
        final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu";
        final String SCALE_PATH_NAME = "/cpufreq/scaling_cur_freq";
        String complete_path;
        String freq_string =  "";
        int i = Runtime.getRuntime().availableProcessors();

        for (int k = 0; k < i; k++) {
            complete_path = SCALE_CUR_FILE + k + SCALE_PATH_NAME;
            freq_string = freq_string + " " + AeroActivity.shell.toMHz(AeroActivity.shell.getInfo(complete_path));
        }
        freq_string = freq_string.replace("Unavailable", " Offline ");

        return freq_string;
    }

    public void createList() {

        /*
         * Cleanup all data, if there are any;
         */
        if (mOverviewData != null) {
            mOverviewData.clear();
        }
        if (adapter != null) {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }

        // Default Overview Menu
        mOverviewData.add(new adapterInit(getString(R.string.kernel_version), AeroActivity.shell.getKernel()));
        mOverviewData.add(new adapterInit(getString(R.string.current_governor), AeroActivity.shell.getInfo(GOV_FILE)));
        mOverviewData.add(new adapterInit(getString(R.string.current_io_governor), AeroActivity.shell.getInfo(GOV_IO_FILE)));
        mOverviewData.add(new adapterInit(getString(R.string.current_cpu_speed), getFreqPerCore()));
        mOverviewData.add(new adapterInit(getString(R.string.current_gpu_speed), AeroActivity.shell.toMHz((AeroActivity.shell.getInfo(gpu_file).substring(0, AeroActivity.shell.getInfo(gpu_file).length() - 3)))));
        mOverviewData.add(new adapterInit(getString(R.string.available_memory), AeroActivity.shell.getMemory(FILENAME_PROC_MEMINFO)));


        if (adapter == null) {
            /*
             * Create our ArrayAdapter and bound it to our listview.
             * Notice; we can only set our Adapter if it is freshly new,
             * otherwise we can just fall through and execute a
             * notifyDataSetChange() of our Adapter in the main UI Thread.
             */
            adapter = new AeroAdapter(getActivity(),
                    R.layout.overviewlist_item, mOverviewData);
            listView1.setAdapter(adapter);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void DrawFirstStart(int header, int content) {

        int actionBarHeight = 0;

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
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        mShowCase = ShowcaseView.insertShowcaseView(100, (actionBarHeight + 50), getActivity(), header, content, mConfigOptions);
    }

    public void setPermissions() {


        final String[] commands = new String[]
                {
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
                };
        AeroActivity.shell.setRootInfo(commands);

    }

}