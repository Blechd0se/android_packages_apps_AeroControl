package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Updater Fragment
 */
public class UpdaterFragment extends PreferenceFragment {
    /*
    TODO:
     */

    public static Fragment newInstance(Context context) {
        UpdaterFragment f = new UpdaterFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.updater_fragment);

        PreferenceScreen root = this.getPreferenceScreen();


        final Preference backup_kernel = root.findPreference("backup_kernel");
        final Preference updater_kernel = root.findPreference("updater_kernel");
        final Preference restore_kernel = root.findPreference("restore_kernel");

        // Disable them all;
        backup_kernel.setEnabled(false);
        updater_kernel.setEnabled(false);
        restore_kernel.setEnabled(false);

    }
}
