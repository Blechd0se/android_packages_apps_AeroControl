package com.aero.control.helpers.PerApp.AppMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 03.05.15.
 * Adds[cleanups] data to our modules and holds the module data.
 */
public class AppModuleData {

    private List<AppModuleMetaData> mAppModuleData;
    private List<AppModule> mModules;
    private final String mClassName = getClass().getName();
    private boolean mCleanUpEnabled = true;

    public AppModuleData(List<AppModule> modules) {
        this.mAppModuleData= new ArrayList<AppModuleMetaData>();
        this.mModules = modules;
        AppLogger.print(mClassName, "AppModuleData successfully initialized!", 0);
    }

    /**
     * Checks if we already have meta information about this app context
     * and returns the AppModuleMetaData
     * @param context AppContext, the context that will be checked
     */
    public final AppModuleMetaData existsAppModuleMetaData(final AppContext context) {

        for (AppModuleMetaData ammd : mAppModuleData) {

            // Do we have a winner?
            if (ammd.getAppContext() == context) {
                return ammd;
            }
        }

        return null;
    }

    /**
     * Returns the current AppModuleData.
     * @return List<AppModuleMetaData>
     */
    public final List<AppModuleMetaData> getAppModuleData() {
        return mAppModuleData;
    }

    /**
     * Allows to enable/disable the cleanup routines in general. This should be only used
     * during the import of data.
     * @param enable boolean
     */
    public final void setCleanupEnable(boolean enable) {
        this.mCleanUpEnabled = enable;
    }

    /**
     * Simple wrapper-method around the real addData() to add multiple values in one call
     * @param context     AppContext, context where we will add the data
     * @param values      ArrayList<Integer>, the values to add
     * @param identifier  Integer, the module identifier
     */
    public void addData(final AppContext context, final ArrayList<Integer> values, final Integer identifier) {

        AppModule targetModule = null;

        for (AppModule module : mModules) {
            if (module.getIdentifier() == identifier) {
                // Winner!
                targetModule = module;
            }
        }

        if (targetModule != null) {
            for (Integer i : values) {
                addData(context, i, targetModule);
            }
        } else {
            throw new ExceptionHandler(ExceptionHandler.EX_MODULE_NOT_FOUND +  " (" + identifier + ")");
        }

    }

    /**
     * Adds data for our app context from a source and checks if we haven't yet build
     * up the MetaData information.
     * @param context AppContext, context where we will add the data
     * @param value   Integer, the value to add
     * @param module  AppModule, for which module do we add the data
     */
    public void addData(final AppContext context, final Integer value, final AppModule module) {

        if (value == null) {
            return;
        }

        AppModuleMetaData appmetadata = existsAppModuleMetaData(context);
        if (appmetadata == null) {
            appmetadata = new AppModuleMetaData(context, mModules);
            AppLogger.print(mClassName, "Adding a new meta data module for: " + context.getAppName(), 0);
            mAppModuleData.add(appmetadata);
        }
        checkForCleanup(appmetadata, context);

        appmetadata.addMetaData(value, module);
    }

    /**
     * Checks the app context if we are above our data storage threshold to prevent
     * old data from being taken into account.
     * Basically a cleanup.
     * @param appmetadata AppModuleMetaData, the MetaData module where we want to do our cleanup
     * @param context     AppContext, the context of the app which we want to cleanup
     */
    private void checkForCleanup(final AppModuleMetaData appmetadata, final AppContext context) {

        // CleanUp only if we are ready and its enabled;
        if (mCleanUpEnabled && appmetadata.readForCleanUp()) {
            // Cleanup not only our data, but also the app context and module data;
            AppLogger.print(mClassName, "Cleaning up the found data for: " + context.getAppName(), 0);
            appmetadata.cleanUp();
            context.cleanUp();
        }
    }
}
