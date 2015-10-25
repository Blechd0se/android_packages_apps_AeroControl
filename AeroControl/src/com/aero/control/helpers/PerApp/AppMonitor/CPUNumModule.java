package com.aero.control.helpers.PerApp.AppMonitor;

import android.content.Context;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.FilePath;

/**
 * Created by Alexander Christ on 14.05.15.
 * Checks for currently onlined cpu(s)
 */
public final class CPUNumModule extends AppModule {

    private final static String SCALE_CUR_FILE = FilePath.CPU_BASE_PATH;
    private final String mClassName = getClass().getName();

    public CPUNumModule(Context context) {
        super(context);
        setName(mClassName);
        setIdentifier(AppModule.MODULE_CPU_NUM_IDENTIFIER);
        setPrefix(context.getText(R.string.pref_cpu_number));
        setSuffix(" Cores");
        setDrawable(context.getResources().getDrawable(R.drawable.appmonitor_number));
        AppLogger.print(mClassName, "CPU Num Module successfully initialized!", 0);
    }

    @Override
    protected void operate() {
        super.operate();
        long temp = System.currentTimeMillis();

        int onlineCPUs = 0;
        final int i = Runtime.getRuntime().availableProcessors();

        if (i == 1) {
            onlineCPUs++;
        } else {
            // Get the online cpus;
            for (int j = 0; j < i; j++) {
                if (AeroActivity.shell.getFastInfo(SCALE_CUR_FILE + j + "/online").equals("1"))
                    onlineCPUs++;
            }
        }

        addValues(onlineCPUs);
        AppLogger.print(mClassName, "CPUNumModule.operate() time: " + (System.currentTimeMillis() - temp), 1);
    }

}
