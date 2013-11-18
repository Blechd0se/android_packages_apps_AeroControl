package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.aero.control.R;
import com.aero.control.adapter.AeroAdapter;
import com.aero.control.adapter.adapterInit;
import com.aero.control.helpers.shellHelper;
import com.espian.showcaseview.ShowcaseView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Overview Fragment
 *
 * TODO: Proper implementation for the UpdateThread
 *       with onResume() and onPause()
 */
public class AeroFragment extends Fragment {

    // Values to read from;
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String GPU_FREQ = "/proc/gpu/cur_rate";
    private static final String FILENAME_PROC_MEMINFO = "/proc/meminfo";

    public ListView listView1;
    public ViewGroup root;
    public shellHelper shell = new shellHelper();
    public AeroAdapter adapter;
    public AeroFragment mAeroFragment;
    public ShowcaseView.ConfigOptions mConfigOptions;
    public ShowcaseView mShowCase;

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


                    if (isVisible()) {
                        createList();
                    } else {
                        // Do nothing
                    }

                }
            }
        };

    // Override for custom view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.overviewlist_item, null);

        /*
         * Start the refresh Thread at startup;
         * Current Problem; Thread won't start again after FragmentSwitch
         */

        try {
            mRefreshThread.start();
        } catch (Exception e) {
        }

        // Generate our main ListView;
        createList();
        setPermissions();



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
        String FILENAME = "firstrun";
        int output = 0;
        byte[] buffer = new byte[1024];

        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_aero_fragment, R.string.showcase_aero_fragment_sum);

    }

    public void createList() {

        // Default Overview Menu
        adapterInit overview_data[] = new adapterInit[]
                {
                        // First Value (0) is for loadable images.
                        new adapterInit(0, getString(R.string.kernel_version), shell.getKernel()),
                        new adapterInit(0, getString(R.string.current_governor), shell.getInfo(GOV_FILE)),
                        new adapterInit(0, getString(R.string.current_io_governor), shell.getInfo(GOV_IO_FILE)),
                        new adapterInit(0, getString(R.string.current_cpu_speed), shell.toMHz(shell.getInfo(SCALE_CUR_FILE))),
                        new adapterInit(0, getString(R.string.current_gpu_speed), shell.toMHz((shell.getInfo(GPU_FREQ).substring(0, shell.getInfo(GPU_FREQ).length() - 3)))),
                        new adapterInit(0, getString(R.string.available_memory), shell.getMemory(FILENAME_PROC_MEMINFO))
                };

        listView1 = (ListView) root.findViewById(R.id.listView1);

        adapter = new AeroAdapter(getActivity(),
                R.layout.overviewlist_item, overview_data);

        listView1.setAdapter(adapter);

    }

    public void DrawFirstStart(int header, int content) {

        String FILENAME = "firstrun";
        String string = "1";

        try {
            FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        mDrawerLayout.openDrawer(mDrawerLayout)
        mShowCase = ShowcaseView.insertShowcaseView(100, 175, getActivity(), header, content, mConfigOptions);
    }

    public void setPermissions() {


        String[] commands = new String[]
                {
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                        "chmod 0664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
                };
        shell.setRootInfo(commands);

    }

}