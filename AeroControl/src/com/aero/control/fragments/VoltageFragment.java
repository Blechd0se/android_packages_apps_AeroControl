package com.aero.control.fragments;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.CustomTextPreference;

import java.util.ArrayList;

/**
 * Created by Alexander Christ on 03.05.14.
 */
public class VoltageFragment extends PreferenceFragment {

    public PreferenceScreen root;
    public PreferenceCategory PrefCat;
    public static final String VOLTAGE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    final ArrayList<String> voltList = new ArrayList<String>();
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();
        TextView mActionBarTitle = (TextView) getActivity().findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));
        mActionBarTitle.setText(R.string.perf_voltage_control);

        // Load our custom preferences;
        loadVoltage();
    }
    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.voltage_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String [] voltArray = voltList.toArray(new String[0]);
        String exec = "";


        switch (item.getItemId()) {
            case R.id.action_mVPlus:

                voltList.clear();
                for (String a : voltArray) {
                    int tmp = Integer.parseInt(a) + 25;
                    voltList.add("" + tmp);

                    exec = exec + " " + tmp;
                }
                executeVolt(exec);

                break;
            case R.id.action_mVMinus:

                voltList.clear();
                for (String a : voltArray) {
                    int tmp = Integer.parseInt(a) - 25;
                    voltList.add("" + tmp);

                    exec = exec + " " + tmp;
                }
                executeVolt(exec);

                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void loadVoltage() {

        String completeParamterList[] = AeroActivity.shell.getInfo(VOLTAGE_PATH, false);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        PrefCat = new PreferenceCategory(getActivity());
        PrefCat.setTitle(R.string.perf_voltage_control);
        root.addPreference(PrefCat);

        String freqTmp, volTmp;
        for (String s : completeParamterList) {
            freqTmp = s.split(":")[0];
            volTmp = s.split(":")[1].replace(" ", "");

            voltList.add(volTmp.replace("mV", ""));

            // Generates our custom text preference
            final CustomTextPreference voltPref = new CustomTextPreference(getActivity());
            voltPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            voltPref.setSummary(volTmp);
            voltPref.setTitle(freqTmp);
            voltPref.setText(volTmp.replace("mV", ""));
            voltPref.setDialogTitle(freqTmp);

            PrefCat.addPreference(voltPref);

            // Custom OnChangeListener for each element in our list;
            voltPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    String [] voltArray = voltList.toArray(new String[0]);
                    String tmp = "";

                    voltArray[preference.getOrder()] = o.toString();
                    preference.setSummary(o.toString() + "mV");

                    // Clears our list so we can set multiple values
                    voltList.clear();
                    for (String a : voltArray) {
                        tmp = tmp + " " + a;
                        voltList.add(a);
                    }

                    executeVolt(tmp);

                    return true;
                };
            });

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            /* For better KitKat+ looks; */
                Preference blankedPref = new Preference(getActivity());
                blankedPref.setSelectable(false);
                PrefCat.addPreference(blankedPref);
        }
    }

    // Executes the new voltage values and updates UI
    public void executeVolt(String exeVolt) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPrefs.edit().putString("voltage_values", exeVolt).commit();
        AeroActivity.shell.setRootInfo(exeVolt, VOLTAGE_PATH);
        updateUI();

    }

    // Updates UI by iterating through the children;
    public void updateUI() {

        String [] voltArray = voltList.toArray(new String[0]);

        for (int i = 0; i < PrefCat.getPreferenceCount() - 1; i++) {
            CustomTextPreference voltPref = (CustomTextPreference)PrefCat.getPreference(i);
            voltPref.setSummary(voltArray[i] + "mV");
            voltPref.setText(voltArray[i]);
        }

    }
}