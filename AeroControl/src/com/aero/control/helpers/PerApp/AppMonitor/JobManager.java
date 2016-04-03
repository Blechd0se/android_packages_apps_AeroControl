package com.aero.control.helpers.PerApp.AppMonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
    private long mExportThreshold = 0;

    private static JobManager mJobManager;

    private JobManager(Context context) {
        this.mAppData = new AppData();
        this.mModules = new ArrayList<AppModule>();
        this.mContext = context;
        loadModules();
        // We add our loaded modules;
        this.mAppModuleData = new AppModuleData(getModules());

        // If necessary load the saved raw data back in;
        if (Configuration.THREADED_IMPORT) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        importData();
                    } catch (OutOfMemoryError e) {
                        AppLogger.print(mClassName, "We tried to import to much data, deleting import file..." + e, 0);
                        new File(new ContextWrapper(mContext).getFilesDir() + "/" + Configuration.EMERGENCY_FILE).delete();
                    }
                }
            };
            Thread worker = new Thread(run);
            worker.start();
        } else {
            try {
                importData();
            } catch (OutOfMemoryError e) {
                AppLogger.print(mClassName, "We tried to import to much data, deleting import file..." + e, 0);
                new File(new ContextWrapper(mContext).getFilesDir() + "/" + Configuration.EMERGENCY_FILE).delete();
            }
        }

        AppLogger.print(mClassName, "JobManager initialized, AppMonitor Version " + getVersion() + " loaded!", -1);
    }

    /**
     * Instead of creating a new instance of the JobManager directly we call this method
     * to synchronize access. It is guaranteed to return a JobManager-Object (creates a
     * new one if its NULL).
     *
     * @param context Context, the current android context
     * @return JobManager
     */
    public static synchronized JobManager instance(Context context) {
        if (mJobManager == null)
            mJobManager = new JobManager(context);

        return mJobManager;
    }

    /**
     * Enables the JobManager
     */
    public final void enable() {
        this.mJobManagerEnable = true;
        AppLogger.print(mClassName, "JobManager enabled!", 0);
    }

    /**
     * Disables the JobManager
     */
    public final void disable() {
        this.mJobManagerEnable = false;
        AppLogger.print(mClassName, "JobManager disabled!", 0);
    }

    /**
     * Returns the current version of appmonitor.
     * @return String
     */
    public final String getVersion() {
        return Configuration.APPMONITOR_VERSION;
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
     * If possible, forces a cleanup of the data for a specific app
     * @param appname String, the appname (e.g. com.aero.control).
     */
    public final void forceCleanUp(String appname) {

        AppContext context = getSimpleAppContext(appname);

        if (context != null) {

            AppModuleMetaData appModuleMetaData = mAppModuleData.existsAppModuleMetaData(context);

            if (appModuleMetaData != null) {
                appModuleMetaData.cleanUp();
            }

            context.cleanUp();
        }
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
    public synchronized final List<AppElement> getParentChildData(final Context context) {

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
            parentData.getChildData().add(new AppElementDetail(ammd.getAppContext().getFormatTimeUsage(), ""));

            for (AppModule module : this.getModules()) {

                parentData.getChildData().add(new AppElementDetail(module.getPrefix(), ammd.getAverage(module.getIdentifier()) + module.getSuffix()));

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
     * Saves the current available data for all apps we have collected so far
     * and their module data.
     * Saves the current AppContext for each app with the TimeUsed as well as
     * LastChecked value. The data for each module is actually saved as raw data
     * inside an array.
     * This Method uses the internal API to get the data and saves it to the
     * file directory of the app in a JSON-formatted string-like textfile.
     */
    public void exportData() {

        // Our "parent" which will contain all information;
        JSONObject parentJson = new JSONObject();
        long time = System.currentTimeMillis();

        // Delete the file if read successfully;
        new File(new ContextWrapper(mContext).getFilesDir() + "/" + Configuration.EMERGENCY_FILE).delete();

        AppLogger.print(mClassName, "Starting emergency write of data...", 0);

        try {
            // Get all app contexts;
            for (AppContext context : mAppData.getAppList()) {

                // One for our current app, one for the data of the app
                JSONObject currentApp = new JSONObject();
                JSONObject appData = new JSONObject();

                // Access our small API and get the information needed;
                appData.put("TimeUsed", context.getTimeUsage());
                appData.put("LastChecked", context.getLastChecked());
                appData.put("AppMonitorVersion", this.getVersion());

                AppLogger.print(mClassName, "Starting export for: " + context.getAppName(), 1);

                List<AppModuleMetaData> moduleMetaData = Collections.synchronizedList(getModuleData().getAppModuleData());

                // Get the meta data for all loaded modules;
                for (AppModuleMetaData ammd : moduleMetaData) {
                    // Find our App context;
                    if (ammd.getAppContext() == context) {

                        AppLogger.print(mClassName, "Current Context: " + context.getAppName(), 1);

                        // Iterate through all loaded modules;
                        for (AppModule module : mModules) {

                            // For each module we need to get the data separately;
                            JSONObject appModule = new JSONObject();
                            // Our actual values are stored in this array;
                            JSONArray values = new JSONArray();

                            AppLogger.print(mClassName, "Adding Data for module: " + module.getName(), 1);

                            // Add our data to our array;
                            List<Integer> currentValues = Collections.synchronizedList(ammd.getRawData(module.getIdentifier()));
                            synchronized (currentValues) {
                                synchronized (values) {
                                    for (Integer i : currentValues) {
                                        values.put(i);
                                    }
                                }
                            }

                            // Add the data to our object;
                            appModule.put("Values", values);
                            // Then add the object to our app data;
                            appData.put(module.getIdentifier() + "", appModule);
                        }
                    }
                }
                // Add the real app name as well as the gathered module data;
                currentApp.put(context.getRealAppName(mContext), appData);
                // Add everything to our parent;
                parentJson.put(context.getAppName(), currentApp);

            }
        } catch (JSONException e) {}

        AppLogger.print(mClassName, "Data gathered, writing to disk..", 1);
        // Write the data to our private directory [files];
        try {
            FileOutputStream fos = mContext.openFileOutput(Configuration.EMERGENCY_FILE, Context.MODE_PRIVATE);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 8192);
            try {
                bos.write(parentJson.toString().getBytes());
            } catch (OutOfMemoryError e) {
                AppLogger.print(mClassName, "We tried to save a too large file, forcing cleanup! Exception: " + e, 0);

                for (AppModuleMetaData ammd : getModuleData().getAppModuleData()) {
                    forceCleanUp(ammd.getAppContext().getAppName());
                }
            }
            bos.flush();
            bos.close();
            AppLogger.print(mClassName, "Data successfully written to disk in (" + (System.currentTimeMillis() - time) + " ms).", 0);
        } catch (IOException e) {
            AppLogger.print(mClassName, "Error during data-write..." + e, 0);
        }
    }

    /**
     * If found, imports a saved file and accesses the internal APIs to load
     * the data back in. Its similar to the normal initialization process
     * except it clears all previous data.
     */
    public void importData() {

        ContextWrapper cw = new ContextWrapper(mContext);
        String tmp = null;
        long time = System.currentTimeMillis();

        // Lock the JobManager during this operation;
        this.mSleeping = true;

        if (AeroActivity.genHelper.doesExist(cw.getFilesDir() + "/" + Configuration.EMERGENCY_FILE)) {
            AppLogger.print(mClassName, "Emergency file detected, starting import... ", 0);

            // Read our file and save it in tmp;
            try {
                InputStream is = mContext.openFileInput(Configuration.EMERGENCY_FILE);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                tmp = new String(buffer, "UTF-8");

            } catch (IOException e) {
                AppLogger.print(mClassName, "Error during import... " + e, 0);
                this.mSleeping = false;
                return;
            }
        } else {
            this.mSleeping = false;
            return;
        }

        // Clear the existing data before;
        mAppData.clearData();
        mModules.clear();
        mModules = new ArrayList<AppModule>();
        loadModules();
        // We add our loaded modules;
        this.mAppModuleData = new AppModuleData(getModules());

        this.mAppModuleData.setCleanupEnable(false);
        // Beginning JSON parsing...
        try {
            JSONObject json = new JSONObject(tmp);
            Iterator<?> keys = json.keys();

            // First we get the actual AppName (e.g. com.aero.control);
            while (keys.hasNext()) {
                String tempAppName = keys.next().toString();
                AppLogger.print(mClassName, tempAppName + " : ", 1);

                AppContext localContext = new AppContext(tempAppName);
                mAppData.addContext(localContext);

                JSONObject appParent = json.getJSONObject(tempAppName);
                Iterator<?> appKeys = appParent.keys();

                // Next (and a little bit redundant) we get the real AppName (e.g. Aero Control);
                while (appKeys.hasNext()) {
                    String tempApp = appKeys.next().toString();
                    AppLogger.print(mClassName, tempApp + ": ", 1);
                    JSONObject appData = appParent.getJSONObject(tempApp);
                    Iterator<?> dataKeys = appData.keys();

                    // Finally we get to the data part, first data for our AppContext
                    while (dataKeys.hasNext()) {
                        String tempData = dataKeys.next().toString();

                        // Find module and appcontext data;
                        try {
                            // This is our module data;
                            int i = Integer.parseInt(tempData);
                            JSONObject moduleData = appData.getJSONObject(tempData);
                            Iterator<?> moduleKeys = moduleData.keys();

                            // Get all the data stored inside the arrays of the modules;
                            while (moduleKeys.hasNext()) {
                                String tempModule = moduleKeys.next().toString();
                                ArrayList<Integer> values = new ArrayList<Integer>();

                                // Add all the data to our array list;
                                int length = moduleData.getJSONArray(tempModule).length();
                                for (int j = 0; j < length; j++) {
                                    values.add(Integer.parseInt(moduleData.getJSONArray(tempModule).get(j).toString()));
                                }

                                // Go through our modules and read/save data;
                                for (AppModule module : mModules) {
                                    try {
                                        mAppModuleData.addData(localContext, values, Integer.parseInt(tempData));
                                    } catch (RuntimeException e) {
                                        AppLogger.print(mClassName, "The data for this module was not added, maybe you tried to add data for a non-existing module?", 0);
                                    }
                                }

                                AppLogger.print(mClassName, tempModule + ": " + moduleData.getJSONArray(tempModule), 1);
                            }
                        } catch (NumberFormatException e) {
                            // No problem, these are our AppContext data;
                            AppLogger.print(mClassName, tempData + ": " + appData.get(tempData), 1);

                            if (tempData.equals("TimeUsed")) {
                                localContext.setTimeUsage(appData.getLong(tempData));
                            } else if(tempData.equals("LastChecked")) {
                                localContext.setLastChecked(appData.getLong(tempData));
                            }

                        }
                    }
                }
            }

        } catch (JSONException e) {
            AppLogger.print(mClassName, "Error during json-parsing: " + e, 0);
            this.mSleeping = false;
        }
        this.mSleeping = false;
        this.mAppModuleData.setCleanupEnable(true);
        AppLogger.print(mClassName, "Import of data successful in (" + (System.currentTimeMillis() - time) + " ms).", 0);
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

        // Don't export data on the first load;
        if (mExportThreshold == 0) {
            setExportTimeNow();
        }

        // If we are above the threshold, export data and set a new threshold;
        if (System.currentTimeMillis() > mExportThreshold ) {

            exportData();

            setExportTimeNow();
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

            List<AppModuleMetaData> moduleMetaData = Collections.synchronizedList(this.getModuleData().getAppModuleData());

            for (AppModuleMetaData ammd : moduleMetaData) {
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
        mModules.add(new CPUFreqModule(mContext));

        if (Runtime.getRuntime().availableProcessors() > 1) {
            // If we just have one core, we dont need this module;
            mModules.add(new CPUNumModule(mContext));
        }

        mModules.add(new RAMModule(mContext));

        if (AeroActivity.genHelper.doesExist(FilePath.CPU_TEMP_FILE)) {
            mModules.add(new TEMPModule(mContext));
        }

        for (String s : FilePath.GPU_FILES_RATE) {
            if (AeroActivity.genHelper.doesExist(s)) {
                counter++;
            }
        }
        if (counter > 0) {
            mModules.add(new GPUFreqModule(mContext));
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
     * Sets the current export threshold to now + the configuration parameter (e.g. 1 minute)
     */
    private void setExportTimeNow() {
        mExportThreshold = System.currentTimeMillis() + Configuration.EXPORT_THRESHOLD;
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
                        .setContentTitle(mContext.getText(R.string.app_name))
                        .setContentText(mContext.getText(R.string.notify_app_monitor_data))
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
