package com.aero.control.helpers.PerApp.AppMonitor;

/**
 * Created by Alexander Christ on 06.06.15.
 * Stores configuration data of appmonitor
 */
public class Configuration {

    public Configuration() {}

    /**
     * The current version of appmonitor. It can be used to distinguish between file formats.
     */
    public static final String APPMONITOR_VERSION = "1.0.0";

    /**
     * The timely threshold for one app context to display data (ms)
     */
    public static final int TIME_THRESHOLD = 60000; // = 1 minutes (60000)

    /**
     * Enables/disables the app logger
     */
    public static final boolean APPLOGGER_ENABLED = true;

    /**
     * Sets the log level (default 0 = only relevant stuff). Set to 3 for full output
     */
    public static final int LOG_LEVEL = 0;

    /**
     * The cleanup threshold. If the usage for one app context is at or above, we cleanup the data.
     */
    public static final int CLEANUP_THRESHOLD = 5760; // = value * 5 = seconds (default 4 hours = 2880)

    /**
     * The name of the emergency file for automatic backup of the data.
     */
    public static final String EMERGENCY_FILE = "APPMonitorData.json";

    /**
     * The interval in milliseconds to export a backup file (e.g. 60000 = 60 seconds)
     */
    public static final int EXPORT_THRESHOLD = 60000;

    /**
     * If true, the import of data will be in a separate thread.
     */
    public static final boolean THREADED_IMPORT = true;
}
