package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 16.09.13.
 * This should replace the Defy Parts.
 */
public class DefyPartsFragment extends PreferenceFragment {
    /*
    TODO:
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.defy_parts);

        PreferenceScreen root = this.getPreferenceScreen();

        ListPreference led_charging = (ListPreference)root.findPreference("led_charging");
        EditTextPreference button_brightness = (EditTextPreference)root.findPreference("button_brightness");
        ListPreference multi_touch = (ListPreference)root.findPreference("multi_touch");

        led_charging.setEntryValues(R.array.charge_led_mode_entries);
        led_charging.setEntries(R.array.charge_led_mode_entries);

        multi_touch.setEntryValues(R.array.touch_point_values);
        multi_touch.setEntries(R.array.touch_point_values);


    }
}
