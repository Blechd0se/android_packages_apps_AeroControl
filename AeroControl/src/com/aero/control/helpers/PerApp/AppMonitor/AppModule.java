package com.aero.control.helpers.PerApp.AppMonitor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Christ on 03.05.15.
 *
 * Dummy class for modules which holds the data
 */
public class AppModule {

    private String mName;
    private final String mClassName = getClass().getName();
    private List<Integer> mValues;
    private Integer mModuleIdentifier;
    private String mSuffix = "";
    private String mPrefix = "";
    private Context mContext;
    private Drawable mDrawable;

    public static final int MODULE_CPU_FREQ_IDENTIFIER = 10;
    public static final int MODULE_CPU_NUM_IDENTIFIER = 20;
    public static final int MODULE_RAM_IDENTIFIER = 30;
    public static final int MODULE_TEMP_IDENTIFIER = 40;
    public static final int MODULE_GPU_IDENTIFIER = 50;

    public AppModule(Context context) {
        this.mName = mClassName;
        this.mValues = new ArrayList<Integer>();
        this.mContext = context;
        AppLogger.print(mClassName, "App Module initialized", 0);
    }

    /**
     * Sets the module identifier for this module
     * @param identifier int, module identifier
     */
    protected final void setIdentifier(final int identifier) {
        if (this.mModuleIdentifier != null)
            throw new ExceptionHandler(ExceptionHandler.EX_IDENTIFIER_ALREADY_DEFINED);
        this.mModuleIdentifier = identifier;
    }

    /**
     * Returns the current module identifier of this module
     * @return int, the current identifier
     */
    public final int getIdentifier() {
        return mModuleIdentifier;
    }

    /**
     * Sets a suffix for this module. Its later used in the GUI (e.g. "MB").
     * @param suffix String, a suffix to set.
     */
    protected final void setSuffix(final String suffix) {
        this.mSuffix = suffix;
    }

    /**
     * Returns the current prefix of this module. Used in the GUI.
     * @return String
     */
    public final String getPrefix() {
        return mPrefix;
    }


    /**
     * Sets a drawable for the module (e.g. logo) for the GUI.
     * @param drawable Drawable, the actual drawable
     */
    public final void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }

    /**
     * Returns the the current Drawable of the module. This could be NULL if
     * the module doesn't have a drawable.
     * @return Drawable
     */
    public final Drawable getDrawable() {
        return mDrawable;
    }

    /**
     * Sets a prefix for this module. Its later used in the GUI (e.g. "CPU FREQ").
     * @param prefix String, a suffix to set.
     */
    protected final void setPrefix(final String prefix) {
        this.mPrefix = prefix;
    }

    /**
     * Sets a prefix for this module. Its later used in the GUI (e.g. "CPU frequency").
     * @param charSequence CharSequence, a suffix to set.
     */
    protected final void setPrefix(final CharSequence charSequence) {
        setPrefix(charSequence.toString());
    }

    /**
     * Returns the current suffix of this module. Used in the GUI.
     * @return String
     */
    public final String getSuffix() {
        return mSuffix;
    }

    /**
     * Gets the module name of this module (e.g. "CPUFreqModule).
     * @return String
     */
    protected final String getName() {
        return mName;
    }

    /**
     * Sets the module name for this module. Usually the classname.
     * @param name String, the new name of the module
     */
    protected final void setName(final String name) {
        this.mName = name;
    }

    /**
     * Adds a value to the module data list.
     * @param value Integer
     */
    protected final void addValues(final Integer value) {
        mValues.add(value);
        AppLogger.print(mClassName, "Value added to module: " + value, 1);
    }

    /**
     * Returns the current list of integers(values) for this module (e.g. "1000, 1200, 900")
     * @return List<Integer>
     */
    protected final List<Integer> getValues() {
        return mValues;
    }

    /**
     * Returns the last added value from the module data list.
     * @return Integer
     */
    protected final Integer getLastValue() {
        if (mValues.size() > 0)
            return mValues.get(mValues.size() - 1);
        else
            return null;
    }

    /**
     * Clears the module data list of this module.
     */
    protected final void cleanUp() {
        this.mValues.clear();
        this.mValues = new ArrayList<Integer>();
    }

    /**
     * Main method of this class (and all its subclasses). It handles the real action
     * inside the module.
     */
    protected void operate() {
        if (mName == null)
            throw new ExceptionHandler("This module has no name, please enter a name!");
    }

}
