package com.aero.control.helpers.PerApp.AppMonitor;


import android.content.Context;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.FilePath;

/**
 * Created by Alexander Christ on 02.08.15.
 * Checks for the current GPU Frequency
 */
public final class GPUFreqModule extends AppModule {

    private final String mClassName = getClass().getName();
    private String mGPUFile;

    public GPUFreqModule(Context context) {
        super(context);
        setName(mClassName);
        setIdentifier(AppModule.MODULE_GPU_IDENTIFIER);
        setPrefix(context.getText(R.string.pref_gpu_frequency));
        setSuffix(" Mhz");
        setDrawable(context.getResources().getDrawable(R.drawable.appmonitor_gpu));

        // Find the correct gpu file, if possible;
        for (String s : FilePath.GPU_FILES_RATE) {
            if (AeroActivity.genHelper.doesExist(s))
                mGPUFile = s;
        }

        AppLogger.print(mClassName, "GPU Frequency Module successfully initialized!", 0);
    }

    private Integer getFormatInt(String s) {

        if (s.length() < 8)
            return (Integer.valueOf(s) / 1000);
        else
            return (Integer.valueOf(s) / 1000000);
    }

    @Override
    protected void operate() {
        super.operate();
        long temp = System.currentTimeMillis();
        Integer gpufreq = null;

        try {
            gpufreq = getFormatInt(AeroActivity.shell.getFastInfo(mGPUFile));
        } catch (NumberFormatException e) {}

        if (gpufreq != null)
            addValues(gpufreq);
        AppLogger.print(mClassName, "GOUFreqModule.operate() time: " + (System.currentTimeMillis() - temp), 1);
    }

}
