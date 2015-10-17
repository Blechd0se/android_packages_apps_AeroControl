package com.aero.control.helpers.PerApp.AppMonitor;

import java.util.ArrayList;
/**
 * Created by Alexander Christ on 30.04.15.
 *
 * Holds the app data in terms for each passed app context.
 * It also increases the time usage as well as the last check time for an app context.
 */
public final class AppData {

    private AppMetaData mMetaData;
    private final String mClassName = getClass().getName();

    public AppData() {
        this.mMetaData = new AppMetaData();
        AppLogger.print(mClassName, "App Data has been initialized!", 0);
    }

    /**
     * Adds the given AppContext to our AppMetaData class, which adds it to our "known" apps list.
     * @param context AppContext, the AppContext which we want to add
     */
    public final void addContext(final AppContext context) {
        AppLogger.print(mClassName, "Trying to add the following app: " + context.getAppName(), 1);
        mMetaData.addToList(context);
    }

    /**
     * Gets the app context based on a appname (e.g. com.aero.control). If its not added to the
     * known apps yet, we create a fresh context here. It also sets all timely based stuff upon
     * call.
     * If one wants just the AppContext, call getSimpleAppContext().
     * @param  appname String, the appname (packagename)
     * @return AppContext
     */
    public final AppContext getAppContext(String appname) {
        return mMetaData.getAppContext(appname);
    }

    /**
     * Gets the app context based on a appname (e.g. com.aero.control). Similar to getAppContext()
     * but without the whole logic, gets just the AppContext or NULL if we don't know about the
     * app yet.
     * @param  appname String, the appname (packagename)
     * @return AppContext
     */
    public final AppContext getSimpleAppContext(final String appname) {
        return mMetaData.getSimpleAppContext(appname);
    }

    /**
     * Gets the current "known" apps inside our data structure.
     * This won't return a NULL value, but the list itself could be
     * empty. Iterating over it is recommended.
     * @return ArrayList<AppContext>, a list containing all known AppContext
     */
    public final ArrayList<AppContext> getAppList() {
        return mMetaData.mAppList;
    }


    /**
     * Clears all available data inside the metadata structure.
     * This should only be called during the import process, when
     * we load data back in.
     */
    public final void clearData() {
        this.mMetaData = new AppMetaData();
    }

    private final class AppMetaData {

        private ArrayList<AppContext> mAppList;
        private final String mClassName = getClass().getName();

        public AppMetaData() {
            this.mAppList = new ArrayList<AppContext>();
        }

        /**
         * Does this AppContext already exist in our list?
         * @param context AppContext to check
         * @return boolean, true if it exists otherwise false
         */
        private boolean existsContext(final AppContext context) {

            int i = 0;

            if (context == null) {
                // Return fast, if the context is null;
                return false;
            }

            for (AppContext ac : mAppList) {
                if (ac.getAppName() != null) {
                    if (ac.getAppName().equals(context.getAppName())) {
                        i++;
                    }
                }
            }

            if (i > 0)
                return true;

            AppLogger.print(mClassName, "Couldn't add the following app, bailing out: " + context.getAppName(), 1);
            return false;
        }

        /**
         * Returns the position where this AppContext is located in our list.
         * @param appname String, the appname (e.g. com.aero.control)
         * @return Integer, can be NULL if no result is found
         */
        private Integer getAppPosition(final String appname) {

            int i = 0;

            for (AppContext ac : mAppList) {
                if (ac.getAppName().equals(appname))
                    return i;

                i++;
            }

            return null;
        }

        /**
         * Returns just the AppContext without the whole logic around it. Used in the GUI.
         * @param appname String, the appname (e.g. com.aero.control)
         * @return AppContext
         */
        public final AppContext getSimpleAppContext(final String appname) {

            AppContext localContext;

            try {
                localContext = mAppList.get(getAppPosition(appname));
            } catch (NullPointerException e) {
                AppLogger.print(mClassName, "We found no match for: " + appname + " we don't know this app yet", 1);
                localContext = null;
            }

            return localContext;
        }

        /**
         * Returns the AppContext from a given appname. First we search in our AppList, if we can't find
         * a match we create a new AppContext and add it to our list. Furthermore we increase the timely
         * usage and set the LastCheckedNow flag.
         * @param appname String, appname (e.g. com.aero.control)
         * @return AppContext, will always return a context
         */
        public final AppContext getAppContext(final String appname) {

            AppContext localContext;

            try {
                localContext = mAppList.get(getAppPosition(appname));
            } catch (NullPointerException e) {
                AppLogger.print(mClassName, "We found no match for: " + appname + " we will add it", 1);

                localContext = new AppContext(appname);
                mAppList.add(localContext);
            }

            localContext.increaseTimeUsage(System.currentTimeMillis() - localContext.getLastChecked());

            // Update the timer for all apps in the list, so we calculate correctly
            for (AppContext ac : mAppList) {
                ac.setLastCheckedNow();
            }
            AppLogger.print(mClassName, "App ("+ appname +") is running for: " + localContext.getTimeUsage() + " ms", 1);

            return localContext;
        }

        /**
         * Adds a AppContext to our AppList if it doesn't already exist there.
         * @param context AppContext to add
         */
        public final void addToList(final AppContext context) {

            if (existsContext(context)) {
                AppLogger.print(mClassName, "App: " + context.getAppName() + " already added.", 1);
                return; // already added to the list
            }

            if (context != null) {
                mAppList.add(context);
            }

            AppLogger.print(mClassName, "App: " + context.getAppName() + " successfully added!", 1);
        }

    }

}
