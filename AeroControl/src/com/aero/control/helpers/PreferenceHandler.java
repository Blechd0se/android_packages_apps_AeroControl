package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
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

    public void generateSettings(final String parameter, String path) {

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

                return true;
            };
        });
    }

}


