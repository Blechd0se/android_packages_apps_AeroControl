package com.aero.control.boot;


import com.aero.control.helpers.settingsHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/*
 * TODO: - Add OC/UC Support
 *       - Make generic for governor parameters
 */

public class bootService extends Service
{

    private static final settingsHelper settings = new settingsHelper();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    public void onDestroy()
    {
        // service stopped
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // service started

        settings.setSettings(getBaseContext(), 1);

        return START_NOT_STICKY;
    }
}