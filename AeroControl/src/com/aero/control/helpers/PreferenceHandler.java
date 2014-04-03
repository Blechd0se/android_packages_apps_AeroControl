package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import com.aero.control.AeroActivity;

/**
 * Created by Alexander Christ on 05.03.14.
 */
public class PreferenceHandler {

    public Context mContext;
    public PreferenceCategory mPrefCat;
    public PreferenceManager mPrefMan;

    /*
     * Default constructor to set our objects
     */
    public PreferenceHandler(Context context, PreferenceCategory PrefCat, PreferenceManager PrefMan) {
        this.mContext = context;
        this.mPrefCat = PrefCat;
        this.mPrefMan = PrefMan;
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
    public void generateSettings(final String parameter, String path, final boolean flag) {

        final CustomTextPreference prefload = new CustomTextPreference(mContext);
        // Strings saves the complete path for a given governor;
        final String parameterPath = path + "/" + parameter;
        String summary = AeroActivity.shell.getInfo(parameterPath);

        // Only show numbers in input field;
        prefload.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        // Setup all things we would normally do in XML;
        prefload.setSummary(summary);
        prefload.setTitle(parameter);
        prefload.setText(summary);
        prefload.setDialogTitle(parameter);

        if (prefload.getSummary().equals("Unavailable")) {
            prefload.setEnabled(false);
            prefload.setSummary("This value can't be changed.");
        }

        mPrefCat.addPreference(prefload);

        // Custom OnChangeListener for each element in our list;
        prefload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;
                CharSequence oldValue = prefload.getSummary();

                AeroActivity.shell.setRootInfo(a, parameterPath);

                if (AeroActivity.shell.checkPath(AeroActivity.shell.getInfo(parameterPath), a)) {
                    prefload.setSummary(a);
                } else {
                    Toast.makeText(mContext, "Couldn't set desired parameter" + " Old value; " +
                            AeroActivity.shell.getInfo(parameterPath) + " New Value; " + a, Toast.LENGTH_LONG).show();
                    prefload.setSummary(oldValue);
                }

                // Store our custom preferences if available;
                SharedPreferences preferences = mPrefMan.getSharedPreferences();
                preferences.edit().putString(parameterPath, o.toString()).commit();

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


