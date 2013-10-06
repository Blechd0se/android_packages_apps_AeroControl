package com.aero.control.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.aero.control.R;

/**
 * Created by root on 21.09.13.
 */
public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preference);

        // Get name;
        getActivity().setTitle(R.string.aero_settings);


    }

}