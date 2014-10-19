package com.aero.control.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.service.PerAppServiceHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Alexander Christ on 21.09.13.
 */
public class PrefsActivity extends PreferenceActivity {

    static Context context;
    public static final Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    public int mActionBarTitleID;
    public TextView mActionBarTitle;
    private int mCounter;
    private CheckBoxPreference mPer_app_check, mRebootChecker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActionBar().setIcon(R.drawable.app_icon_actionbar);

        mCounter = 0;

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

        if (mRebootChecker == null)
            mRebootChecker = (CheckBoxPreference)root.findPreference("reboot_checker");
        if (mPer_app_check == null)
            mPer_app_check = (CheckBoxPreference)root.findPreference("per_app_service");

        Preference resetTutorials = root.findPreference("reset_tutorials");
        Preference about = root.findPreference("about");
        Preference version = root.findPreference("version");
        Preference legal = root.findPreference("legal");
        Preference xda = root.findPreference("xda_thread");

        mRebootChecker.setIcon(R.drawable.ic_action_phone);
        setCheckedState(mRebootChecker);
        mPer_app_check.setIcon(R.drawable.ic_action_person);
        resetTutorials.setIcon(R.drawable.ic_action_warning);
        setCheckedState(mPer_app_check);
        version.setIcon(R.drawable.rocket);
        xda.setIcon(R.drawable.xda);

        try {
            version.setTitle("Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            version.setSummary("Build:" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {

        }

        about.setIcon(R.drawable.ic_action_about);
        legal.setIcon(R.drawable.ic_action_legal);


        xda.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Uri uri = Uri.parse("http://forum.xda-developers.com/showthread.php?t=2483827");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        mRebootChecker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setCheckedState((CheckBoxPreference) preference);
                return false;
            }
        });

        mPer_app_check.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                setCheckedState((CheckBoxPreference)preference);

                if (mPer_app_check.isChecked()) {

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

        resetTutorials.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);

                dialog.setTitle(R.string.pref_reset_tutorials_title);
                dialog.setMessage(R.string.pref_reset_tutorials_dialog);
                dialog.setIcon(R.drawable.warning);
                dialog.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Going to reset..
                        String[] fileArray = AeroActivity.shell.getDirInfo(getApplicationInfo().dataDir + "/files/", true);
                        for (String s : fileArray) {
                            if (!(new File(getApplicationInfo().dataDir + "/files/" + s).delete()))
                                Log.e("Aero", "Couldn't delete: " + s);
                        }
                    }
                });
                dialog.setNegativeButton(R.string.cancel, null);

                dialog.create().show();

                return false;
            }
        });

        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle(R.string.about);
                builder.setIcon(R.drawable.beer);

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
                builder.setIcon(R.drawable.email);

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

        version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                mCounter++;

                // Show testsuite if clicked;
                if (mCounter >= 7) {

                    mCounter = 0;
                    ContextWrapper cw = new ContextWrapper(getBaseContext());
                    File testsuite = new File(cw.getFilesDir() + "/testsuite");

                    if (testsuite.exists()) {
                        testsuite.delete();
                        Toast.makeText(getApplicationContext(), "You have disabled the TestSuite!", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    try {
                        FileOutputStream fos = getApplicationContext().openFileOutput("testsuite", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos.close();
                    } catch (IOException e) {
                        Log.e("Aero", "Could not save file. ", e);
                    }

                    Toast.makeText(getApplicationContext(), "You have enabled the TestSuite!", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }

    private final void setCheckedState(CheckBoxPreference preference) {

        if (preference.isChecked())
            preference.setSummary(R.string.enabled);
        else
            preference.setSummary(R.string.disabled);
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