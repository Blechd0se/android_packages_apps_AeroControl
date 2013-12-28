package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.helpers.shellHelper;

/**
 * Created by ac on 16.09.13.
 */
public class GPUFragment extends PreferenceFragment {

    public static final String GPU_FREQ_MAX = "/sys/kernel/gpu_control/max_freq";
    public static final String GPU_CONTROL_ACTIVE = "/sys/kernel/gpu_control/gpu_control_active";
    public static final String GPU_FREQ_CUR = "/proc/gpu/cur_rate";
    public static final String DISPLAY_COLOR ="/sys/class/misc/display_control/display_brightness_value";
    public boolean checkGpuControl;

    shellHelper shell = new shellHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.gpu_fragment);

        PreferenceScreen root = this.getPreferenceScreen();
        // I don't like the following, can we simplify it?

        // Find our ListPreference (max_frequency);
        final ListPreference gpu_control_frequencies = (ListPreference)root.findPreference("gpu_max_freq");
        // Set our gpu control flag;
        final CheckBoxPreference gpu_control_enable = (CheckBoxPreference)root.findPreference("gpu_control_enable");
        final ListPreference display_control = (ListPreference)root.findPreference("display_control");


        if (shell.getInfo(DISPLAY_COLOR).equals("Unavailable"))
            display_control.setEnabled(false);

        // Get our strings;
        CharSequence[] display_entries = {
                getText(R.string.defy_red_colors),
                getText(R.string.defy_green_colors),
                getText(R.string.defy_energy_saver)
        };
        CharSequence[] display_values = {"31", "9", "0"};
        display_control.setEntries(display_entries);
        display_control.setEntryValues(display_values);

        // Just throw in our frequencies;
        gpu_control_frequencies.setEntries(R.array.gpu_frequency_list);
        gpu_control_frequencies.setEntryValues(R.array.gpu_frequency_list_values);

        try  {
            gpu_control_frequencies.setValue(shell.getInfoArray(GPU_FREQ_MAX, 1, 0)[0]);
            gpu_control_frequencies.setSummary(shell.toMHz((shell.getInfoArray(GPU_FREQ_MAX, 0, 0)[0].substring(0,
                    shell.getInfoArray(GPU_FREQ_MAX, 0, 0)[0].length() - 3))));

            // Check if enabled or not;
            if (shell.getInfo(GPU_CONTROL_ACTIVE).equals("1"))
                checkGpuControl = true;
            else
                checkGpuControl = false;

            gpu_control_enable.setChecked(checkGpuControl);

        } catch (ArrayIndexOutOfBoundsException e) {
            /*
             * If the folder is missing, disable this feature completely;
             */
            gpu_control_frequencies.setValue("Unavailable");
            gpu_control_frequencies.setSummary("Unavailable");
            gpu_control_frequencies.setEnabled(false);
            gpu_control_enable.setEnabled(false);

            Toast.makeText(getActivity(), "GPU Control is not supported with your kernel.", Toast.LENGTH_LONG).show();
        }
        gpu_control_frequencies.setDialogIcon(R.drawable.gpu_dark);

        gpu_control_frequencies.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * Its pretty much the same like on the governor, except we only deal with numbers
                 * Also this should make no problems when the user is using different
                 * Clocks than default...
                 */
                String a = (String) o;

                shell.setRootInfo(a, GPU_FREQ_MAX);

                // Sleep the thread again for UI delay;
                try {
					Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e("Aero",
                          "Something interrupted the main Thread, try again.",
                           e);
                }

                gpu_control_frequencies.setSummary(shell.toMHz((a.substring(0, a.length() - 3))));

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        gpu_control_enable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                String a =  o.toString();

                if (a.equals("true"))
                    shell.setRootInfo("1", GPU_CONTROL_ACTIVE);
                else if (a.equals("false"))
                    shell.setRootInfo("0", GPU_CONTROL_ACTIVE);

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        display_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;

                // Get Permissions first, then execute;
                String[] commands = new String[]
                        {
                                "chmod 0664 " + DISPLAY_COLOR,
                                "echo " + a + " > " + DISPLAY_COLOR,
                        };
                shell.setRootInfo(commands);

                Toast.makeText(getActivity(), "Turn your display off/on :)", Toast.LENGTH_LONG).show();

                return true;
            }
        });
    }
}
