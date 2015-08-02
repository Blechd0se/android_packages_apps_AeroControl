package com.aero.control.helpers.PerApp.AppMonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.PerApp.AppMonitor.model.AppElement;
import com.aero.control.helpers.PerApp.AppMonitor.model.AppElementDetail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Alexander Christ on 30.04.15.
 *
 * Handles the main calls and passes the app context through
 */
public final class JobManager {

    private AppData mAppData;
    private final String mClassName = getClass().getName();
    private static final String mPreferenceValue = "per_app_monitor";
    private static final String FILENAME_APPMONITOR_NOTIFY = "appmonitor_notify";
    private boolean mJobManagerEnable = true;
    private List<AppModule> mModules;
    private AppModuleData mAppModuleData;
    private boolean mSleeping = false;
    private boolean mPrevSleeping = false;
    private Context mContext;
    private boolean mNotifcationShowed = false;

    private static JobManager mJobManager;

    private JobManager() {
        this.mAppData = new AppData();
        this.mModules = new ArrayList<AppModule>();
        loadModules();
        // We add our loaded modules;
        this.mAppModuleData = new AppModuleData(getModules());
        AppLogger.print(mClassName, "JobManager initialized!", 0);
    }

    /**
     * Instead of creating a new instance of the JobManager directly we call this method
     * to synchronize access. It is guaranteed to return a JobManager-Object (creates a
     * new one if its NULL).
     * @return JobManager
     */
    public static synchronized JobManager instance() {
        if (mJobManager == null)
            mJobManager = new JobManager();

        return mJobManager;
    }

    /**
     * Enables the JobManager
     */
    public final void enable() {
        this.mJobManagerEnable = true;
        AppLogger.print(mClassName, "JobManager enabled!",0 );
    }

    /**
     * Disables the JobManager
     */
    public final void disable() {
        this.mJobManagerEnable = false;
        AppLogger.print(mClassName, "JobManager disabled!",0 );
    }

    /**
     * Returns the current state of the JobManager. If true, the JobManager is enabled.
     * @return boolean
     */
    public final boolean getJobManagerState() {
        return mJobManagerEnable;
    }

    /**
     * Sets the current context (androids context).
     * @param context
     */
    public final void setContext(final Context context) {
        this.mContext = context;
    }

    /**
     * Main method to get the gathered data in some kind of pretty format.
     * It iterates through all available modules(data) for each found app
     * and adds them to a custom AppElement object.
     * Also sorts the list descending for the app usage time and only adds
     * apps which are at least above the minimum time threshold.
     *
     * @return List<AppElement>
     */
    public final List<AppElement> getParentChildData(final Context context) {

        final List<AppElement> data = new ArrayList<AppElement>();
        final PackageManager pm = context.getPackageManager();
        final List<AppModuleMetaData> appModuleMetaData = this.getModuleData().getAppModuleData();
        Drawable appicon;

        for (AppModuleMetaData ammd : appModuleMetaData) {
            AppLogger.print(mClassName, "App Module Data found! (" + ammd.getAppContext().getAppName() + ")", 2);
            AppLogger.print(mClassName, ammd.getAppContext().getAppName() + " Time used: (" + ammd.getAppContext().getTimeUsage() + "ms) " + ":", 2);

            // Is this context "ready"?
            if (!ammd.getAppContext().isAboveThreshold())
                continue;

            // Load our package image and our package label + package name here;
            try {
                appicon = pm.getApplicationIcon(ammd.getAppContext().getAppName());
            } catch (PackageManager.NameNotFoundException e) {
                appicon = null;
            }

            final AppElement parentData = new AppElement(ammd.getAppContext().getAppName(), appicon);

            // We copy the usage time once into our parent (for sorting reasons) and once into our child;
            parentData.setUsage(ammd.getAppContext().getTimeUsage());
            parentData.setRealName(ammd.getAppContext().getRealAppName(context));
            parentData.getChilData().add(new AppElementDetail(ammd.getAppContext().getFormatTimeUsage(), ""));

            for (AppModule module : this.getModules()) {

                parentData.getChilData().add(new AppElementDetail(module.getPrefix(), ammd.getAverage(module.getIdentifier()) + module.getSuffix()));

                AppLogger.print(mClassName, "------ Average: " + ammd.getAverage(module.getIdentifier()), 2);
            }

            // Null would mean the app has been deleted or got missing
            if (parentData.getRealName() != null)
                data.add(parentData);
        }

        Collections.sort(data, new Comparator<AppElement>() {
            @Override
            public int compare(AppElement lhs, AppElement rhs) {
                return rhs.getUsage().compareTo(lhs.getUsage());
            }
        });

        return data;
    }

    /**
     * Returns the raw data for an app (appname e.g. com.aero.control) for the UI.
     * It contains the gathered data for one module(identifier).
     * @param appname    String, appname [packagename]
     * @param identifier int, module identifier (see AppModule for valid identifiers)
     * @return List<Integer>
     */
    public final List<Integer> getRawData(final String appname, final int identifier) {

        final AppContext context = getSimpleAppContext(appname);

        if (context == null)
            return null;

        for (AppModuleMetaData ammd : this.getModuleData().getAppModuleData()) {
            if (ammd.getAppContext() == context) {
                // Winner!
                return ammd.getRawData(identifier);
            }
        }

        return null;
    }

    /**
     * Main method of the JobManager. Iterates through all added modules and gets the values
     * for the app context. Also handles the logic if the device is sleeping.
     * @param context AppContext which is passed from the calling method.
     */
    public final void schedule(final AppContext context) {
        // If the context is null, return early;
        if (context == null) {
            return;
        }

        if (mPrevSleeping && !mSleeping) {
            // Set the last check time, so we don't count sleep-time;
            context.setLastCheckedNow();
        }

        // Return early if we are sleeping;
        if (mSleeping) {
            return;
        }

        // Allow to disable (in the next cycle) the JobManager at runtime;
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(mPreferenceValue, true))
            disable();

        AppLogger.print(mClassName, "Calling context switch for: " + context.getAppName(), 1);
        mAppData.addContext(context);

        // Go through our modules and read/save data;
        for (AppModule module : mModules) {
            module.operate();
            mAppModuleData.addData(context, module.getLastValue(), module);
        }

        if (!mNotifcationShowed) {
            for (AppModuleMetaData ammd : this.getModuleData().getAppModuleData()) {
                // Is this context "ready"?
                if (ammd.getAppContext().isAboveThreshold()) {
                    if (!AeroActivity.genHelper.doesExist(mContext.getFilesDir().getAbsolutePath() + "/" + FILENAME_APPMONITOR_NOTIFY))
                        showNotification();
                }
            }
        }

    }

    /**
     * Returns just the AppContext from a given appname if the JobManager is not disabled
     * or not sleeping.
     * @param appname String, appname (e.g. com.aero.control)
     * @return AppContext
     */
    public final AppContext getSimpleAppContext(final String appname) {

        // Since this class is used for GUI only, the sleeping part should in theory never be true;
        if (!mJobManagerEnable || mSleeping) {
            if (mSleeping && !mPrevSleeping)
                AppLogger.print(mClassName, "JobManager is disabled", 0);
            return null;
        }

        return mAppData.getSimpleAppContext(appname);
    }

    /**
     * Gets the AppContext object from a lower class. If the JobManager is disabled we return
     * null. Increases the timely usage and potential other usage counters.
     * @param   appname String, the app name (e.g. "com.aero.control")
     * @return  AppContext
     */
    public final AppContext getAppContext(final String appname) {
        if (!mJobManagerEnable || mSleeping) {
            if (mSleeping && !mPrevSleeping)
                AppLogger.print(mClassName, "JobManager is disabled", 0);
            return null;
        }

        if (mPrevSleeping && !mSleeping) {
            // Set the last check time, so we don't count sleep-time;
            if (mAppData.getSimpleAppContext(appname) != null)
                mAppData.getSimpleAppContext(appname).setLastCheckedNow();
        }

        return mAppData.getAppContext(appname);
    }

    /**
     * Allows the JobManager to sleep when the device is sleeping. The actual code to check
     * if the screen is turned off/on is not included here.
     *
     * @param sleepValue boolean, should we sleep or not?
     */
    public final void setSleep(final boolean sleepValue) {
        // Show debug info, but only once per sleep-cycle;
        if (sleepValue && !mSleeping) {
            AppLogger.print(mClassName, "JobManager is sleeping because the display is off!", 0);
        }
        this.mPrevSleeping = mSleeping;
        this.mSleeping = sleepValue;
    }

    /**
     * Returns the current sleeping state (true = sleeping)
     * @return boolean
     */
    public final boolean getSleepState() {
        return mSleeping;
    }

    /**
     * Checks if we are sleeping at the moment and wakes the JobManager up.
     * Used inside the GUI.
     */
    public synchronized final void wakeUp() {
        if (getSleepState()) {
            AppLogger.print(mClassName, "Forcing a wakeup of the JobManager...", 0);
            setSleep(false);
        }
    }

    /**
     * Load all desired modules upon start which will then be periodically checked.
     */
    private void loadModules() {

        int counter = 0;

        // Load our modules;
        mModules.add(new CPUFreqModule());
        mModules.add(new CPUNumModule());
        mModules.add(new RAMModule());
        if (AeroActivity.genHelper.doesExist(FilePath.CPU_TEMP_FILE))
            mModules.add(new TEMPModule());

        for (String s : FilePath.GPU_FREQ_ARRAY) {
            if (AeroActivity.genHelper.doesExist(s))
                counter++;
        }
        if (counter >= 0) {
            mModules.add(new GPUFreqModule());
        }

        AppLogger.print(mClassName, "Modules successfully initialized!", 0);
    }

    /**
     * Gets the complete AppModuleData which is available to the JobManager.
     *
     * @return AppModuleData
     */
    private AppModuleData getModuleData() {
        return mAppModuleData;
    }

    /**
     * Gets all available and loaded modules.
     *
     * @return List<AppModule>
     */
    public final List<AppModule> getModules() {
        return mModules;
    }

    /**
     * Shows a notification which allows the user to directly enter to appmonitor fragment
     */
    protected final void showNotification() {

        final Intent resultIntent = new Intent(mContext, AeroActivity.class);

        resultIntent.putExtra("NOTIFY_STRING", "APPMONITOR");
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent viewPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification.Builder builder = new Notification.Builder(mContext)
                        .setContentTitle("Aero Control")
                        .setContentText("We collected enough data, check them out in the AppMonitor section!")
                        .setSmallIcon(R.drawable.rocket)
                        .setContentIntent(viewPendingIntent)
                        .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            notificationManager.notify(0, builder.build());
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            notificationManager.notify(0, builder.getNotification());

        try {
            final FileOutputStream fos = mContext.openFileOutput(FILENAME_APPMONITOR_NOTIFY, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {}

        this.mNotifcationShowed = true;
    }
}
