package com.aero.control.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aero.control.helpers.settingsHelper;

import java.util.Map;

/**
 * Created by Alexander Christ on 17.05.14.
 */
public final class PerAppService extends Service {

    private static String mPreviousApp = null;
    private static String mCurrentApp = null;
    private static final String perAppProfileHandler = "perAppProfileHandler";
    private boolean mActive;
    private String mProfile;
    private static final settingsHelper settingsHelper = new settingsHelper();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Do work in its own thread;
                runTask();

            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final void runTask() {

        final SharedPreferences perAppPrefs = getApplicationContext().getSharedPreferences(perAppProfileHandler, Context.MODE_PRIVATE);

        Looper.prepare();

        // init our data;
        setAppData();

        if (mPreviousApp != null && mCurrentApp != null) {
            if (!(mPreviousApp.equals(mCurrentApp))) {

                if(mActive) {
                    Toast.makeText(getBaseContext(), "Returning to normal usage", 2000).show();
                    mActive = false;
                    mProfile = null;
                    settingsHelper.executeDefault();
                }

                final Map<String,?> keys = perAppPrefs.getAll();

                for (final Map.Entry<String,?> entry : keys.entrySet()) {

                    final String savedSelectedProfiles = perAppPrefs.getString(entry.getKey(), null);
                    if (savedSelectedProfiles == null)
                        return;

                    String tmp[];
                    tmp = savedSelectedProfiles.replace("+", " ").split(" ");


                    for (final String a : tmp) {
                        if (mCurrentApp.equals(a)) {
                            mProfile = entry.getKey();
                            Log.e("Aero", "We found a match! " + mCurrentApp);
                            Toast.makeText(getBaseContext(), "Applying Per-App Profile ", 2000).show();
                            mActive = true;

                            // Passing the profile to our settings helper;
                            settingsHelper.setSettings(getBaseContext(), mProfile);
                        }
                    }
                }
            }
            else {
                // No app change detected, return;
                return;
            }
        }
        Looper.loop();

    }
    private final void setAppData() {

        String PackageName = mCurrentApp;
        mPreviousApp = PackageName;

        final ActivityManager am = (ActivityManager) PerAppService.this.getSystemService(ACTIVITY_SERVICE);
        // Get the first item in the list;
        ActivityManager.RunningTaskInfo AppInfo = am.getRunningTasks(1).get(0);

        PackageName = AppInfo.topActivity.getPackageName();

        mCurrentApp = PackageName;
    }
}
