package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.aero.control.R;
import com.aero.control.adapter.AeroAdapter;
import com.aero.control.adapter.adapterInit;
import com.aero.control.shell.shellScripts;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Overview Fragment
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
    public shellScripts shell = new shellScripts();
    public AeroAdapter adapter;
    public AeroFragment mAeroFragment;

    public Fragment newInstance(Context context) {
        mAeroFragment = new AeroFragment();

        return mAeroFragment;
    }

    private class RefreshThread extends Thread {

        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }
        public void continueThread() {
            mInterrupt = false;
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

                    adapter.notifyDataSetChanged();

                    if (isVisible()) {
                        createList();
                    } else {
                        //
                    }

                }
            }
        };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRefreshThread.interrupt();
        try {
            mRefreshThread.join();
        } catch (InterruptedException e) {
        }
    }



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



        return root;
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
                        new adapterInit(0, getString(R.string.current_gpu_speed), shell.getInfo(GPU_FREQ)),
                        new adapterInit(0, getString(R.string.available_memory), shell.getMemory(FILENAME_PROC_MEMINFO))
                };

        listView1 = (ListView) root.findViewById(R.id.listView1);

        adapter = new AeroAdapter(getActivity(),
                R.layout.overviewlist_item, overview_data);

        listView1.setAdapter(adapter);

    }

}