package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;

/**
 * Created by Alexander Christ on 03.04.14.
 */
public class MiscSettingsFragment extends PreferenceFragment {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private static final String MISC_VIBRATOR_CONTROL = "/sys/devices/virtual/timed_output/vibrator";
    private static final String MISC_THERMAL_CONTROL = "/sys/module/msm_thermal/parameters";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();

        // Load our custom preferences;
        loadSettings();
    }

    public void loadSettings() {

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.pref_misc_settings);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            String[][] array = new String[][] {
                    {"vtg_level", MISC_VIBRATOR_CONTROL},
                    {"temp_threshold", MISC_THERMAL_CONTROL}
            };
            h.genPrefFromFiles(array);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }
    }
}