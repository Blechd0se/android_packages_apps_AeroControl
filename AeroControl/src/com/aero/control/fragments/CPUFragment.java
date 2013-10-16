package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.helpers.shellHelper;

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
    public static final String CPU_VSEL = "/proc/overclock/mpu_opps";
    public static final String CPU_MAX_RATE = "/proc/overclock/max_rate";

    public PreferenceCategory PrefCat;
    public ListPreference listPref;
    public ListPreference min_frequency;
    public ListPreference max_frequency;

    shellHelper shell = new shellHelper();



    @Override
    final public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.cpu_fragment);

        final PreferenceScreen root = this.getPreferenceScreen();
        final String overclockOutput = shell.getRootInfo("cat", CPU_VSEL);

        // I don't like the following, can we simplify it?

        // Find our ListPreference (max_frequency);
        max_frequency = (ListPreference) root.findPreference("max_frequency");
        updateMaxFreq();
        max_frequency.setDialogIcon(R.drawable.lightning_dark);

        // Find our ListPreference (min_frequency);
        min_frequency = (ListPreference) root.findPreference("min_frequency");
        updateMinFreq();
        min_frequency.setDialogIcon(R.drawable.lightning_dark);

        final Preference cpu_oc_uc = (Preference) root.findPreference("cpu_oc_uc");
        if (overclockOutput.equals("Unavailable"))
            cpu_oc_uc.setEnabled(false);

        cpu_oc_uc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Set up Alert Dialog and view;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.cpu_oc_uc, null);


                // Set up our EditText fields;
                final EditText value1 = (EditText) layout.findViewById(R.id.value1);
                final EditText value2 = (EditText) layout.findViewById(R.id.value2);
                final EditText value3 = (EditText) layout.findViewById(R.id.value3);
                final EditText value4 = (EditText) layout.findViewById(R.id.value4);
                final EditText value5 = (EditText) layout.findViewById(R.id.value5);
                final EditText value6 = (EditText) layout.findViewById(R.id.value6);
                final EditText value7 = (EditText) layout.findViewById(R.id.value7);
                final EditText value8 = (EditText) layout.findViewById(R.id.value8);

                // Left side (cpu frequencies);
                value1.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[0]);
                value3.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[1]);
                value5.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[2]);
                value7.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[3]);

                // Substring is not ideal, but it gets the job done;
                value2.setText(overclockOutput.substring(42, 44));
                value4.setText(overclockOutput.substring(104, 106));
                value6.setText(overclockOutput.substring(166, 168));
                value8.setText(overclockOutput.substring(228, 230));


                    // Inflate and set the layout for the dialog
                    // Pass null as the parent view because its going in the dialog layout
                builder.setView(layout)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // save the current screen;
                            Object a = 4 + " " + (value1.getText().toString().substring(0, value1.getText().toString().length() - 4) + "000000") + " " + value2.getText();
                            Object b = 3 + " " + (value3.getText().toString().substring(0, value3.getText().toString().length() - 4) + "000000") + " " + value4.getText();
                            Object c = 2 + " " + (value5.getText().toString().substring(0, value5.getText().toString().length() - 4) + "000000") + " " + value6.getText();
                            Object d = 1 + " " + (value7.getText().toString().substring(0, value7.getText().toString().length() - 4) + "000000") + " " + value8.getText();
                            // Object for max_rate;
                            Object f = (value1.getText().toString().substring(0, value1.getText().toString().length() - 4) + "000");

                            // Debug info;
                            Log.e("Aero: ", a.toString());
                            Log.e("Aero: ", b.toString());
                            Log.e("Aero: ", c.toString());
                            Log.e("Aero: ", d.toString());

                            // Set our values
                            shell.setRootInfo(a, CPU_VSEL);
                            shell.setRootInfo(f, CPU_MAX_RATE);
                            shell.setRootInfo(b, CPU_VSEL);
                            shell.setRootInfo(c, CPU_VSEL);
                            shell.setRootInfo(d, CPU_VSEL);


                            // Left side (cpu frequencies);
                            value1.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[0]);
                            value3.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[1]);
                            value5.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[2]);
                            value7.setText(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0)[3]);

                            // Set voltages;
                            value2.setText(shell.getRootInfo("cat", CPU_VSEL).substring(42, 44));
                            value4.setText(shell.getRootInfo("cat", CPU_VSEL).substring(104, 106));
                            value6.setText(shell.getRootInfo("cat", CPU_VSEL).substring(166, 168));
                            value8.setText(shell.getRootInfo("cat", CPU_VSEL).substring(228, 230));

                            // Before we update anything in the UI, sleep a while;
                            try {
                                Thread.currentThread().sleep(250);
                            } catch (InterruptedException e) {
                                Log.e("Aero",
                                        "Something interrupted the main Thread, try again.",
                                        e);
                            }
                            updateMinFreq();
                            updateMaxFreq();

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                                    //LoginDialogFragment.this.getDialog().cancel();
                         }
                    });

                builder.setTitle("Live OC/UC").show();


                return false;
            }
        });


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
                if(shell.checkPath(shell.getInfo(CURRENT_GOV_AVAILABLE), a)) {
                    listPref.setSummary(a);
                } else {
                    Toast.makeText(getActivity(), "Couldn't set governor."   + " Old value; " +
                            shell.getInfo(CURRENT_GOV_AVAILABLE) + " New Value; " + a, Toast.LENGTH_LONG).show();
                    listPref.setSummary(shell.getInfo(CURRENT_GOV_AVAILABLE));
                }

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

                    // Should restore old values if something goes wrong;
                    listPref.setSummary(shell.getInfo(CURRENT_GOV_AVAILABLE));
                    listPref.setValue(shell.getInfo(CURRENT_GOV_AVAILABLE));

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
                CharSequence oldValue = max_frequency.getSummary();

                shell.setRootInfo((a.substring(0, a.length() - 4) + "000"), CPU_MAX_FREQ);

                if (shell.checkPath(shell.getInfo(CPU_MAX_FREQ), a)) {
                    max_frequency.setSummary(shell.toMHz((a.substring(0, a.length() - 4) + "000")));
                } else {
                    Toast.makeText(getActivity(), "Couldn't set max frequency." + " Old value; " +
                            shell.getInfo(CPU_MAX_FREQ) + " New Value; " + a, Toast.LENGTH_LONG).show();
                    max_frequency.setSummary(oldValue);
                }


                return true;
            }

            ;
        });

        min_frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;
                CharSequence oldValue = min_frequency.getSummary();

                shell.setRootInfo((a.substring(0, a.length() - 4) + "000"), CPU_MIN_FREQ);

                if (shell.checkPath(shell.getInfo(CPU_MIN_FREQ), a)) {
                    min_frequency.setSummary(shell.toMHz((a.substring(0, a.length() - 4) + "000")));
                } else {
                    Toast.makeText(getActivity(), "Couldn't set min frequency."  + " Old value; " +
                            shell.getInfo(CPU_MIN_FREQ) + " New Value; " + a, Toast.LENGTH_LONG).show();
                    min_frequency.setSummary(oldValue);
                }

                return true;
            }

            ;
        });


    }
    public void updateMinFreq() {
        // Just throw in our frequencies;
        min_frequency.setEntries(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        min_frequency.setEntryValues(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        try {
            min_frequency.setValue(shell.getInfoArray(CPU_MIN_FREQ, 1, 0)[0]);
            min_frequency.setSummary(shell.getInfoArray(CPU_MIN_FREQ, 1, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            min_frequency.setValue("Unavailable");
            min_frequency.setSummary("Unavailable");
        }
    }

    public void updateMaxFreq() {
        // Just throw in our frequencies;
        max_frequency.setEntries(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        max_frequency.setEntryValues(shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        try {
            max_frequency.setValue(shell.getInfoArray(CPU_MAX_FREQ, 1, 0)[0]);
            max_frequency.setSummary(shell.getInfoArray(CPU_MAX_FREQ, 1, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            max_frequency.setValue("Unavailable");
            max_frequency.setSummary("Unavailable");
        }
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
                    CharSequence oldValue = prefload.getSummary();

                    shell.setRootInfo(a, parameterPath);

                    if (shell.checkPath(shell.getInfo(parameterPath), a)) {
                        prefload.setSummary(a);
                    } else {
                        Toast.makeText(getActivity(), "Couldn't set desired parameter"  + " Old value; " +
                                shell.getInfo(parameterPath) + " New Value; " + a, Toast.LENGTH_LONG).show();
                        prefload.setSummary(oldValue);
                    }

                    return true;
                }

                ;
            });

        }
    }

}
