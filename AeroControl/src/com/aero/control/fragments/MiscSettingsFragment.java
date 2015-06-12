package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.CustomListPreference;
import com.aero.control.helpers.FileManager.FileManagerListener;
import com.aero.control.helpers.FileManager.FileManager;
import com.aero.control.helpers.FilePath;
import com.aero.control.helpers.PreferenceHandler;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Alexander Christ on 03.04.14.
 */
public class MiscSettingsFragment extends PlaceHolderFragment implements FileManagerListener {

    public static final String FILENAME_MISC = "firstrun_misc";
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
    private ShowcaseView mShowCase;


    private static final String MISC_SETTINGS_STORAGE = "miscSettingsStorage";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.empty_preference);
        root = this.getPreferenceScreen();

        mContext = getActivity();

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
                for (int i = 0; i < mMiscCat.getPreferenceCount() - 1; i++) {
                    allMiscSettings.add(mMiscCat.getPreference(i).getTitle().toString());
                }
                if (allMiscSettings.size() == 0) {
                    Toast.makeText(mContext, R.string.pref_misc_no_settings, Toast.LENGTH_LONG).show();
                    break;
                }

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // Set up our file;
        int output = 0;

        if (AeroActivity.genHelper.doesExist(getActivity().getFilesDir().getAbsolutePath() + "/" + FILENAME_MISC)) {
            output = 1;
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_your_settings, R.string.showcase_your_settings_sum);

    }

    public void DrawFirstStart(int header, int content) {

        try {
            final FileOutputStream fos = getActivity().openFileOutput(FILENAME_MISC, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        Target homeTarget = new Target() {
            @Override
            public Point getPoint() {
                // Get approximate position of overflow action icon's center
                int actionBarSize = getActivity().findViewById(R.id.action_add_item).getHeight();
                int x = getResources().getDisplayMetrics().widthPixels - actionBarSize / 2;
                int y = actionBarSize / 2;
                return new Point(x, y);
            }
        };

        mShowCase = new ShowcaseView.Builder(getActivity())
                .setContentTitle(header)
                .setContentText(content)
                .setTarget(homeTarget)
                .build();
    }

    public void OnCannotFileRead(File file) { }

    public void OnFileClicked(File file) {

        // Sanity-check; is this tunable already added?
        for (int i = 0; i < mMiscCat.getPreferenceCount() - 1; i++) {
            if (file.toString().contains(mMiscCat.getPreference(i).getTitle().toString())) {
                Toast.makeText(mContext, "This tunable was already added!", Toast.LENGTH_LONG).show();
                mFileDialog.dismiss();
                return;
            }

        }

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
        mHandler.addInvisiblePreference();

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

        mHandler.addInvisiblePreference();

        if (i == 0 && !forceAdd) {
            root.removePreference(mMiscCat);
        }

    }

    private void loadParalist() {

        mParaList = new ArrayList<String>();
        mNameList = new ArrayList<String>();

        mNameList.add("vtg_level");
        mParaList.add(FilePath.MISC_VIBRATOR_CONTROL);

        mNameList.add("amp");
        mParaList.add(FilePath.MISC_VIBRATOR_CONTROL);

        mNameList.add("temp_threshold");
        mParaList.add(FilePath.MISC_THERMAL_CONTROL);

        mNameList.add("volume_boost");
        mParaList.add(FilePath.MISC_HEADSET_VOLUME_BOOST);

        setHasOptionsMenu(true);
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
        tcpPreference.setSummary(AeroActivity.shell.getInfo(FilePath.MISC_TCP_CONGESTION_CURRENT));
        tcpPreference.setValue(AeroActivity.shell.getInfo(FilePath.MISC_TCP_CONGESTION_CURRENT));
        tcpPreference.setEntries(AeroActivity.shell.getInfoArray(FilePath.MISC_TCP_CONGESTION_AVAILABLE, 0, 0));
        tcpPreference.setEntryValues(AeroActivity.shell.getInfoArray(FilePath.MISC_TCP_CONGESTION_AVAILABLE, 0, 0));

        if (AeroActivity.genHelper.doesExist(FilePath.MISC_TCP_CONGESTION_AVAILABLE))
            PrefCat.addPreference(tcpPreference);

        tcpPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;

                AeroActivity.shell.setRootInfo(a, FilePath.MISC_TCP_CONGESTION_CURRENT);
                tcpPreference.setSummary(a);

                return true;
            }
        });
    }
}