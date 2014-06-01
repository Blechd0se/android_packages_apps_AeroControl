package com.aero.control.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Alexander Christ on 29.05.14.
 */
public class PerAppServiceHelper {

    private Intent mBackgroundIntent = null;
    private AlarmManager mTimer = null;
    private PendingIntent mPendingIntent = null;
    private SharedPreferences mPrefs;
    private Context mContext;
    private boolean mState;

    public PerAppServiceHelper(Context context) {
        this.mContext = context;
        mBackgroundIntent = new Intent(mContext, PerAppService.class);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public final void setState(boolean state) { mState = state; }

    public final boolean getState() {
        return mState;
    }

    public final boolean shouldBeStarted() {

        final boolean tmp = mPrefs.getBoolean("per_app_service", false);

        if (!tmp)
            setState(false);
        else if (tmp)
            setState(true);

        return getState();
    }

    public final void startService() {

        /* Start Service */
        final Calendar cal = Calendar.getInstance();
        Log.e("Aero", "Service should be started now!");
        mBackgroundIntent = new Intent(mContext, PerAppService.class);
        mContext.startService(mBackgroundIntent);
        mPendingIntent = PendingIntent.getService(mContext, 0, mBackgroundIntent, 0);

        mTimer = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mTimer.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 7 * 1000, mPendingIntent);

        mState = true;
    }

    public final void stopService() {

        if (mBackgroundIntent == null || mTimer == null || mPendingIntent == null) {
            // Nothing is running
            mState = false;
            Log.e("Aero", "Service wasn't even running!");
            return;
        } else {

            // Cleanup;
            mContext.stopService(mBackgroundIntent);
            mTimer.cancel(mPendingIntent);

            mTimer = null;
            mBackgroundIntent = null;
            mPendingIntent = null;
            mState = false;
        }
        Log.e("Aero", "Service should be stopped now!");
    }

}
