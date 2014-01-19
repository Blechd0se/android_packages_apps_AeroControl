package com.aero.control.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;

/**
 * Created by Alexander Christ on 21.09.13.
 */
public class PrefsActivity extends PreferenceActivity {

    static Context context;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String a = prefs.getString("app_theme", null);

        if (a == null)
            a = "";

        if (a.equals("red"))
            setTheme(R.style.RedHolo);
        else if (a.equals("light"))
            setTheme(android.R.style.Theme_Holo_Light);
        else if (a.equals("dark"))
            setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
        else
            setTheme(R.style.RedHolo);

        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preference);

        // Get name;
        this.setTitle(R.string.aero_settings);

        context = this;

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final PreferenceScreen root = this.getPreferenceScreen();
        String[] data = {
                "red", "light", "dark"
        };

        CheckBoxPreference checkbox_preference = (CheckBoxPreference)root.findPreference("checkbox_preference");
        CheckBoxPreference reboot_checker = (CheckBoxPreference)root.findPreference("reboot_checker");
        ListPreference appTheme = (ListPreference)root.findPreference("app_theme");
        Preference about = root.findPreference("about");
        Preference legal = root.findPreference("legal");

        checkbox_preference.setIcon(R.drawable.ic_action_warning);
        reboot_checker.setIcon(R.drawable.ic_action_phone);

        appTheme.setEntries(R.array.app_themes);
        appTheme.setEntryValues(data);
        appTheme.setEnabled(true);
        appTheme.setIcon(R.drawable.ic_action_event);

        about.setIcon(R.drawable.ic_action_about);
        legal.setIcon(R.drawable.ic_action_legal);

        appTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle(R.string.about);
                builder.setIcon(R.drawable.email_dark);

                aboutText.setText(getText(R.string.about_dialog));

                builder.setView(layout)
                        .setPositiveButton(R.string.github, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Uri uri = Uri.parse("https://github.com/Blechd0se/android_packages_apps_AeroControl");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.donation_blechdose, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Uri uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=46VQEKBETN36U");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        })
                        .setNeutralButton(R.string.donation_quarx, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Uri uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=quarx%40yandex%2eru&lc=DE&no_note=0&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });

                builder.show();


                return true;
            }
        });

        legal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle(R.string.legal);
                builder.setIcon(R.drawable.email_dark);

                aboutText.setText(getText(R.string.legal_dialog));
                aboutText.setTextSize(13);

                builder.setView(layout)
                        .setPositiveButton(R.string.send_email, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                        "mailto", "alex.christ@hotmail.de", null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[AeroControl] Got something for you");
                                startActivity(Intent.createChooser(emailIntent, getText(R.string.send_email)));
                            }
                        });

                builder.show();

                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}