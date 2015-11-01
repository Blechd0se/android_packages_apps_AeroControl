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
 * Created by Alexander Christ on 09.03.14.
 */
public class CPUHotplugFragment extends PlaceHolderFragment {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private String mHotplugPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();

        for (String s : FilePath.HOTPLUG_PATH) {
            if (AeroActivity.genHelper.doesExist(s))
                mHotplugPath = s;
        }

        // Set title
        setTitle(getActivity().getText(R.string.perf_cpu_hotplug_driver).toString());

        // Load our custom preferences;
        loadHotplug();
    }

    public void loadHotplug() {

        String completeParamterList[] = AeroActivity.shell.getDirInfo(mHotplugPath, true);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.perf_cpu_hotplug);
        root.addPreference(PrefCat);

        try {

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, mHotplugPath);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

    }
}