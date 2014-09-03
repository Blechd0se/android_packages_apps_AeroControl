package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.CustomEditText;
import com.aero.control.helpers.CustomListPreference;
import com.aero.control.helpers.CustomTextPreference;

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

    private CustomListPreference led_charging;
    private CustomListPreference multi_touch;
    private CustomTextPreference button_brightness;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.defy_parts);

        PreferenceScreen root = this.getPreferenceScreen();
        final PreferenceCategory defyParts = (PreferenceCategory) findPreference("defy_parts");

        String charger = AeroActivity.shell.getRootInfo("getprop ", AeroActivity.files.PROP_CHARGE_LED_MODE);
        String multitouch = AeroActivity.shell.getRootInfo("getprop ", AeroActivity.files.PROP_TOUCH_POINTS);
        String brightness = AeroActivity.shell.getRootInfo("getprop", AeroActivity.files.PROP_BUTTON_BRIGHTNESS);

        led_charging = new CustomListPreference(getActivity());
        led_charging.setName("led_charging");
        led_charging.setSummary(R.string.pref_charging_led_sum);
        led_charging.setTitle(R.string.pref_charging_led);
        led_charging.setDialogTitle(R.string.pref_charging_led);
        led_charging.setOrder(1);
        defyParts.addPreference(led_charging);

        button_brightness = new CustomTextPreference(getActivity());
        button_brightness.setName("button_brightness");
        button_brightness.setPrefSummary(brightness);
        button_brightness.setPrefText(getText(R.string.pref_button_brightness).toString());
        button_brightness.setDialogTitle(getText(R.string.pref_button_brightness).toString());
        button_brightness.setSummary(brightness);
        button_brightness.setTitle(getText(R.string.pref_button_brightness).toString());
        button_brightness.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        button_brightness.setOrder(5);
        defyParts.addPreference(button_brightness);

        multi_touch = new CustomListPreference(getActivity());
        multi_touch.setName("multi_touch");
        multi_touch.setSummary(R.string.pref_multitouch_sum);
        multi_touch.setTitle(R.string.pref_multitouch);
        multi_touch.setDialogTitle(R.string.pref_multitouch);
        multi_touch.setOrder(10);
        defyParts.addPreference(multi_touch);

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

                changePreference(preference, o, AeroActivity.files.PROP_CHARGE_LED_MODE);

                led_charging.setValue(o.toString());
                led_charging.setSummary(o.toString());

                return true;
            }
        });

        multi_touch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                changePreference(preference, o, AeroActivity.files.PROP_TOUCH_POINTS);

                multi_touch.setValue(o.toString());
                multi_touch.setSummary(o.toString());

                return true;
            }
        });

        button_brightness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                changePreference(preference, o, AeroActivity.files.PROP_BUTTON_BRIGHTNESS);

                button_brightness.setText(o.toString());
                button_brightness.setPrefSummary(o.toString());

                return true;
            }
        });

    }

    private void changePreference(Preference preference, Object o, String file) {

        String[] command = new String[] {
                "setprop " + file + " " + o.toString()
        };

        AeroActivity.shell.setRootInfo(command);

        Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_SHORT).show();
    }
}
