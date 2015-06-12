package com.aero.control.fragments;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.CustomTextPreference;
import com.aero.control.helpers.FilePath;

import java.util.ArrayList;

/**
 * Created by Alexander Christ on 03.05.14.
 */
public class VoltageFragment extends PlaceHolderFragment {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private final ArrayList<String> voltList = new ArrayList<String>();
    private SharedPreferences mPrefs;
    private TextView mActionBarTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();
        mActionBarTitle = (TextView) getActivity().findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));
        setTitle();

        // Load our custom preferences;
        loadVoltage();
    }

    public final void setTitle() {
        if (mActionBarTitle != null)
            mActionBarTitle.setText(R.string.perf_voltage_control);
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

        String completeParamterList[] = AeroActivity.shell.getInfo(FilePath.VOLTAGE_PATH, false);

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
            voltPref.setPrefSummary(volTmp);
            voltPref.setTitle(freqTmp);
            voltPref.setPrefText(freqTmp);
            voltPref.setText(volTmp.replace("mV", ""));
            voltPref.setDialogTitle(freqTmp);
            voltPref.setHideOnBoot(true);

            PrefCat.addPreference(voltPref);

            // Custom OnChangeListener for each element in our list;
            voltPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    String [] voltArray = voltList.toArray(new String[0]);
                    String tmp = "";

                    voltArray[preference.getOrder()] = o.toString();
                    preference.setSummary(o.toString() + "mV");
                    voltPref.setPrefSummary(o.toString() + "mV");

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
        AeroActivity.shell.setRootInfo(exeVolt, FilePath.VOLTAGE_PATH);
        updateUI();

    }

    // Updates UI by iterating through the children;
    public void updateUI() {

        String [] voltArray = voltList.toArray(new String[0]);

        for (int i = 0; i < PrefCat.getPreferenceCount() - 1; i++) {
            CustomTextPreference voltPref = (CustomTextPreference)PrefCat.getPreference(i);
            voltPref.setSummary(voltArray[i] + "mV");
            voltPref.setPrefSummary(voltArray[i] + "mV");
            voltPref.setText(voltArray[i]);
        }

    }
}