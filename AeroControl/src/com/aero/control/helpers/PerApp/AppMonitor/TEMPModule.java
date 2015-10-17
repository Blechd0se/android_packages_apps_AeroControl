package com.aero.control.helpers.PerApp.AppMonitor;


import android.content.Context;

import com.aero.control.AeroActivity;
import com.aero.control.R;

/**
 * Created by Alexander Christ on 03.05.15.
 * Checks for the current CPU temperature
 */
public final class TEMPModule extends AppModule {

    private final String mClassName = getClass().getName();
    private final static String CPU_TEMP_FILE = "/sys/devices/virtual/thermal/thermal_zone4/temp";

    public TEMPModule(Context context) {
        super(context);
        setName(mClassName);
        setIdentifier(AppModule.MODULE_TEMP_IDENTIFIER);
        setPrefix(context.getText(R.string.pref_temp_usage));
        setSuffix(" °C");
        setDrawable(context.getResources().getDrawable(R.drawable.appmonitor_temp));
        AppLogger.print(mClassName, "Temperature Module successfully initialized!", 0);
    }

    @Override
    protected void operate() {
        super.operate();
        long temp = System.currentTimeMillis();
        Integer temperature = null;

        try {
            temperature = Integer.parseInt(AeroActivity.shell.getFastInfo(CPU_TEMP_FILE));
        } catch (NumberFormatException e) {
        }

        if (temperature != null)
            addValues(temperature);
        AppLogger.print(mClassName, "TEMPModule.operate() time: " + (System.currentTimeMillis() - temp), 1);
    }

}
