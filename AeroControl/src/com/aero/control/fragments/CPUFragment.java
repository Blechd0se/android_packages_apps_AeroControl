package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.shell.shellScripts;

/**
 * Created by ac on 03.10.13.
 */
public class CPUFragment extends PreferenceFragment {

    public static final String CPU_AVAILABLE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String ALL_GOV_AVAILABLE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String CURRENT_GOV_AVAILABLE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String CPU_MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String CPU_MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String CPU_GOV_SET_BASE = "/sys/devices/system/cpu/cpufreq/";

    public ListPreference listPref;
    public PreferenceCategory PrefCat;

    shellScripts shell = new shellScripts();

    @Override
    final public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.cpu_fragment);

        final PreferenceScreen root = this.getPreferenceScreen();
        // I don't like the following, can we simplify it?

        // Find our ListPreference (max_frequency);
        final ListPreference max_frequency = (ListPreference) root.findPreference("max_frequency");
        // Just throw in our frequencies;
        max_frequency.setEntries(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        max_frequency.setEntryValues(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        max_frequency.setValue(shell.getInfoArray(CPU_MAX_FREQ, 1, 0)[0]);
        max_frequency.setSummary(shell.getInfoArray(CPU_MAX_FREQ, 1, 0)[0]);
        max_frequency.setDialogIcon(R.drawable.lightning_dark);

        // Find our ListPreference (min_frequency);
        final ListPreference min_frequency = (ListPreference) root.findPreference("min_frequency");
        // Just throw in our frequencies;
        min_frequency.setEntries(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        min_frequency.setEntryValues(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        min_frequency.setValue(shell.getInfoArray(CPU_MIN_FREQ, 1, 0)[0]);
        min_frequency.setSummary(shell.getInfoArray(CPU_MIN_FREQ, 1, 0)[0]);
        min_frequency.setDialogIcon(R.drawable.lightning_dark);


        // Find our ListPreference (governor_settings);
        listPref = (ListPreference) root.findPreference("set_governor");
        // Just throw in our frequencies;
        listPref.setEntries(shell.getInfoArray(ALL_GOV_AVAILABLE, 0, 0));
        listPref.setEntryValues(shell.getInfoArray(ALL_GOV_AVAILABLE, 0, 0));
        listPref.setValue(shell.getInfo(CURRENT_GOV_AVAILABLE));
        listPref.setSummary(shell.getInfo(CURRENT_GOV_AVAILABLE));
        listPref.setDialogIcon(R.drawable.cpu_dark);


        //different listener for each element
        listPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * I need to cast the object to a string first, but setRootInfo
                 * will take the object instead (its casted again there).
                 * The intention behind this is, is to solve the slow UI reaction
                 * and the slow file write process. Otherwise the UI would show the
                 * value _before_ the value actually was changed.
                 */
                String a = (String) o;
                shell.setRootInfo(o, CURRENT_GOV_AVAILABLE);

                // Check if it really sticks;
                if(shell.checkPath(shell.getInfo(CURRENT_GOV_AVAILABLE), a))
                    listPref.setSummary(a);
                else
                    Toast.makeText(getActivity(), "Couldn't set governor."   + " Old value; " +
                            shell.getInfo(CURRENT_GOV_AVAILABLE) + " New Value; " + a, Toast.LENGTH_LONG).show();


                String complete_path = CPU_GOV_SET_BASE + a;

                try {
                    /*
                     * Probably the kernel takes a while to update the dictionaries
                     * and therefore we sleep for a short interval;
                     */
                    try {
                        Thread.currentThread().sleep(250);
                    } catch (InterruptedException e) {
                        Log.e("Aero",
                                "Something interrupted the main Thread, try again.",
                                e);
                    }
                    String completeParamterList[] = shell.getDirInfo(complete_path);

                    // If there are already some entries, kill them all (with fire)
                    if (PrefCat != null)
                        root.removePreference(PrefCat);

                    PrefCat = new PreferenceCategory(getActivity());
                    PrefCat.setTitle("Governor Specific Settings");
                    root.addPreference(PrefCat);

                    handler h = new handler();

                    for (String b : completeParamterList)
                        h.generateSettings(completeParamterList, complete_path);

                    // Probably the wrong place, should be in getDirInfo ?
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), "Looks like there are no parameter for this governor?", Toast.LENGTH_LONG).show();
                    Log.e("Aero",
                            "There isn't any folder i can check. Does this governor has parameters?",
                            e);

                    // To clean up the UI;
                    if (PrefCat != null)
                        root.removePreference(PrefCat);
                }
                return true;
            }

            ;
        });

        max_frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * Its pretty much the same like on the governor, except we only deal with numbers
                 * Also this should make no problems when the user is using different
                 * Clocks than default...
                 */
                String a = (String) o;

                shell.setRootInfo((a.substring(0, a.length() - 4) + "000"), CPU_MAX_FREQ);

                if (shell.checkPath(shell.getInfo(CPU_MAX_FREQ), a))
                    max_frequency.setSummary(shell.toMHz((a.substring(0, a.length() - 4) + "000")));
                else
                    Toast.makeText(getActivity(), "Couldn't set max frequency." + " Old value; " +
                            shell.getInfo(CPU_MAX_FREQ) + " New Value; " + a, Toast.LENGTH_LONG).show();


                return true;
            }

            ;
        });

        min_frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;

                shell.setRootInfo((a.substring(0, a.length() - 4) + "000"), CPU_MIN_FREQ);

                if (shell.checkPath(shell.getInfo(CPU_MIN_FREQ), a))
                    min_frequency.setSummary(shell.toMHz((a.substring(0, a.length() - 4) + "000")));
                else
                    Toast.makeText(getActivity(), "Couldn't set min frequency."  + " Old value; " +
                            shell.getInfo(CPU_MIN_FREQ) + " New Value; " + a, Toast.LENGTH_LONG).show();

                return true;
            }

            ;
        });

    }

    // Make a private class to load all parameters;
    private class handler {

        private int index = 0;

        public void generateSettings(final String parameter[], String path) {

            final GovernorTextPreference prefload = new GovernorTextPreference(getActivity());
            // Strings saves the complete path for a given governor;
            final String parameterPath = path + "/" + parameter[index];

            // Only show numbers in input field;
            prefload.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

            // Setup all things we would normaly do in XML;
            prefload.setSummary(shell.getInfo(parameterPath));
            prefload.setTitle(parameter[index]);
            prefload.setText(shell.getInfo(parameterPath));
            prefload.setDialogTitle(parameter[index]);

            PrefCat.addPreference(prefload);
            index++;

            // Custom OnChangeListener for each element in our list;
            prefload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    String a = (String) o;

                    shell.setRootInfo(a, parameterPath);

                    if (shell.checkPath(shell.getInfo(parameterPath), a))
                        prefload.setSummary(a);
                    else
                        Toast.makeText(getActivity(), "Couldn't set desired parameter"  + " Old value; " +
                                shell.getInfo(parameterPath) + " New Value; " + a, Toast.LENGTH_LONG).show();

                    return true;
                }

                ;
            });

        }
    }

}
