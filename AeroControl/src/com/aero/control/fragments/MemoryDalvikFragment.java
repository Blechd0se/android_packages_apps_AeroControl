package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;

/**
 * Created by Alexander Christ on 05.03.14.
 */
public class MemoryDalvikFragment extends PreferenceFragment {

    public MemoryDalvikFragment mMemoryDalvikFragment;
    public PreferenceScreen root;
    public PreferenceCategory PrefCat;
    public static final String DALVIK_TWEAK = "/proc/sys/vm";

    public Fragment newInstance(Context context) {
        mMemoryDalvikFragment = new MemoryDalvikFragment();

        return mMemoryDalvikFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.dalvik_fragment);
        root = this.getPreferenceScreen();

        // Load our custom preferences;
        loadDalvik();
    }

    public void loadDalvik() {

        String completeParamterList[] = AeroActivity.shell.getDirInfo(DALVIK_TWEAK, true);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.pref_dalvik_setttings);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            for (String b : completeParamterList)
                h.generateSettings(b, DALVIK_TWEAK);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

    }
}