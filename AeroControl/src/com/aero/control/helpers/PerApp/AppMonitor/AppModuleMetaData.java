package com.aero.control.helpers.PerApp.AppMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 03.05.15.
 *
 * Stores the real gathered data for an app context such as CPU or RAM usage.
 */
public class AppModuleMetaData {

    private final String mClassName = getClass().getName();
    private AppContext mAppContext;
    private List<AppModule> mAppModules;
    private List<List<Integer>> mModules;
    private int mUsage = 0;
    private int mModuleUsage = 0;

    public AppModuleMetaData(AppContext context, List<AppModule> modules) {
        this.mAppContext = context;
        this.mModules = new ArrayList<List<Integer>>();
        this.mAppModules = modules;
        for (AppModule module : modules) {
            mModules.add(new ArrayList<Integer>());
        }
    }

    /**
     * Returns the current AppContext which is stored inside this MetaData-Class
     * @return AppContext, the stored context
     */
    public final AppContext getAppContext() {
        return mAppContext;
    }

    /**
     * Returns true or false depending on the cleanup-state
     * @return boolean, true means it will cleanup the data
     */
    public final boolean readForCleanUp() {
        return (mUsage >= Configuration.CLEANUP_THRESHOLD);
    }

    /**
     * Clears the data for all loaded modules
     */
    public final void cleanUp() {
        this.mUsage = 0;

        int i = 0;

        for (List<Integer> moduleData : mModules) {
            moduleData.clear();

            mModules.get(i).clear();

            i++;
        }

        // Cleanup the module data separately;
        for (AppModule module : mAppModules) {
            module.cleanUp();
        }
    }

    /**
     * Returns the raw array list data for a module (identifier)
     * @param identifier int, the module to get the data
     * @return List<Integer>, all data collected for this module
     */
    public final List<Integer> getRawData(final int identifier) {

        int n = 0;
        List<Integer> rawData = null;

        // Go through all loaded modules data and find the right one;
        for (AppModule modules : mAppModules) {
            if (modules.getIdentifier() == identifier)
                rawData = mModules.get(n);

            n++;
        }

        return rawData;
    }

    /**
     * Adds new data to a given module
     * @param value  Integer, data to add
     * @param module AppModule, for which module should this data be added
     */
    public final void addMetaData(final Integer value, final AppModule module) {

        int i = 0;

        if (value == null)
            return;

        /**
         * Since we call this method for each available module, we want to increase
         * the counter only if we went through all modules;
         */
        if (mModuleUsage >= mAppModules.size()) {
            mModuleUsage = 0;
            // Increase our usage counter;
            mUsage++;
        } else {
            mModuleUsage++;
        }

        // Go through all loaded modules data and find the right one;
        for (AppModule modules : mAppModules) {
            if (module == modules)
                mModules.get(i).add(value);

            i++;
        }
    }

    /**
     * Returns the average of all stored data inside a module (identifier)
     * @param identifier int, module-identifier
     * @return int, the average value
     */
    public final int getAverage(final int identifier) {

        int k = 0;
        List<Integer> tmp = null;

        for (AppModule modules : mAppModules) {
            if (modules.getIdentifier() == identifier)
                tmp = mModules.get(k);

            k++;
        }

        int average = 0;

        if (tmp == null)
            throw new ExceptionHandler(ExceptionHandler.EX_NO_IDENTIFIER_FOUND);

        for (Integer n : tmp) {
            average += n;
        }
        average = average / Math.max(tmp.size(), 1);

        return average;
    }

}
