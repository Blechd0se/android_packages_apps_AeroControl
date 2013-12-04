package com.aero.control.prefs;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 21.09.13.
 */
public class PrefsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preference);

        // Get name;
        this.setTitle(R.string.aero_settings);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final PreferenceScreen root = this.getPreferenceScreen();
        String[] data = {
                "red", "light"
        };

        EditTextPreference updateLocation = (EditTextPreference)root.findPreference("update_location");
        updateLocation.setEnabled(false);
        ListPreference appTheme = (ListPreference)root.findPreference("app_theme_list");
        appTheme.setEntries(data);
        appTheme.setEntryValues(data);
        appTheme.setEnabled(false);

        appTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;

                // Somehow set the style here....;
                if (a.equals("red")) {
                    //this.setTheme(R.style.RedHolo);
                    root.setLayoutResource(R.style.RedHolo);
                }
                else if (a.equals("light")) {
                    //getActivity().setTheme(android.R.style.Theme_Holo_Light);
                    root.setLayoutResource(android.R.style.Theme_Holo_Light);
                }

                return true;
            };
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}