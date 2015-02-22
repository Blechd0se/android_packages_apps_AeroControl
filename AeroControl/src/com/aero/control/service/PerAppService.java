package com.aero.control.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.Handler;
import android.widget.Toast;

import com.aero.control.helpers.settingsHelper;

import java.util.List;
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
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable;

    @Override
    public void onCreate() {

        if (mContext == null)
            mContext = this;

        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do work in its own thread;
                            runTask();
                        }
                    });
                }
            };
        }
        new Thread(mRunnable).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Do work in its own thread;
        if (mRunnable != null)
            new Thread(mRunnable).start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runTask() {

        if (mPerAppPrefs == null)
            mPerAppPrefs = mContext.getSharedPreferences(perAppProfileHandler, Context.MODE_PRIVATE);

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
                            settingsHelper.setSettings(mContext, mProfile, false);
                        }
                    }
                }
            }
            else {
                // No app change detected, return;
                return;
            }
        }
    }
    private void setAppData() {

        String PackageName = mCurrentApp;
        mPreviousApp = PackageName;

        if (mAm == null)
            mAm = (ActivityManager) PerAppService.this.getSystemService(ACTIVITY_SERVICE);

        // Get the first item in the list;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            PackageName = getTopApp();
        } else {
            ActivityManager.RunningTaskInfo AppInfo = mAm.getRunningTasks(1).get(0);

            PackageName = AppInfo.topActivity.getPackageName();
        }

        mCurrentApp = PackageName;
    }

    private String getTopApp() {

        List<ActivityManager.RunningAppProcessInfo> appProcesses = mAm.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo appProcess : appProcesses){
            if(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                // Always return the first one, since this is the one that we are looking for;
                return appProcess.processName;
            }
        }
        return null;
    }
}