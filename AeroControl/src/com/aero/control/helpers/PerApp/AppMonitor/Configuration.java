package com.aero.control.helpers.PerApp.AppMonitor;

/**
 * Created by Alexander Christ on 06.06.15.
 * Stores configuration data of appmonitor
 */
public class Configuration {

    public Configuration() {}

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
    public static final int CLEANUP_THRESHOLD = 5760; // = value * 5 = seconds (default 8 hours = 5760)

    /**
     * The name of the emergency file for automatic backup of the data.
     */
    public static final String EMERGENCY_FILE = "APPMonitorData.json";

}
