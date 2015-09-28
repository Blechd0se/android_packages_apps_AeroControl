package com.aero.control.helpers.PerApp.AppMonitor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.aero.control.helpers.Util;

import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Christ on 30.04.15.
 *
 * The main class to hold the app context for each app. It also holds
 * information about the time usage of the spoken app.
 */
public class AppContext {

    private String mAppName;
    private long mTimeUsage = 0;                      // TimeUsage in milliseconds
    private long mLastChecked = 0;

    public AppContext(final String appname) {
        if (mAppName != null)
            throw new ExceptionHandler(ExceptionHandler.EX_APP_NAME_OVERRIDE + " (" + mAppName + ") ");

        this.mAppName = appname;
    }

    /**
     * Returns the appname of this context (e.g. com.aero.control)
     * @return String, the packagename of the app.
     */
    public final String getAppName() {
        return mAppName;
    }

    /**
     * Sets the the last checked time to now (in ms).
     */
    public final void setLastCheckedNow() {
        this.mLastChecked = System.currentTimeMillis();
    }

    /**
     * Sets the last checked time to a desired time (in ms)
     * @param lastchecked, the actual value in ms
     */
    public final void setLastChecked(long lastchecked) {
        this.mLastChecked = lastchecked;
    }

    /**
     * Returns the last checked value, if it hasn't been checked before
     * it calls setLastCheckedNow() to set the current time.
     * @return long, the last checked time value
     */
    public final long getLastChecked() {
        if (mLastChecked == 0)
            mLastChecked = System.currentTimeMillis();

        return mLastChecked;
    }

    /**
     * Increases the timely usage of this context by the passed amount.
     * @param time long, the amount to increase the usage counter.
     */
    public final void increaseTimeUsage(final long time) {
        this.mTimeUsage += time;
    }

    /**
     * Returns the current usage time of this app context.
     * @return long, current timely usage.
     */
    public final long getTimeUsage() {
        return mTimeUsage;
    }

    /**
     * Sets the current time usage value for this context
     * @param timeusage long, the amount of total usage in ms
     */
    public final void setTimeUsage(long timeusage) {
        this.mTimeUsage = timeusage;
    }

    /**
     * Returns a formatted string of the current time usage.
     * Just a wrapper to call the right method in the Util class
     * @return String
     */
    public final String getFormatTimeUsage() {
        return Util.getFormatedTimeString(getTimeUsage());
    }

    /**
     * Returns true or false whether the current timely usage is above our threshold.
     * Used for the cleanup-routines.
     * @return boolean
     */
    public final boolean isAboveThreshold() {
        return getTimeUsage() > Configuration.TIME_THRESHOLD;
    }

    /**
     * Small cleanup-routine to reset our last checked time and timely usage values.
     */
    public final void cleanUp() {
        this.mLastChecked = 0;
        this.mTimeUsage = 0;
    }

    /**
     * Returns the real appname of this app context (e.g. Aero Control) by searching
     * for it via the getAppName() method. If we can't find the app (because it has
     * been removed from the system for example) we return NULL.
     * @param context Context, a context passed from the caller
     * @return String, can be NULL otherwise the real app name.
     */
    public final String getRealAppName(final Context context) {

        final PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(getAppName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            // We didnt find anything :(
            return null;
        }

        return (String) pm.getApplicationLabel(appInfo);
    }
}
