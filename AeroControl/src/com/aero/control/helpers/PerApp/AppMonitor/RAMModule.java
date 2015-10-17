package com.aero.control.helpers.PerApp.AppMonitor;


import android.content.Context;

import com.aero.control.R;
import com.aero.control.helpers.FilePath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Alexander Christ on 03.05.15.
 * Checks for the current free amount of ram of the *whole* system
 */
public final class RAMModule extends AppModule {

    private final String mClassName = getClass().getName();

    public RAMModule(Context context) {
        super(context);
        setName(mClassName);
        setIdentifier(AppModule.MODULE_RAM_IDENTIFIER);
        setPrefix(context.getText(R.string.pref_ram_usage));
        setSuffix(" MB");
        setDrawable(context.getResources().getDrawable(R.drawable.appmonitor_ram));
        AppLogger.print(mClassName, "RAM Module successfully initialized!", 0);
    }

    @Override
    protected void operate() {
        super.operate();
        long temp = System.currentTimeMillis();
        Integer freeRAM;
        String totalMemory;
        String totalFreeMemory = "0";

        try {
            /* /proc/meminfo entries follow this format:
             * MemTotal:         362096 kB
             * MemFree:           29144 kB
             * Buffers:            5236 kB
             * Cached:            81652 kB
             */
            final BufferedReader reader = new BufferedReader(new FileReader(FilePath.FILENAME_PROC_MEMINFO), 1024);
            totalMemory = reader.readLine();
            totalFreeMemory = reader.readLine();

            if (totalFreeMemory != null) {
                String parts[] = totalMemory.split("\\s+");
                parts = totalFreeMemory.split("\\s+");
                if (parts.length == 3) {
                    totalFreeMemory = Long.parseLong(parts[1]) + "";
                }
            }


        } catch (IOException e) {}

        freeRAM = Integer.parseInt(totalFreeMemory);
        freeRAM = freeRAM / 1000;

        addValues(freeRAM);
        AppLogger.print(mClassName, "RAMModule.operate() time: " + (System.currentTimeMillis() - temp), 1);
    }

}
