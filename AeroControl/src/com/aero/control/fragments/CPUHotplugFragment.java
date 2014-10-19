package com.aero.control.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
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
public class CPUHotplugFragment extends PreferenceFragment {

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
        loadHotplug();
    }

    public final void setTitle() {
        if (mActionBarTitle != null)
            mActionBarTitle.setText(R.string.perf_cpu_hotplug_driver);
    }

    public void loadHotplug() {

        String completeParamterList[] = AeroActivity.shell.getDirInfo(FilePath.HOTPLUG_PATH, true);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.perf_cpu_hotplug);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, FilePath.HOTPLUG_PATH);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

    }
}