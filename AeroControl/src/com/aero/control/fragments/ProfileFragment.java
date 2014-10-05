package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PerAppManager;
import com.aero.control.helpers.perAppHelper;
import com.aero.control.helpers.settingsHelper;
import com.aero.control.service.PerAppServiceHelper;
import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarController.AdvancedUndoListener;
import com.cocosw.undobar.UndoBarStyle;
import com.espian.showcaseview.ShowcaseView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fr.nicolaspomepuy.discreetapprate.AppRate;

/**
 * Created by Alexander Christ on 09.12.13.
 */
public class ProfileFragment extends PreferenceFragment implements AdvancedUndoListener {

    private static final String LOG_TAG = PreferenceFragment.class.getName();
    private ViewGroup mContainerView;
    public ShowcaseView mShowCase;
    private SharedPreferences mPrefs;
    public ShowcaseView.ConfigOptions mConfigOptions;
    private static final String perAppProfileHandler = "perAppProfileHandler";
    private  String[] mCompleteProfiles;
    public static final String FILENAME_PROFILES = "firstrun_profiles";
    public static final String FILENAME_PERAPP = "firstrun_perapp";
    public static final settingsHelper settings = new settingsHelper();
    private ViewGroup mDeletedChild;
    private String mDeletedProfile;
    private SharedPreferences mPerAppPrefs;
    private Context mContext;
    private List<ApplicationInfo> mPackages;
    private ProgressDialog mProgressDialog;
    private boolean mWarning;
    private boolean mPerAppDialogVisible;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        mWarning = false;
        mPerAppDialogVisible = false;

        mPerAppPrefs = mContext.getSharedPreferences(perAppProfileHandler, Context.MODE_PRIVATE);
        final View v = inflater.inflate(R.layout.profile_fragment, null);
        final TextView empty = (TextView)v.findViewById(android.R.id.empty);
        empty.setTypeface(AeroActivity.files.kitkatFont);

        mContainerView = (ViewGroup)v.findViewById(R.id.container);

        // Load all available profiles;
        loadProfiles();

        return mContainerView;
    }

    private void loadProfiles() {

        mCompleteProfiles = AeroActivity.shell.getDirInfo(AeroActivity.files.sharedPrefsPath, true);

        for (String s : mCompleteProfiles) {

            // Don't take default xml;
            if (!(s.equals("com.aero.control_preferences.xml") || s.equals("showcase_internal.xml")
                    || s.equals("app_rate_prefs.xml") || s.equals(perAppProfileHandler + ".xml")
                    || s.equals("miscSettingsStorage.xml"))) {
                // Just for the looks;
                s = s.replace(".xml", "");
                addProfile(s, false);
                mContainerView.findViewById(android.R.id.empty).setVisibility(View.GONE);
                mContainerView.findViewById(R.id.empty_image).setVisibility(View.GONE);
            }
        }

        // If null, initiate the helper;
        if (AeroActivity.perAppService == null)
            AeroActivity.perAppService = new PerAppServiceHelper(mContext);

        // User has assigned apps, but no service is running;
        if (!(AeroActivity.perAppService.getState())) {
            if (checkAllStates() && !mWarning) {
                AppRate.with(getActivity())
                        .text(R.string.pref_profile_service_not_running)
                        .fromTop(false)
                        .delay(1000)
                        .autoHide(15000)
                        .allowPlayLink(false)
                        .forceShow();
                mWarning = true;
            }
        }
    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        inflater.inflate(R.menu.profiles_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:

                // Check if there are actual changes;
                if (!(AeroActivity.genHelper.doesExist(AeroActivity.files.sharedPrefsPath + "com.aero.control_preferences.xml"))) {
                    Toast.makeText(mContext, R.string.pref_profile_no_changes , Toast.LENGTH_LONG).show();
                    break;
                }

                showDialog(new EditText(mContext));
                break;
            case R.id.action_reload:
                showResetDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showResetDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(R.drawable.warning);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.about_screen, null);
        TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

        builder.setTitle(R.string.pref_profile_reset);

        aboutText.setText(R.string.pref_profile_reset_sum);

        builder.setView(layout)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Continue with resetting
                        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor  editor = mPrefs.edit();
                        editor.clear();
                        editor.commit();
                        Toast.makeText(mContext, R.string.successful , Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.maybe_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.show();
    }

    private void showDialog(final EditText editText) {

        mCompleteProfiles = AeroActivity.shell.getDirInfo(AeroActivity.files.sharedPrefsPath, true);

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.add_a_name)
                .setIcon(R.drawable.profile_new)
                .setMessage(R.string.define_a_name)
                .setView(editText)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String allProfiles = Arrays.asList(mCompleteProfiles).toString();
                        String profileTitle = editText.getText().toString();

                        // Add content;
                        if (profileTitle.equals(""))
                            Toast.makeText(mContext, R.string.pref_profile_enter_name, Toast.LENGTH_LONG).show();
                        else if (allProfiles.contains(profileTitle + ".xml"))
                            Toast.makeText(mContext, R.string.pref_profile_name_exists, Toast.LENGTH_LONG).show();
                        else {

                            /* Check here if there are path seperators, but don't interrupt the process */
                            if (profileTitle.contains("/"))
                                profileTitle = profileTitle.replace("/", "-");

                            addProfile(profileTitle, true);
                            // Set up our file;
                            int output = 0;
                            final byte[] buffer = new byte[1024];

                            try {
                                FileInputStream fis = mContext.openFileInput(FILENAME_PERAPP);
                                output = fis.read(buffer);
                                fis.close();
                            } catch (IOException e) {
                                Log.e(LOG_TAG, "Couldn't open File... " + output);
                            }

                            // Only show showcase once;
                            if (output == 0)
                                DrawFirstStart(R.string.showcase_perapp_profiles, R.string.showcase_perapp_profiles_sum, FILENAME_PERAPP, null);

                            // Hide the "empty" view since there is now at least one item in the list.
                            mContainerView.findViewById(android.R.id.empty).setVisibility(View.GONE);
                            mContainerView.findViewById(R.id.empty_image).setVisibility(View.GONE);

                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.pref_profile_import, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

                        final String dir = AeroActivity.files.EXTERNAL_PATH + "/com.aero.control/profiles";

                        if (!(AeroActivity.genHelper.doesExist(dir)))
                            return;


                        final String[] strings = AeroActivity.shell.getDirInfo(dir, true);
                        final ArrayList<Boolean> importProfiles = new ArrayList<Boolean>();

                        for (String s : strings)
                            importProfiles.add(false);

                        dialog.setTitle(R.string.pref_profile_import_select);
                        dialog.setIcon(R.drawable.restore);
                        dialog.setMultiChoiceItems(strings, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                if (b) {
                                    importProfiles.add(i, true);
                                } else {
                                    importProfiles.add(i, false);
                                }
                            }
                        })
                                // Set the action buttons
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        int i = 0;
                                        for (String s : strings) {
                                            if (importProfiles.get(i)) {
                                                // Do the import;
                                                try {
                                                    AeroActivity.genHelper.copyFile(AeroActivity.genHelper.getNewFile(dir + "/" + s),
                                                            AeroActivity.genHelper.getNewFile(AeroActivity.files.sharedPrefsPath + s));
                                                } catch (IOException e) {
                                                    Log.e(LOG_TAG, "Couldn't copy file: " + dir + "/" + s, e);
                                                }

                                                if (AeroActivity.genHelper.doesExist(AeroActivity.files.sharedPrefsPath + s))
                                                    Toast.makeText(mContext, R.string.successful, Toast.LENGTH_SHORT).show();
                                            }
                                            i++;
                                        }
                                        // Reload UI;
                                        for (int j = 0; j < mContainerView.getChildCount(); j++) {
                                            mContainerView.getChildAt(j).setVisibility(View.GONE);
                                        }
                                        loadProfiles();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Do Nothing

                                    }
                                })
                        ;
                        dialog.create().show();
                    }
                })
                .create();

        dialog.show();
    }

    // Adds the object to our "list", s = Name
    private void addProfile(final String s, boolean flag) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences AeroProfile = mContext.getSharedPreferences(s, Context.MODE_PRIVATE);

        // Init the perApp data here, so we can re-use it for each profile
        final perAppHelper perApp = new perAppHelper(mContext);

        if(!(AeroActivity.genHelper.doesExist(AeroActivity.files.sharedPrefsPath + "com.aero.control_preferences.xml")))
            return;

        // Flag if we add a profile to the list which already exists as a file;
        if (flag) {
            // This will save the current profile as a preference and invokes a new scan;
            saveNewProfile(AeroProfile);
        }

        // Instantiate a new "row" view.
        final ViewGroup childView = (ViewGroup) LayoutInflater.from(mContext).inflate(
                R.layout.profiles_list, mContainerView, false);

        // Create TextView, with Content and Listeners;
        final TextView txtView = (TextView)childView.findViewById(R.id.profile_text);
        final TextView txtViewSummary = (TextView)childView.findViewById(R.id.profile_text_summary);
        txtView.setText(s);

        /*
         * Case 1; We open up our data and check, if the user has checked anything for this profile
         * Case 2: We actually map the found data to our objects later on (checked state)
         */

        if (checkState(s)) {
            // he has checked something!
            updateStatus(txtViewSummary, true);
        } else {
            updateStatus(txtViewSummary, false);
        }

        txtView.setTypeface(AeroActivity.files.kitkatFont);
        createListener(txtView, txtViewSummary);


        final UndoBarStyle style = new UndoBarStyle(R.drawable.ic_action_undo, R.string.pref_profile_undo,
                R.drawable.undobar_background, 5000).setAnim(AnimationUtils.loadAnimation(mContext,
                android.R.anim.fade_in), AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));

        // Remove the complete ViewGroup;
        childView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UndoBarController.show(getActivity(), getText(R.string.pref_profile_deleted), ProfileFragment.this, style);

                if (mDeletedChild != null)
                    deleteProfile(mDeletedProfile);

                mDeletedProfile = txtView.getText().toString();
                mDeletedChild = childView;
                mPrefs = mContext.getSharedPreferences(mDeletedProfile, Context.MODE_PRIVATE);

                mContainerView.removeView(mDeletedChild);

            }
        });
        // Assign this profile to an app
        childView.findViewById(R.id.assign_to_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handles mapping and calls the dialog builder;
                getPersistentData(perApp, s, txtViewSummary);
            }
        });

        mContainerView.addView(childView, 0);

    }

    /*
     * Called to revoke the operations;
     */
    @Override
    public void onUndo(final Parcelable token) {
        Toast.makeText(mContext, R.string.successful, Toast.LENGTH_SHORT).show();
        mContainerView.addView(mDeletedChild);
    }

    /*
     * Called to execute the operations;
     */
    @Override
    public void onHide(final Parcelable token) {
        if (deleteProfile(mDeletedProfile)) {
            mContainerView.removeView(mDeletedChild);
        }

        // If there are no rows remaining, show
        // the empty view.
        if (mContainerView.getChildCount() == 2) {
            mContainerView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            mContainerView.findViewById(R.id.empty_image).setVisibility(View.VISIBLE);
        }

    }

    // Returns true if any available profile is assigned
    private boolean checkAllStates() {

        for (String s : mCompleteProfiles) {

            // Don't take default xml;
            if (!(s.equals("com.aero.control_preferences.xml") || s.equals("showcase_internal.xml")
                    || s.equals("app_rate_prefs.xml") || s.equals(perAppProfileHandler + ".xml")
                    || s.equals("miscSettingsStorage.xml"))) {
                // Just for the looks;
                s = s.replace(".xml", "");
                if (checkState(s))
                    return true;
            }
        }
        return false;

    }

    // Checks and returns true if the profile is assigned
    private boolean checkState(String name) {

        final String profile = mPerAppPrefs.getString(name, null);

        // No profile was created anyway, return quickly;
        if (profile == null)
            return false;

        String tmp[];
        tmp = profile.replace("+", " ").split(" ");

        // If no assigned apps are found, set to false, otherwise update UI
        for (final String a : tmp) {
            if (a.equals(""))
                return false;
        }
        return true;

    }

    /*
     * Maps the persistent data in shared_prefs to our currently
     * available objects and also sets up the per app dialog
     */
    private void getPersistentData(final perAppHelper perApp, final String profileName, final TextView txtViewSummary) {

        if (mPerAppDialogVisible)
            return;

        mPerAppDialogVisible = true;

        final String savedSelectedProfiles = mPerAppPrefs.getString(profileName, null);
        String systemApps = mPerAppPrefs.getString("systemStatus", null);

        //Probably a "fresh" profile;
        if (systemApps == null)
            systemApps = "false";

        perApp.setSystemAppStatus(Boolean.valueOf(systemApps));

        if (mPackages != null) {
            // We are good to go, save time!
            perApp.setPackages(mPackages);

            // Map data if present
            if (savedSelectedProfiles != null) {
                String tmp[];
                tmp = savedSelectedProfiles.replace("+", " ").split(" ");

                // Finds the matches;
                perApp.findMatch(tmp);
            }

            showPerAppDialog(perApp, profileName, txtViewSummary);

        } else {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setMessage(getText(R.string.pref_profile_loading_app_data));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.spinner_animation));
            }

            mProgressDialog.show();

            // Worst case;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    /*
                     * While we are getting the relevant data, we show a spinner
                     * and continue with our operation afterwards in the UI thread
                     */
                    perApp.getAllApps(perApp.getSystemAppStatus());
                    mPackages = perApp.getPackages();

                    // Map data if present
                    if (savedSelectedProfiles != null) {
                        String tmp[];
                        tmp = savedSelectedProfiles.replace("+", " ").split(" ");

                        // Finds the matches;
                        perApp.findMatch(tmp);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mProgressDialog.dismiss();

                            showPerAppDialog(perApp, profileName, txtViewSummary);
                        }
                    });
                }
            };
            new Thread(runnable).start();
        }
    }

    private void updateStatus(TextView txtView, boolean toggle) {

        if (toggle) {
            txtView.setText(R.string.perAppAssigned);
            txtView.setTextColor(Color.parseColor("#1abc9c"));
        } else {
            txtView.setText(R.string.notperAppAssigned);
            txtView.setTextColor(Color.parseColor("#e74c3c"));
        }

    }

    private void showPerAppDialog(final perAppHelper perApp, final String profileName, final TextView txtViewSummary) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

        final PerAppManager pam = new PerAppManager(mContext, null, perApp);

        dialog.setView(pam);
        dialog.setTitle(R.string.pref_profile_perApp);
        dialog.setIcon(R.drawable.rocket);
        dialog.setCancelable(false);

        dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                String[] packageNames = perApp.getCurrentSelectedPackages();

                String tmp = "";

                if (packageNames != null) {
                    for (String a : packageNames) {
                        tmp = tmp + a + "+";
                    }
                }
                mPerAppPrefs.edit().remove(profileName);

                mPerAppPrefs.edit().putString("systemStatus", perApp.getSystemAppStatus() + "").commit();
                mPerAppPrefs.edit().putString(profileName, tmp).commit();

                if (checkState(profileName)) {
                    updateStatus(txtViewSummary, true);
                } else {
                    updateStatus(txtViewSummary, false);
                }

                // If null, initiate the helper;
                if (AeroActivity.perAppService == null)
                    AeroActivity.perAppService = new PerAppServiceHelper(mContext);

                // User has assigned apps, but no service is running;
                if (!(AeroActivity.perAppService.getState())) {
                    if (checkAllStates() && !mWarning) {
                        AppRate.with(getActivity())
                                .text(R.string.pref_profile_service_not_running)
                                .fromTop(false)
                                .delay(1000)
                                .autoHide(15000)
                                .allowPlayLink(false)
                                .forceShow();
                        mWarning = true;
                    }
                }
                mPerAppDialogVisible = false;
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Do Nothing
                        mPerAppDialogVisible = false;
                    }
                })
                .setNeutralButton(R.string.pref_profile_showSystem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        perApp.setSystemAppStatus(!perApp.getSystemAppStatus());
                        mPerAppPrefs.edit().putString("systemStatus", perApp.getSystemAppStatus() + "").commit();

                        mPerAppDialogVisible = false;

                        getPersistentData(perApp, profileName, txtViewSummary);
                    }

                });

        dialog.create().show();
    }

    private boolean deleteProfile(String ProfileName) {

        final File prefFile = new File(AeroActivity.files.sharedPrefsPath + ProfileName + ".xml");

        mPerAppPrefs.edit().remove(ProfileName).commit();

        //Delete it;
        prefFile.delete();

        // Check if file is gone;
        if(!(prefFile.exists())) {
            return true;
        } else {
            // Now we need to try to delete it with fire
            Log.e(LOG_TAG, "Whoop, it still exists, something went wrong");

            // Delete the file, not just clear the pref;
            final String[] cmd = new String[] {
                    "rm " + "\"" + AeroActivity.files.sharedPrefsPath + ProfileName + ".xml" + "\""
            };

            AeroActivity.shell.setRootInfo(cmd);

            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Something interrupted the main Thread, try again.", e);
            }
            return true;
        }
    }

    private void saveNewProfile(SharedPreferences AeroProfile) {
        // Just to be save, loading default again;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = AeroProfile.edit();

        // Get all our preferences;
        final Map<String,?> allKeys = mPrefs.getAll();

        saveProfile(allKeys, editor);

    }

    private void applyProfile(SharedPreferences AeroProfile) {

        // Just to be save, loading default again;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        // Get all our preferences;
        final Map<String,?> allKeys = AeroProfile.getAll();

        saveProfile(allKeys, editor);
    }

    private void saveProfile(Map<String,?> allKeys, SharedPreferences.Editor editor) {


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

    private  void renameProfile(CharSequence oldName, String newName, TextView txtView, TextView txtViewSummary) {

        final File prefFile = new File(AeroActivity.files.sharedPrefsPath + oldName.toString() + ".xml");

        newName = newName.replace("/", "-");

        prefFile.renameTo(AeroActivity.genHelper.getNewFile(AeroActivity.files.sharedPrefsPath + newName + ".xml"));
        prefFile.delete();

        if (prefFile.exists()) {
            final String[] cmd = new String[] {
                    "mv " + "\"" + AeroActivity.files.sharedPrefsPath + oldName + ".xml" + "\"" + " " + "\"" + AeroActivity.files.sharedPrefsPath + newName + ".xml" + "\""
            };

            AeroActivity.shell.setRootInfo(cmd);
        }

        // We need to delete the "old" preference if there are profiles assigned;
        final String valueOld = mPerAppPrefs.getString(oldName.toString(), null);
        mPerAppPrefs.edit().remove(oldName.toString()).commit();
        mPerAppPrefs.edit().putString(newName, valueOld).commit();

        txtView.setText(newName);
    }

    /*
     * Create a onClick Listener for each profile;
     */

    private void createListener(final TextView txtView, final TextView txtViewSummary) {

        // Get our relative layout parent view first and set the listener
        View v = (View)txtView.getParent();

        // Show the actual profile;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final SharedPreferences AeroProfile = mContext.getSharedPreferences(txtView.getText().toString(), Context.MODE_PRIVATE);
                TextView profileText = new TextView(mContext);
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
                    else if (tmp.contains("/sys/devices/virtual/timed_output/vibrator/"))
                        tmp = tmp.replace("/sys/devices/virtual/timed_output/vibrator/", "vibrator -> ");
                    else if (tmp.contains("/sys/module/msm_thermal/parameters/"))
                        tmp = tmp.replace("/sys/module/msm_thermal/parameters/", "thermal_control -> ");
                    else if (tmp.contains("/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/devfreq/"))
                        tmp = tmp.replace("/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/devfreq/", "");

                    content = tmp + " = " + entry.getValue().toString() + "\n" + content;

                    profileText.setText(content);

                }

                profileText.setVerticalScrollBarEnabled(true);
                profileText.setMovementMethod(new ScrollingMovementMethod());
                profileText.setPadding(20, 20, 20, 20);
                profileText.setTypeface(AeroActivity.files.kitkatFont);

                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle(getText(R.string.slider_overview) + ": " + txtView.getText().toString())
                        .setView(profileText)
                        .setIcon(R.drawable.profile)
                        .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mPrefs = mContext.getSharedPreferences("com.aero.control_preferences", Context.MODE_PRIVATE);
                                deleteProfile("com.aero.control_preferences");
                                SharedPreferences AeroProfile = mContext.getSharedPreferences(txtView.getText().toString(), Context.MODE_PRIVATE);
                                applyProfile(AeroProfile);
                                settings.setSettings(mContext, null);

                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setNeutralButton(R.string.pref_profile_export, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String dir = AeroActivity.files.EXTERNAL_PATH + "/com.aero.control/profiles";
                                String title = txtView.getText().toString() + ".xml";

                                if (!(AeroActivity.genHelper.doesExist(dir)))
                                    if (!(new File(dir).mkdirs()))
                                        Log.e(LOG_TAG, "Couldn't create path: " + dir);

                                try {
                                    AeroActivity.genHelper.copyFile(AeroActivity.genHelper.getNewFile(AeroActivity.files.sharedPrefsPath
                                            + title), AeroActivity.genHelper.getNewFile(dir + "/" + title));
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, "Couldn't copy file: " + AeroActivity.files.sharedPrefsPath
                                            + title, e);
                                }

                                if (AeroActivity.genHelper.doesExist(dir + "/" + title))
                                    Toast.makeText(mContext, R.string.successful, Toast.LENGTH_SHORT).show();

                            }
                        })
                        .create();
                dialog.show();

            }

        });

        // Change the name of the profile;
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mCompleteProfiles = AeroActivity.shell.getDirInfo(AeroActivity.files.sharedPrefsPath, true);
                final EditText editText = new EditText(mContext);
                final CharSequence oldName = txtView.getText();
                editText.setText(oldName);

                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.pref_profile_change_name)
                        .setMessage(R.string.pref_profile_change_name_sum)
                        .setView(editText)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String newName = editText.getText().toString();
                                String allProfiles = Arrays.asList(mCompleteProfiles).toString();

                                if (allProfiles.contains(newName + ".xml")) {
                                    Toast.makeText(mContext, R.string.pref_profile_name_exists, Toast.LENGTH_LONG).show();
                                } else {
                                    txtView.setText(newName);
                                    renameProfile(oldName, newName, txtView, txtViewSummary);
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
            FileInputStream fis = mContext.openFileInput(FILENAME_PROFILES);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_profile_fragment, R.string.showcase_profile_fragment_sum, FILENAME_PROFILES,  R.id.action_add_item);

    }

    public void DrawFirstStart(int header, int content, String filename, Integer id) {

        try {
            FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Could not save file. ", e);
        }

        if (id == null)
            mShowCase = ShowcaseView.insertShowcaseView(150, 200, getActivity(), header, content, mConfigOptions);
        else
            mShowCase = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, id, getActivity(), header, content, mConfigOptions);
    }


}
