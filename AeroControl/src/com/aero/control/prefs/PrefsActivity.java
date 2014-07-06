package com.aero.control.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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
import com.aero.control.service.PerAppServiceHelper;

/**
 * Created by Alexander Christ on 21.09.13.
 */
public class PrefsActivity extends PreferenceActivity {

    static Context context;
    private SharedPreferences prefs;
    public static final Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    public int mActionBarTitleID;
    public TextView mActionBarTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String a = prefs.getString("app_theme", null);
        getActionBar().setIcon(R.drawable.app_icon_actionbar);

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
        mActionBarTitleID = getResources().getIdentifier("action_bar_title", "id", "android");
        mActionBarTitle = (TextView) findViewById(mActionBarTitleID);
        mActionBarTitle.setTypeface(font);

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
        final CheckBoxPreference per_app_check = (CheckBoxPreference)root.findPreference("per_app_service");
        ListPreference appTheme = (ListPreference)root.findPreference("app_theme");
        Preference about = root.findPreference("about");
        Preference legal = root.findPreference("legal");

        checkbox_preference.setIcon(R.drawable.ic_action_warning);
        reboot_checker.setIcon(R.drawable.ic_action_phone);
        per_app_check.setIcon(R.drawable.ic_action_person);

        appTheme.setEntries(R.array.app_themes);
        appTheme.setEntryValues(data);
        appTheme.setEnabled(true);
        appTheme.setIcon(R.drawable.ic_action_event);

        about.setIcon(R.drawable.ic_action_about);
        legal.setIcon(R.drawable.ic_action_legal);

        per_app_check.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (per_app_check.isChecked()) {

                    // Only start if really down;
                    if (AeroActivity.perAppService == null)
                        AeroActivity.perAppService = new PerAppServiceHelper(getBaseContext());

                    if (AeroActivity.perAppService.getState() == false)
                        AeroActivity.perAppService.startService();


                    //** store preferences
                    preference.getEditor().commit();
                    return true;
                } else  {

                    // Only stop if running;
                    if (AeroActivity.perAppService == null)
                        return false;

                    if (AeroActivity.perAppService.getState() == true)
                        AeroActivity.perAppService.stopService();

                    return false;
                }
            }
        });

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
                finish();
                Intent i = new Intent(context, AeroActivity.class);
                context.startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}