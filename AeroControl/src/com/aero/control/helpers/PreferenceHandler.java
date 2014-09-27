package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.aero.control.AeroActivity;

import java.io.File;


/**
 * Created by Alexander Christ on 05.03.14.
 */
public class PreferenceHandler {

    public Context mContext;
    public PreferenceCategory mPrefCat;
    public PreferenceManager mPrefMan;

    private SharedPreferences mPreferences;

    /*
     * Default constructor to set our objects
     */
    public PreferenceHandler(Context context, PreferenceCategory PrefCat, PreferenceManager PrefMan) {
        this.mContext = context;
        this.mPrefCat = PrefCat;
        this.mPrefMan = PrefMan;
    }

    /**
     * Generates the preferences for a path
     *
     * @param array     => Contains the parameters in the path/dictionary
     * @param path      => directory (where to look up the files)
     *
     * @return nothing
     */
    public final void genPrefFromDictionary(String[] array, String path) {

        int counter = array.length;
        int i = 0;

        for (String b : array) {
            generateSettings(b, path, false);
            i++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    !(ViewConfiguration.get(mContext).hasPermanentMenuKey())) {
                /* For better KitKat+ looks; */
                if (i == counter) {
                    Preference blankedPref = new Preference(mContext);
                    blankedPref.setSelectable(false);
                    mPrefCat.addPreference(blankedPref);
                }
            }
        }
    }

    /**
     * Gets a file from a given path and adds a Preference on top of it
     *
     * @param nameArray     => Array which contains the file names
     * @param paraArray     => Array which contains the file path (without name)
     * @param showEmpty     => Should we show a empty preference at the end?
     *
     * @return nothing
     */
    public final void genPrefFromFiles(String[] nameArray, String[] paraArray, Boolean showEmpty) {

        int counter = nameArray.length;
        int i = 0;

        for (int j = 0; j < nameArray.length; j++) {

            //TODO: Move this into the parent class
            if (nameArray[j].equals("vtg_level") || nameArray[j].equals("amp"))
                generateSettings(nameArray[j], paraArray[j], true);
            else
                generateSettings(nameArray[j], paraArray[j], false);
            
            i++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    !(ViewConfiguration.get(mContext).hasPermanentMenuKey())
                    && showEmpty) {
                /* For better KitKat+ looks; */
                if (i == counter) {
                    Preference blankedPref = new Preference(mContext);
                    blankedPref.setSelectable(false);
                    mPrefCat.addPreference(blankedPref);
                }
            }
        }
    }

    /**
     * Gets a file from a given path and adds a Preference on top of it
     *
     * @param path     => Generates a single preference from a complete path
     *
     * @return nothing
     */
    public final void genPrefFromSingleFile(String path) {

        String[] array = path.split("/");
        String paraName = "";
        int i = 0;

        for (String a : array) {
            if (array.length - 1 == i)
                paraName = a;
            i++;
        }
        path = path.replace("/" + paraName, "");

        generateSettings(paraName, path, false);
    }

    /**
     * Gets all files in a given dictionary and adds Preferences on top of them
     *
     * @param parameter     => actual file to read/write
     * @param path          => directory (where to look up the file)
     * @param flag          => force vibration after change
     *
     * @return nothing
     */
    private void generateSettings(final String parameter, final String path, final boolean flag) {

        final CustomTextPreference prefload = new CustomTextPreference(mContext);
        // Strings saves the complete path for a given governor;
        final String parameterPath = path + "/" + parameter;
        final String summary = AeroActivity.shell.getInfo(parameterPath);
        final File checkFile = new File(parameterPath);

        // If the file doesn't exist, no need to waste time;
        if (!(checkFile.exists()))
            return;

        Integer tmp = null;
        try {
            tmp = Integer.parseInt(summary);
        } catch (NumberFormatException e) {
            // Do nothing
        }

        // Only show numbers in input field if its a number;
        if (tmp != null)
            prefload.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        mPreferences = mPrefMan.getSharedPreferences();

        // If no entry exists, we can be sure that we won't have to check the checkbox;
        if (mPreferences.getString(parameterPath, null) != null) {
            prefload.setChecked(true);
        }

        // Setup all things we would normally do in XML;
        prefload.setPrefSummary(summary);
        prefload.setTitle(parameter);
        prefload.setText(summary);
        prefload.setPrefText(parameter);
        prefload.setDialogTitle(parameter);
        prefload.setName(parameterPath);

        if (prefload.getPrefSummary().equals("Unavailable")) {
            prefload.setEnabled(false);
            prefload.setPrefSummary("This value can't be changed.");
        }

        mPrefCat.addPreference(prefload);

        // Custom OnChangeListener for each element in our list;
        prefload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;
                CharSequence oldValue = prefload.getPrefSummary();

                AeroActivity.shell.setRootInfo(a, parameterPath);

                if (AeroActivity.shell.checkPath(AeroActivity.shell.getInfo(parameterPath), a)) {
                    prefload.setPrefSummary(a);
                } else {
                    Toast.makeText(mContext, "Couldn't set desired parameter" + " Old value; " +
                            AeroActivity.shell.getInfo(parameterPath) + " New Value; " + a, Toast.LENGTH_LONG).show();
                    prefload.setPrefSummary(oldValue);
                }


                if (prefload.isChecked() == true) {
                    // Store our custom preferences if available;

                    mPreferences.edit().putString(parameterPath, o.toString()).commit();
                }

                if (flag)
                    forceVibration();

                return true;
            };
        });
    }

    public void forceVibration() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.e("Aero", "Something interrupted the main Thread, try again.", e);
        }

        Vibrator vibrate = (Vibrator)mContext.getSystemService(mContext.VIBRATOR_SERVICE);

        vibrate.vibrate(500);
    }

}


