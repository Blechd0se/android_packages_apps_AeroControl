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
    public shellScripts shell = new shellScripts();
    public String current_speed;
    public AeroAdapter adapter;

    public static Fragment newInstance(Context context) {
        AeroFragment f = new AeroFragment();

        return f;
    }

    private class CurCPUThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(1000);
                    final String curFreq = shell.getInfo(SCALE_CUR_FILE);
                    if (curFreq != null)
                        mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, curFreq));
                }
            } catch (InterruptedException e) {
            }
        }
    }

    ;

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            current_speed = (shell.toMHz((String) msg.obj));
            adapter.notifyDataSetChanged();
        }
    };

    // Override for custom view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.overviewlist_item, null);


        /*
         * We should refresh our list (to be exactly; cpu/gpu speed) every Second
         * How can we update a dynamic list view?
         */
        try {
            mCurCPUThread.start();
        } catch (Exception e) {
        }

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

        return root;
    }


}