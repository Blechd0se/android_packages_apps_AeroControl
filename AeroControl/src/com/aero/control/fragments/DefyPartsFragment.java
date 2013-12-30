package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.helpers.shellHelper;

/**
 * Created by Alexander Christ on 16.09.13.
 * This should replace the Defy Parts.
 */
public class DefyPartsFragment extends PreferenceFragment {
    /*
    TODO: - Simplify it!
          - Add checks for different kernel settings
          - Bring in other defy part features
     */
    private static final String PROP_CHARGE_LED_MODE = "persist.sys.charge_led";
    private static final String PROP_TOUCH_POINTS = "persist.sys.multitouch";
    private static final String PROP_BUTTON_BRIGHTNESS = "persist.sys.button_brightness";

    shellHelper shell = new shellHelper();

    private ListPreference led_charging;
    private ListPreference multi_touch;
    private EditTextPreference button_brightness;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.defy_parts);

        PreferenceScreen root = this.getPreferenceScreen();

        String charger = shell.getRootInfo("getprop ", PROP_CHARGE_LED_MODE);
        String multitouch = shell.getRootInfo("getprop ", PROP_TOUCH_POINTS);
        String brightness = shell.getRootInfo("getprop", PROP_BUTTON_BRIGHTNESS);

        led_charging = (ListPreference)root.findPreference("led_charging");
        button_brightness = (EditTextPreference)root.findPreference("button_brightness");
        multi_touch = (ListPreference)root.findPreference("multi_touch");

        led_charging.setEntryValues(R.array.charge_led_mode_values);
        led_charging.setEntries(R.array.charge_led_mode_entries);

        if (charger.length() > 1) {
            led_charging.setValue(charger);
            led_charging.setSummary(charger);
        } else {
            led_charging.setEnabled(false);
        }

        multi_touch.setEntryValues(R.array.touch_point_values);
        multi_touch.setEntries(R.array.touch_point_values);

        if (charger.length() > 1) {
            multi_touch.setValue(multitouch);
            multi_touch.setSummary(multitouch);
        } else {
            multi_touch.setEnabled(false);
        }

        if (charger.length() > 1) {
            button_brightness.setText(brightness);
        } else {
            button_brightness.setEnabled(false);
        }

        led_charging.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                changePreference(preference, o, PROP_CHARGE_LED_MODE);

                led_charging.setValue(o.toString());
                led_charging.setSummary(o.toString());

                return true;
            }
        });

        multi_touch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                changePreference(preference, o, PROP_TOUCH_POINTS);

                multi_touch.setValue(o.toString());
                multi_touch.setSummary(o.toString());

                return true;
            }
        });

        button_brightness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                changePreference(preference, o, PROP_BUTTON_BRIGHTNESS);

                button_brightness.setText(o.toString());

                return true;
            }
        });

    }

    private void changePreference(Preference preference, Object o, String file) {

        String[] command = new String[] {
                "setprop " + file + " " + o.toString()
        };

        shell.setRootInfo(command);

        Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_SHORT).show();

        // Save values;
        preference.getEditor().commit();

    }
}
