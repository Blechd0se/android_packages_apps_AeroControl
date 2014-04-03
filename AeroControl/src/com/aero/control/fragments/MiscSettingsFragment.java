package com.aero.control.fragments;

import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;

import java.util.Objects;

/**
 * Created by Alexander Christ on 03.04.14.
 */
public class MiscSettingsFragment extends PreferenceFragment {

    public PreferenceScreen root;
    public PreferenceCategory PrefCat;
    public static final String MISC_SETTINGS_PATH = "/sys/devices/virtual/timed_output/vibrator";

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
        PrefCat.setTitle(R.string.perf_misc_settings_vib);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.generateSettings("vtg_level", MISC_SETTINGS_PATH, true);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }
    }
}