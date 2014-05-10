package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;

/**
 * Created by Alexander Christ on 09.03.14.
 */
public class GPUGovernorFragment extends PreferenceFragment {

    public PreferenceScreen root;
    public PreferenceCategory PrefCat;
    public static final String GPU_GOV_PATH = "/sys/module/msm_kgsl_core/parameters";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();
        TextView mActionBarTitle = (TextView) getActivity().findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));
        mActionBarTitle.setText(R.string.perf_gpu_gov);

        // Load our custom preferences;
        loadGPUGov();
    }

    public void loadGPUGov() {

        String completeParamterList[] = AeroActivity.shell.getDirInfo(GPU_GOV_PATH, true);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.perf_gpu_gov_settings);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, GPU_GOV_PATH);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

    }
}