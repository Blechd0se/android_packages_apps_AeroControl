package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;

import java.util.ArrayList;

/**
 * Created by Alexander Christ on 03.04.14.
 */
public class MiscSettingsFragment extends PreferenceFragment {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private ArrayList<String> mParaList;
    private ArrayList<String> mNameList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();

        // Load parameter data:
        loadParalist();

        // Load our custom preferences;
        loadSettings();
    }

    private void loadParalist() {

        mParaList = new ArrayList<String>();
        mNameList = new ArrayList<String>();

        mNameList.add("vtg_level");
        mParaList.add(AeroActivity.files.MISC_VIBRATOR_CONTROL);

        mNameList.add("amp");
        mParaList.add(AeroActivity.files.MISC_VIBRATOR_CONTROL);

        mNameList.add("temp_threshold");
        mParaList.add(AeroActivity.files.MISC_THERMAL_CONTROL);
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

            h.genPrefFromFiles(mNameList.toArray(new String[0]), mParaList.toArray(new String[0]), false);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }
    }
}