package com.aero.control.helpers.PerApp.AppMonitor;

import android.util.Log;

/**
 * Created by Alexander Christ on 30.04.15.
 *
 * A small yet simple logger mechanism for the AppMonitor
 */
public class AppLogger {

    public AppLogger() {}

    /**
     * Prints a message to logcat depending on the log-level. If the passed
     * loglevel is above the loggerlevel we won't display the message.
     * @param tag String, a tag (usually the classname)
     * @param message String, the real message we want to print
     * @param level  int, the target loglevel
     */
    public static void print(final String tag, final String message, final int level) {
        if (Configuration.APPLOGGER_ENABLED) {
            // Do we want to show this message?
            if (level <= Configuration.LOG_LEVEL)
                Log.e(tag, message);
        }
    }

    /**
     * Returns the current LogLevel.
     * @return int, (0 = deepest level)
     */
    public static int getLogLevel() {
        return Configuration.LOG_LEVEL;
    }

}
