package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;

import java.io.File;
/**
 * Created by ac on 16.09.13.
 */
public class GPUFragment extends PreferenceFragment {

    public static final String GPU_FREQ_MAX = "/sys/kernel/gpu_control/max_freq";
    public static final String GPU_CONTROL_ACTIVE = "/sys/kernel/gpu_control/gpu_control_active";
    public static final String GPU_FREQ_CUR = "/proc/gpu/cur_rate";
    public static final String DISPLAY_COLOR ="/sys/class/misc/display_control/display_brightness_value";
    public static final String GPU_FREQ_NEXUS4 = "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
    public static final String GPU_FREQ_NEXUS4_VALUES = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
    public static final String SWEEP2WAKE = "/sys/android_touch/sweep2wake";
    public static final String COLOR_CONTROL = "/sys/devices/platform/kcal_ctrl.0/kcal";
    public boolean checkGpuControl;
    public boolean checkSweep2wake;
    public String[] mColorValues;

    public String gpu_file;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.gpu_fragment);

        PreferenceScreen root = this.getPreferenceScreen();
        PreferenceCategory gpuCategory = (PreferenceCategory) findPreference("gpu_settings");
        // I don't like the following, can we simplify it?

        // Set our gpu control flag;
        final CheckBoxPreference gpu_control_enable = (CheckBoxPreference)root.findPreference("gpu_control_enable");
        final CheckBoxPreference sweep2wake = (CheckBoxPreference)root.findPreference("sweeptowake");
        // Find our ListPreference (max_frequency);
        final ListPreference gpu_control_frequencies = (ListPreference)root.findPreference("gpu_max_freq");
        final ListPreference display_control = (ListPreference)root.findPreference("display_control");
        final Preference color_control = root.findPreference("display_color_control");

        if(!(new File(SWEEP2WAKE).exists()))
            gpuCategory.removePreference(sweep2wake);

        if(!(new File(GPU_CONTROL_ACTIVE).exists()))
                gpuCategory.removePreference(gpu_control_enable);

        if (!(new File(GPU_FREQ_MAX).exists() || new File(GPU_FREQ_NEXUS4).exists()))
            gpuCategory.removePreference(gpu_control_frequencies);

        if (!(new File(COLOR_CONTROL).exists()))
            gpuCategory.removePreference(color_control);

        // Check for nexus;
        if (new File(GPU_FREQ_NEXUS4).exists())
            gpu_file = GPU_FREQ_NEXUS4;
        else
            gpu_file = GPU_FREQ_MAX;

        if (AeroActivity.shell.getInfo(DISPLAY_COLOR).equals("Unavailable"))
            gpuCategory.removePreference(display_control);

        final Preference gpu_gov_settings = root.findPreference("gpu_gov_settings");
        if (new File("/sys/module/msm_kgsl_core/parameters").exists()) {
            gpu_gov_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .replace(R.id.content_frame, new GPUGovernorFragment())
                            .addToBackStack("GPU Governor")
                            .commit();

                    return true;
                }
            });
        } else {
            gpuCategory.removePreference(gpu_gov_settings);
        }

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
        if (gpu_file.equals(GPU_FREQ_NEXUS4)) {
            gpu_control_frequencies.setEntries(AeroActivity.shell.getInfoArray(GPU_FREQ_NEXUS4_VALUES, 1, 0));
            gpu_control_frequencies.setEntryValues(AeroActivity.shell.getInfoArray(GPU_FREQ_NEXUS4_VALUES, 0, 0));
        } else {
            gpu_control_frequencies.setEntries(R.array.gpu_frequency_list);
            gpu_control_frequencies.setEntryValues(R.array.gpu_frequency_list_values);
        }

        try  {
            gpu_control_frequencies.setValue(AeroActivity.shell.getInfoArray(gpu_file, 0, 0)[0]);
            gpu_control_frequencies.setSummary(AeroActivity.shell.toMHz((AeroActivity.shell.getInfoArray(gpu_file, 0, 0)[0].substring(0,
                    AeroActivity.shell.getInfoArray(gpu_file, 0, 0)[0].length() - 3))));

            // Check if enabled or not;
            if (AeroActivity.shell.getInfo(GPU_CONTROL_ACTIVE).equals("1"))
                checkGpuControl = true;
            else
                checkGpuControl = false;

            // Check if enabled or not;
            if (AeroActivity.shell.getInfo(SWEEP2WAKE).equals("1"))
                checkSweep2wake = true;
            else
                checkSweep2wake = false;

            sweep2wake.setChecked(checkSweep2wake);
            gpu_control_enable.setChecked(checkGpuControl);

        } catch (ArrayIndexOutOfBoundsException e) {
            /*
             * If the folder is missing, disable this feature completely;
             */
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

                Log.e("Aero", "output: " + a);

                AeroActivity.shell.setRootInfo(a, gpu_file);

                // Sleep the thread again for UI delay;
                try {
					Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e("Aero",
                          "Something interrupted the main Thread, try again.",
                           e);
                }

                gpu_control_frequencies.setSummary(AeroActivity.shell.toMHz((a.substring(0, a.length() - 3))));

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
                    AeroActivity.shell.setRootInfo("1", GPU_CONTROL_ACTIVE);
                else if (a.equals("false"))
                    AeroActivity.shell.setRootInfo("0", GPU_CONTROL_ACTIVE);

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
                final String[] commands = new String[]
                        {
                                "chmod 0664 " + DISPLAY_COLOR,
                                "echo " + a + " > " + DISPLAY_COLOR,
                        };
                AeroActivity.shell.setRootInfo(commands);

                Toast.makeText(getActivity(), "Turn your display off/on :)", Toast.LENGTH_LONG).show();

                return true;
            }
        });

        sweep2wake.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String a =  o.toString();

                if (a.equals("true"))
                    AeroActivity.shell.setRootInfo("1", SWEEP2WAKE);
                else if (a.equals("false"))
                    AeroActivity.shell.setRootInfo("0", SWEEP2WAKE);

                //** store preferences
                preference.getEditor().commit();

                return true;
            }
        });

        color_control.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                mColorValues = AeroActivity.shell.getInfoArray(COLOR_CONTROL, 0, 0);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.gpu_color_control, null);
                final SeekBar redValues = (SeekBar)layout.findViewById(R.id.redValues);
                final SeekBar greenValues = (SeekBar)layout.findViewById(R.id.greenValues);
                final SeekBar blueValues = (SeekBar)layout.findViewById(R.id.blueValues);

                final EditText redValue = (EditText)layout.findViewById(R.id.redValue);
                final EditText greenValue = (EditText)layout.findViewById(R.id.greenValue);
                final EditText blueValue = (EditText)layout.findViewById(R.id.blueValue);

                redValues.setProgress(Integer.parseInt(mColorValues[0]));
                greenValues.setProgress(Integer.parseInt(mColorValues[1]));
                blueValues.setProgress(Integer.parseInt(mColorValues[2]));

                redValue.setText(mColorValues[0]);
                greenValue.setText(mColorValues[1]);
                blueValue.setText(mColorValues[2]);

                redValues.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        redValue.setText("" + i);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                greenValues.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        greenValue.setText("" + i);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                blueValues.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        blueValue.setText("" + i);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


                builder.setTitle(R.string.pref_display_color);

                builder.setView(layout)
                        .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                if (Integer.parseInt(redValue.getText().toString()) > 255
                                        || Integer.parseInt(greenValue.getText().toString()) > 255
                                        || Integer.parseInt(blueValue.getText().toString()) > 255 ) {
                                    Toast.makeText(getActivity(), "The values are out of range!", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                String rgbValues = redValue.getText() + " " + greenValue.getText() + " " + blueValue.getText();
                                AeroActivity.shell.setRootInfo(rgbValues, COLOR_CONTROL);

                                //** store preferences
                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                                preference.edit().putString("rgbValues", rgbValues).commit();

                            }
                        })
                        .setNegativeButton(R.string.maybe_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                builder.show();

                return false;
            }
        });
    }
}
