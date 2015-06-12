package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.PreferenceHandler;


/**
 * Created by Alexander Christ on 09.03.14.
 */
public class GPUGovernorFragment extends PlaceHolderFragment {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private TextView mActionBarTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();
        mActionBarTitle = (TextView) getActivity().findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));
        setTitle();

        // Load our custom preferences;
        loadGPUGov();
    }

    public final void setTitle() {
        if (mActionBarTitle != null)
            mActionBarTitle.setText(R.string.perf_gpu_gov);
    }

    public void loadGPUGov() {

        String completeParamterList[] = AeroActivity.shell.getDirInfo(FilePath.GPU_GOV_PATH, true);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.perf_gpu_gov_settings);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, FilePath.GPU_GOV_PATH);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

    }
}