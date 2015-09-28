package com.aero.control.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Display;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.helpers.PerApp.AppMonitor.AppContext;
import com.aero.control.helpers.PerApp.AppMonitor.AppLogger;
import com.aero.control.helpers.PerApp.AppMonitor.JobManager;
import com.aero.control.helpers.settingsHelper;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
    private static JobManager mJobManager = AeroActivity.mJobManager;
    private final String mClassName = getClass().getName();
    private int mNullCounter = 0;

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
                            if (AeroActivity.perAppService.getState()) {
                                runTask();
                            }
                            mHandler.postDelayed(mRunnable, 5000);
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

        AppContext localContext = null;

        if (mPerAppPrefs == null)
            mPerAppPrefs = mContext.getSharedPreferences(perAppProfileHandler, Context.MODE_PRIVATE);

        // init our data;
        setAppData();

        if (mJobManager != null) {

            mJobManager.setContext(mContext);
            localContext = mJobManager.getAppContext(mCurrentApp);

            mJobManager.setSleep(isScreenOn());

            /*
             * We need to check three things here before shutting down the JobManager;
             * 1.) Is the context null, e.g. are we disabled or sleeping?
             * 2.) Is the last known state NOT the sleeping state?
             * 3.) Is the screen currently on?
             */
            if (localContext == null && !mJobManager.getSleepState() && isScreenOn()) {
                AppLogger.print(mClassName, "Shutting down JobManager...", 0);
                mJobManager = null;
            } else {
                mJobManager.schedule(localContext);
            }
            AeroActivity.mJobManager = mJobManager;
        }

        // Check if our screen is on, if not, return;
        if (isScreenOn()) {
            return;
        }

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

            if (PackageName == null && !isScreenOn()) {

                mNullCounter++;

                // If the user hasn't set the permissions in 120 seconds, we disable it again;
                if (mNullCounter > 24) {
                    AppLogger.print(mClassName, "Stopping service since the user didn't give us permission", 0);
                    // If we get a null, we should disable this feature (and service);
                    AeroActivity.perAppService.stopService();
                    // We also deactive this in aero controls settings, because the user didn't give us the permissions;
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putBoolean("per_app_service", false);
                    editor.putBoolean("per_app_monitor", false);
                    editor.commit();
                    mNullCounter = 0;
                }
            } else {
                mNullCounter = 0;
            }

        } else {
            ActivityManager.RunningTaskInfo AppInfo = mAm.getRunningTasks(1).get(0);

            PackageName = AppInfo.topActivity.getPackageName();
        }
        if (PackageName != null) {
            PackageName = PackageName.trim();
        }

        mCurrentApp = PackageName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getTopApp() {

        String visibleApp = null;
        long time = System.currentTimeMillis();
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);

        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> sortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!sortedMap.isEmpty()) {
                // Get the last name;
                visibleApp = sortedMap.get(sortedMap.lastKey()).getPackageName();
            }
        }

        return visibleApp;
    }

    private boolean isScreenOn() {

        // Take special care for API20+
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            DisplayManager dm = (DisplayManager) mContext.getSystemService(mContext.DISPLAY_SERVICE);

            // We iterate through all available displays
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF)
                    return false;
            }
            // If we are here, all displays are on;
            return true;

        } else {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            return !pm.isScreenOn();
        }
    }
}