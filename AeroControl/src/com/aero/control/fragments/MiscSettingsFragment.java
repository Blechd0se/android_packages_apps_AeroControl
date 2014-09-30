package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.CustomListPreference;
import com.aero.control.helpers.FileManagerListener;
import com.aero.control.helpers.FileManager;
import com.aero.control.helpers.PreferenceHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Alexander Christ on 03.04.14.
 */
public class MiscSettingsFragment extends PreferenceFragment implements FileManagerListener {

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private PreferenceCategory mMiscCat;
    private PreferenceHandler mHandler;
    private ArrayList<String> mParaList;
    private ArrayList<String> mNameList;
    private SharedPreferences mPrefs;
    private SharedPreferences mMiscSettings;
    private FileManager mLocalFolders;
    private Dialog mFileDialog;
    private Context mContext;

    private static final String MISC_SETTINGS_STORAGE = "miscSettingsStorage";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();

        this.mContext = getActivity();

        // Always rebuild;
        mMiscSettings = mContext.getSharedPreferences(MISC_SETTINGS_STORAGE, mContext.MODE_PRIVATE);

        // Load parameter data:
        loadParalist();

        // Load our custom preferences;
        loadSettings();
    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

        inflater.inflate(R.menu.misc_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:

                // Setup our file manager;
                mLocalFolders = new FileManager(mContext, null);
                mLocalFolders.setIFolderItemListener(this);
                mLocalFolders.setDir("/");

                if (mFileDialog == null) {
                    mFileDialog = new Dialog(mContext);
                    ViewGroup.LayoutParams abc = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mFileDialog.addContentView(mLocalFolders, abc);

                    // In case we are just creating the dialog, fill it with the root-path;
                    mFileDialog.setTitle("/");
                    mLocalFolders.setDialog(mFileDialog);
                }
                mFileDialog.show();

                break;
            case R.id.action_delete_item:

                if (mMiscCat == null)
                    break;

                final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                final ArrayList<String> allMiscSettings = new ArrayList<String>();
                final ArrayList<Boolean> miscSettingsDelete = new ArrayList<Boolean>();
                for (int i = 0; i < mMiscCat.getPreferenceCount(); i++) {
                    allMiscSettings.add(mMiscCat.getPreference(i).getTitle().toString());
                }
                if (allMiscSettings.size() == 0)
                    break;

                // Fill with default data;
                for (String a : allMiscSettings) {
                    miscSettingsDelete.add(false);
                }

                final String[] preferenceData = allMiscSettings.toArray(new String[0]);

                dialog.setTitle(R.string.pref_misc_delete_misc);
                dialog.setIcon(R.drawable.warning);
                dialog.setMultiChoiceItems(preferenceData, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b) {
                            miscSettingsDelete.add(i, true);
                        } else {
                            miscSettingsDelete.add(i, false);
                        }
                    }
                })
                        // Set the action buttons
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                SharedPreferences.Editor editor = mMiscSettings.edit();
                                SharedPreferences.Editor aero_editor = mPrefs.edit();
                                final Map<String,?> keys = mMiscSettings.getAll();

                                int i = 0;
                                for (String s : preferenceData) {
                                    if (miscSettingsDelete.get(i)) {
                                        for (final Map.Entry<String,?> entry : keys.entrySet()) {

                                            // Delete our marked entries;

                                            String key = entry.getKey();
                                            String value = entry.getValue().toString();

                                            if (preferenceData[i].equals(value)) {
                                                editor.remove(key).commit();
                                                aero_editor.remove(key).commit();
                                            }
                                        }
                                    }
                                    i++;
                                }
                                // Re-init to rebuild UI;
                                root.removePreference(mMiscCat);
                                mMiscCat = null;
                                initMisc();
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
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OnCannotFileRead(File file) { }

    public void OnFileClicked(File file) {

        mHandler.genPrefFromSingleFile(file.toString());

        String[] array = file.toString().split("/");
        String paraName = "";
        int i = 0;

        for (String a : array) {
            if (array.length - 1 == i)
                paraName = a;
            i++;
        }

        mMiscSettings.edit().putString(file.toString(), paraName).commit();
        root.addPreference(mMiscCat);

        mFileDialog.dismiss();
    }

    /*
     * Basically inits everything and maps data;
     */
    private void initMisc() {

        final Map<String,?> keys = mMiscSettings.getAll();
        int i = 0;
        boolean forceAdd = false;

        if (mMiscCat == null) {
            mMiscCat = new PreferenceCategory(mContext);
            mMiscCat.setTitle(R.string.pref_misc_your_settings);
            root.addPreference(mMiscCat);

            mHandler = new PreferenceHandler(mContext, mMiscCat, getPreferenceManager());

            // Load our saved data;
            for (final Map.Entry<String,?> entry : keys.entrySet()) {
                String key = entry.getKey();
                mHandler.genPrefFromSingleFile(key);
                i++;
            }
        } else {
            root.addPreference(mMiscCat);
            if (mMiscCat.getPreferenceCount() != 0)
                forceAdd = true;
        }

        if (mHandler == null) {
            mHandler = new PreferenceHandler(mContext, mMiscCat, getPreferenceManager());
        }

        if (i == 0 && !forceAdd) {
            root.removePreference(mMiscCat);
        }

    }

    private void loadParalist() {

        mParaList = new ArrayList<String>();
        mNameList = new ArrayList<String>();

        mNameList.add("vtg_level");
        mParaList.add(AeroActivity.files.MISC_VIBRATOR_CONTROL);

        mNameList.add("amp");
        mParaList.add(AeroActivity.files.MISC_VIBRATOR_CONTROL);

        mNameList.add("temp_threshold");
        mParaList.add(AeroActivity.files.MISC_THERMAL_CONTROL);
    }

    public void loadSettings() {

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);

        final CustomListPreference tcpPreference = new CustomListPreference(mContext);

        PrefCat = new PreferenceCategory(mContext);
        PrefCat.setTitle(R.string.pref_misc_settings);
        root.addPreference(PrefCat);

        initMisc();

        try {

            PreferenceHandler h = new PreferenceHandler(mContext, PrefCat, getPreferenceManager());

            h.genPrefFromFiles(mNameList.toArray(new String[0]), mParaList.toArray(new String[0]), false);

        } catch (NullPointerException e) {
            Log.e("Aero", "I couldn't get any files!", e);
        }

        // Needed for set-on-boot;
        tcpPreference.setName("tcp_congestion");
        tcpPreference.setTitle(R.string.pref_misc_tcp_congestion);
        tcpPreference.setDialogTitle(R.string.pref_misc_tcp_congestion);
        tcpPreference.setSummary(AeroActivity.shell.getInfo(AeroActivity.files.MISC_TCP_CONGESTION_CURRENT));
        tcpPreference.setValue(AeroActivity.shell.getInfo(AeroActivity.files.MISC_TCP_CONGESTION_CURRENT));
        tcpPreference.setEntries(AeroActivity.shell.getInfoArray(AeroActivity.files.MISC_TCP_CONGESTION_AVAILABLE, 0, 0));
        tcpPreference.setEntryValues(AeroActivity.shell.getInfoArray(AeroActivity.files.MISC_TCP_CONGESTION_AVAILABLE, 0, 0));

        if (AeroActivity.genHelper.doesExist(AeroActivity.files.MISC_TCP_CONGESTION_AVAILABLE))
            PrefCat.addPreference(tcpPreference);

        tcpPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;

                AeroActivity.shell.setRootInfo(a, AeroActivity.files.MISC_TCP_CONGESTION_CURRENT);
                tcpPreference.setSummary(a);

                return true;
            }
        });
    }
}