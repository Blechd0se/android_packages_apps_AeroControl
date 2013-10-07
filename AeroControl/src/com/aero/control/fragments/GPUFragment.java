package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.shell.shellScripts;

/**
 * Created by ac on 16.09.13.
 */
public class GPUFragment extends PreferenceFragment {

    public static final String GPU_FREQ_MAX = "/proc/gpu/max_rate";
    public static final String GPU_FREQ_CUR = "/proc/gpu/cur_rate";


    shellScripts shell = new shellScripts();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.gpu_fragment);

        PreferenceScreen root = this.getPreferenceScreen();
        // I don't like the following, can we simplify it?

        // Find our ListPreference (max_frequency);
        final ListPreference gpu_control_frequencies = (ListPreference)root.findPreference("gpu_max_freq");
        // Just throw in our frequencies;
        gpu_control_frequencies.setEntries(R.array.gpu_frequency_list);
        gpu_control_frequencies.setEntryValues(R.array.gpu_frequency_list);

        try  {
            gpu_control_frequencies.setValue(shell.getInfoArray(GPU_FREQ_MAX, 1, 0)[0]);
            gpu_control_frequencies.setSummary(shell.getInfoArray(GPU_FREQ_MAX, 0, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            gpu_control_frequencies.setValue("Unavailable");
            gpu_control_frequencies.setSummary("Unavailable");
        }
        gpu_control_frequencies.setDialogIcon(R.drawable.gpu_dark);


        // Set our gpu control flag;
        final CheckBoxPreference gpu_control_enable = (CheckBoxPreference)root.findPreference("gpu_control_enable");


        gpu_control_frequencies.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * Its pretty much the same like on the governor, except we only deal with numbers
                 * Also this should make no problems when the user is using different
                 * Clocks than default...
                 */
                String a = (String) o;
                Toast.makeText(getActivity(), a, Toast.LENGTH_LONG).show();

                shell.setRootInfo(a, GPU_FREQ_MAX);

                gpu_control_frequencies.setSummary(a);

                return true;
            };
        });

        gpu_control_enable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Enabling/Disabling is currently not supported by kernel.", Toast.LENGTH_LONG).show();

                return true;
            };
        });
    }
}
