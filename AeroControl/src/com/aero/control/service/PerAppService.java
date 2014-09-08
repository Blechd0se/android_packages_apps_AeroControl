package com.aero.control.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
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
    private SharedPreferences mPerAppPrefs;
    private ActivityManager mAm;
    private Context mContext;
    private static final settingsHelper settingsHelper = new settingsHelper();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mContext == null)
            mContext = this;

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

        if (mPerAppPrefs == null)
            mPerAppPrefs = mContext.getSharedPreferences(perAppProfileHandler, Context.MODE_PRIVATE);

        Looper.prepare();

        // init our data;
        setAppData();

        if (mPreviousApp != null && mCurrentApp != null) {
            if (!(mPreviousApp.equals(mCurrentApp))) {

                if(mActive) {
                    Toast.makeText(mContext, "Returning to normal usage", Toast.LENGTH_LONG).show();
                    mActive = false;
                    mProfile = null;
                    settingsHelper.executeDefault();
                }

                final Map<String,?> keys = mPerAppPrefs.getAll();

                for (final Map.Entry<String,?> entry : keys.entrySet()) {

                    final String savedSelectedProfiles = mPerAppPrefs.getString(entry.getKey(), null);
                    if (savedSelectedProfiles == null)
                        return;

                    final String tmp[] = savedSelectedProfiles.replace("+", " ").split(" ");

                    for (final String a : tmp) {
                        if (mCurrentApp.equals(a)) {
                            mProfile = entry.getKey();
                            Toast.makeText(mContext, "Applying Per-App Profile ", Toast.LENGTH_LONG).show();
                            mActive = true;

                            // Passing the profile to our settings helper;
                            settingsHelper.setSettings(mContext, mProfile);
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

        if (mAm == null)
            mAm = (ActivityManager) PerAppService.this.getSystemService(ACTIVITY_SERVICE);
        // Get the first item in the list;
        final ActivityManager.RunningTaskInfo AppInfo = mAm.getRunningTasks(1).get(0);

        PackageName = AppInfo.topActivity.getPackageName();

        mCurrentApp = PackageName;
    }
}