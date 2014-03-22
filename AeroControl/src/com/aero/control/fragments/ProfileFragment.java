package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.settingsHelper;
import com.espian.showcaseview.ShowcaseView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Alexander Christ on 09.12.13.
 */
public class ProfileFragment extends PreferenceFragment {

    private static final String LOG_TAG = PreferenceFragment.class.getName();
    private ViewGroup mContainerView;
    public ShowcaseView mShowCase;
    private SharedPreferences prefs;
    public ShowcaseView.ConfigOptions mConfigOptions;
    private static final String sharedPrefsPath = "/data/data/com.aero.control/shared_prefs/";
    private  String[] mCompleteProfiles;
    public static final String FILENAME_PROFILES = "firstrun_profiles";
    public static final settingsHelper settings = new settingsHelper();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final View v = inflater.inflate(R.layout.profile_fragment, null);

        mContainerView = (ViewGroup)v.findViewById(R.id.container);

        // Load all available profiles;
        loadProfiles();

        // Load default profiles;
        //addDefaultProfiles(new EditText(getActivity()));

        return mContainerView;
    }

    /*
     * Can be used later to create default profiles, placeholder for now
     */
    private final void addDefaultProfiles(EditText editText) {

        // If the profile doesn't exist, create it;
        final File prefFile = new File (sharedPrefsPath + "performance.xml");
        if(prefFile.exists()) {
            Log.e(LOG_TAG, "Performance Profile exists already!");
        } else {
            editText.setText("performance");
            addProfile(editText.getText().toString(), true);
        }
    }

    private final void loadProfiles() {

        mCompleteProfiles = AeroActivity.shell.getDirInfo(sharedPrefsPath, true);

        for (String s : mCompleteProfiles) {

            // Don't take default xml;
            if (!(s.equals("com.aero.control_preferences.xml") || s.equals("showcase_internal.xml"))) {
                // Just for the looks;
                s = s.replace(".xml", "");
                addProfile(s, false);
                mContainerView.findViewById(android.R.id.empty).setVisibility(View.GONE);
            }
        }

    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String a = prefs.getString("app_theme", null);

        if (a == null)
            a = "";

        if (a.equals("red"))
            inflater.inflate(R.menu.profiles_menu, menu);
        else if (a.equals("light"))
            inflater.inflate(R.menu.profiles_menu, menu);
        else if (a.equals("dark"))
            inflater.inflate(R.menu.profiles_menu_light, menu);
        else
            inflater.inflate(R.menu.profiles_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                // Hide the "empty" view since there is now at least one item in the list.
                mContainerView.findViewById(android.R.id.empty).setVisibility(View.GONE);

                showDialog(new EditText(getActivity()));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final void showDialog(final EditText editText) {

        mCompleteProfiles = AeroActivity.shell.getDirInfo(sharedPrefsPath, true);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_a_name)
                .setMessage(R.string.define_a_name)
                .setView(editText)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String allProfiles = Arrays.asList(mCompleteProfiles).toString();
                        String profileTitle = editText.getText().toString();

                        // Add content;
                        if(profileTitle.equals(""))
                            Toast.makeText(getActivity(), R.string.pref_profile_enter_name , Toast.LENGTH_LONG).show();
                        else if (allProfiles.contains(profileTitle + ".xml"))
                            Toast.makeText(getActivity(), R.string.pref_profile_name_exists , Toast.LENGTH_LONG).show();
                        else
                            addProfile(profileTitle, true);

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    // Adds the object to our "list", s = Name
    private void addProfile(String s, boolean flag) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences AeroProfile = getActivity().getSharedPreferences(s, Context.MODE_PRIVATE);
        final File defaultFile = new File(sharedPrefsPath + "com.aero.control_preferences.xml");

        if(defaultFile.exists()) {
            //
        } else {
            return;
        }

        // Flag if we add a profile to the list which already exists as a file;
        if (flag) {
            // This will save the current profile as a preference;
            saveNewProfile(AeroProfile);
        }

        // Instantiate a new "row" view.
        final ViewGroup childView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(
                R.layout.profiles_list, mContainerView, false);

        // Create TextView, with Content and Listeners;
        final TextView txtView = (TextView)childView.findViewById(R.id.profile_text);
        txtView.setText(s);
        createListener(txtView);

        // Remove the complete ViewGroup;
        childView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (deleteProfile(txtView.getText().toString()))
                    mContainerView.removeView(childView);

                // If there are no rows remaining, show
                // the empty view.
                if (mContainerView.getChildCount() == 1) {
                    mContainerView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
            }
        });

        mContainerView.addView(childView, 0);

    }

    private final boolean deleteProfile(String ProfileName) {

        // Delete the file, not just clear the pref;
        final String[] cmd = new String[] {
                "rm " + "\"" + sharedPrefsPath + ProfileName + ".xml" + "\""
        };

        AeroActivity.shell.setRootInfo(cmd);

        try {
            Thread.sleep(350);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Something interrupted the main Thread, try again.", e);
        }

        // Check if file is gone;
        final File prefFile = new File (sharedPrefsPath + ProfileName + ".xml");

        if(prefFile.exists()) {
            Log.e(LOG_TAG, "Whoop, it still exists, something went wrong");
            return false;
        } else {
            return true;
        }
    }

    private final void saveNewProfile(SharedPreferences AeroProfile) {
        // Just to be save, loading default again;
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = AeroProfile.edit();

        // Get all our preferences;
        final Map<String,?> allKeys = prefs.getAll();

        saveProfile(allKeys, editor);

    }

    private final void applyProfile(SharedPreferences AeroProfile) {

        // Just to be save, loading default again;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

        // Get all our preferences;
        final Map<String,?> allKeys = AeroProfile.getAll();

        saveProfile(allKeys, editor);
    }

    private final void saveProfile(Map<String,?> allKeys, SharedPreferences.Editor editor) {


        for(Map.Entry<String, ?> entry : allKeys.entrySet()) {

            String value = entry.getValue().toString();
            String key = entry.getKey().toString();

            // We found a boolean, wow!
            if (value.equals("true") || value.equals("false")) {

                Boolean tmp;
                /*
                 * Somehow getBoolean doesn't work for me here
                 */

                if (value.equals("false"))
                    tmp = false;
                else if (value.equals("true"))
                    tmp = true;
                else
                    tmp = false;

                editor.putBoolean(key, tmp);

            } else {
                editor.putString(key, value);
            }
        }

        editor.commit();

    }

    private final void renameProfile(CharSequence oldName, String newName, TextView txtView) {

        final String[] cmd = new String[] {
                "mv " + "\"" + sharedPrefsPath + oldName + ".xml" + "\"" + " " + "\"" + sharedPrefsPath + newName + ".xml" + "\""
        };

        AeroActivity.shell.setRootInfo(cmd);

        txtView.setText(newName);

    }

    /*
     * Create a onClick Listener for each profile;
     */

    private final void createListener(final TextView txtView) {

        // Change something else?
        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final SharedPreferences AeroProfile = getActivity().getSharedPreferences(txtView.getText().toString(), Context.MODE_PRIVATE);
                TextView profileText = new TextView(getActivity());
                String content = "";
                String tmp;

                // Get all our preferences;
                final Map<String,?> allKeys = AeroProfile.getAll();

                for(Map.Entry<String, ?> entry : allKeys.entrySet()) {

                    tmp = entry.getKey();

                    // For better looking;
                    if (tmp.contains("/sys/devices/system/cpu/cpufreq/"))
                        tmp = tmp.replace("/sys/devices/system/cpu/cpufreq/", "");
                    else if (tmp.contains("/proc/sys/vm/"))
                        tmp = tmp.replace("/proc/sys/vm/", "");
                    else if (tmp.contains("/sys/module/msm_kgsl_core/parameters/"))
                        tmp = tmp.replace("/sys/module/msm_kgsl_core/parameters/", "gpu -> ");
                    else if (tmp.contains("/sys/kernel/hotplug_control/"))
                        tmp = tmp.replace("/sys/kernel/hotplug_control/", "hotplug_control -> ");


                    content = tmp + " = " + entry.getValue().toString() + "\n" + content;

                    profileText.setText(content);

                }

                profileText.setVerticalScrollBarEnabled(true);
                profileText.setMovementMethod(new ScrollingMovementMethod());
                profileText.setPadding(20, 20, 20, 20);

                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getText(R.string.slider_overview) + ": " + txtView.getText().toString())
                        .setView(profileText)
                        .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteProfile("com.aero.control_preferences");
                                SharedPreferences AeroProfile = getActivity().getSharedPreferences(txtView.getText().toString(), Context.MODE_PRIVATE);
                                applyProfile(AeroProfile);
                                settings.setSettings(getActivity());

                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.show();

            }

        });

        // Change the name of the profile;
        txtView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mCompleteProfiles = AeroActivity.shell.getDirInfo(sharedPrefsPath, true);
                final EditText editText = new EditText(getActivity());
                final CharSequence oldName = txtView.getText();
                editText.setText(oldName);

                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.pref_profile_change_name)
                        .setMessage(R.string.pref_profile_change_name_sum)
                        .setView(editText)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String newName = editText.getText().toString();
                                String allProfiles = Arrays.asList(mCompleteProfiles).toString();

                                if (allProfiles.contains(newName + ".xml")) {
                                    Toast.makeText(getActivity(), R.string.pref_profile_name_exists, Toast.LENGTH_LONG).show();
                                } else {
                                    txtView.setText(newName);
                                    renameProfile(oldName, newName, txtView);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.show();

                return true;
            }

        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        // Prepare Showcase;
        mConfigOptions = new ShowcaseView.ConfigOptions();
        mConfigOptions.hideOnClickOutside = false;
        mConfigOptions.shotType = ShowcaseView.TYPE_ONE_SHOT;

        // Set up our file;
        int output = 0;
        final byte[] buffer = new byte[1024];

        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME_PROFILES);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_profile_fragment, R.string.showcase_profile_fragment_sum, FILENAME_PROFILES);

    }

    public void DrawFirstStart(int header, int content, String filename) {

        try {
            FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        mShowCase = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, R.id.action_add_item, getActivity(), header, content, mConfigOptions);
    }


}
