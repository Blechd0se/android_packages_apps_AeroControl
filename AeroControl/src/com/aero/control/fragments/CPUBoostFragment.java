package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.PreferenceHandler;


/**
 * Created by Alexander Christ on 19.10.15.
 */
public class CPUBoostFragment extends PlaceHolderFragment {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();

        // Set title
        setTitle(getActivity().getText(R.string.perf_cpu_boost_control).toString());

        // Load our custom preferences;
        loadCPUBoost();
    }

    public void loadCPUBoost() {

        String completeParamterList[] = AeroActivity.shell.getDirInfo(FilePath.CPU_BOOST, true);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.perf_cpu_boost_control);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, FilePath.CPU_BOOST);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

    }
}