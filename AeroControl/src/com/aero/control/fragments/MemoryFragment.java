package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;
import com.espian.showcaseview.ShowcaseView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Memory Fragment
 */
public class MemoryFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String DYANMIC_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String CMDLINE_ZACHE = "/system/bootmenu/2nd-boot/cmdline";
    public static final String WRITEBACK = "/sys/devices/virtual/misc/writeback/writeback_enabled";
    public static final String LOW_MEM = "/system/build.prop";
    public static final String FILENAME = "firstrun_trim";
    public static final String FILENAME_HIDDEN = "firstrun_hidden_feature";
    public static final String GOV_IO_PARAMETER = "/sys/block/mmcblk0/queue/iosched";

    public ShowcaseView.ConfigOptions mConfigOptions;
    public ShowcaseView mShowCase;
    public PreferenceCategory PrefCat;
    public PreferenceScreen root;
    private SharedPreferences prefs;

    public boolean showDialog = true;

    public static final Handler progressHandler = new Handler();

    private CheckBoxPreference mDynFSync, mZCache, mLowMemoryPref, mWriteBackControl;
    private Preference mFSTrimToggle, mDalvikSettings;
    private ListPreference mIOScheduler;
    private String mFileSystem;

    private static final String MEMORY_SETTINGS_CATEGORY = "memory_settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.memory_fragment);
        setHasOptionsMenu(true);

        root = this.getPreferenceScreen();
        final PreferenceCategory memorySettingsCategory =
                (PreferenceCategory) findPreference(MEMORY_SETTINGS_CATEGORY);

        mDynFSync = (CheckBoxPreference) findPreference("dynFsync");
        if ("1".equals(AeroActivity.shell.getInfo(DYANMIC_FSYNC))) {
            mDynFSync.setChecked(true);
        } else if ("0".equals(AeroActivity.shell.getInfo(DYANMIC_FSYNC))) {
            mDynFSync.setChecked(false);
        } else {
            if (memorySettingsCategory != null) memorySettingsCategory.removePreference(mDynFSync);
        }

        mZCache = (CheckBoxPreference) findPreference("zcache");
        if ("Unavailable".equals(AeroActivity.shell.getInfo(CMDLINE_ZACHE))) {
            if (memorySettingsCategory != null) memorySettingsCategory.removePreference(mZCache);
        } else {
            final String fileCMD = AeroActivity.shell.getInfo(CMDLINE_ZACHE);
            final boolean zcacheEnabled = fileCMD.length() != 0 && fileCMD.contains("zcache");
            mZCache.setChecked(zcacheEnabled);
        }

        mWriteBackControl = (CheckBoxPreference) findPreference("writeback");
        if ("1".equals(AeroActivity.shell.getInfo(WRITEBACK))) {
            mWriteBackControl.setChecked(true);
        } else if ("0".equals(AeroActivity.shell.getInfo(WRITEBACK))) {
            mWriteBackControl.setChecked(false);
        } else {
            if (memorySettingsCategory != null)
                memorySettingsCategory.removePreference(mWriteBackControl);
        }

        mLowMemoryPref = (CheckBoxPreference) findPreference("low_mem");
        mFSTrimToggle = findPreference("fstrim_toggle");
        mDalvikSettings = findPreference("dalvik_settings");

        mIOScheduler = (ListPreference) findPreference("io_scheduler_list");
        mIOScheduler.setEntries(AeroActivity.shell.getInfoArray(GOV_IO_FILE, 0, 1));
        mIOScheduler.setEntryValues(AeroActivity.shell.getInfoArray(GOV_IO_FILE, 0, 1));
        mIOScheduler.setValue(AeroActivity.shell.getInfoString(AeroActivity.shell.getInfo(GOV_IO_FILE)));
        mIOScheduler.setSummary(AeroActivity.shell.getInfoString(AeroActivity.shell.getInfo(GOV_IO_FILE)));
        mIOScheduler.setDialogIcon(R.drawable.memory_dark);
        mIOScheduler.setOnPreferenceChangeListener(this);

        if (showDialog) {
            // Ensure only devices with this special path are checked;
            final String fileMount[] = AeroActivity.shell.getInfo("/proc/mounts", false);
            boolean fileMountCheck = false;

            for (String tmp : fileMount) {
                if (tmp.contains("/dev/block/mmcblk1p25")) {
                    fileMountCheck = true;
                    break;
                }
            }

            showDialog = false;

            if (fileMountCheck) {
                final String fileJournal = AeroActivity.shell.getRootInfo("tune2fs -l", "/dev/block/mmcblk1p25");
                final boolean fileSystemCheck = fileJournal.length() != 0 && fileJournal.contains("has_journal");
                if (!fileSystemCheck) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    // Just reuse aboutScreen, because its Linear and has a TextView
                    View layout = inflater.inflate(R.layout.about_screen, null);
                    TextView aboutText = (TextView) (layout != null ? layout.findViewById(R.id.aboutScreen) : null);
                    builder.setTitle(R.string.has_journal_dialog_header);
                    if (aboutText != null) {
                        aboutText.setText(getText(R.string.has_journal_dialog));
                        aboutText.setTextSize(13);
                    }
                    builder.setView(layout)
                            .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    builder.show();
                }
            }
        }
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
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_memory_fragment_trim, R.string.showcase_memory_fragment_trim_sum, FILENAME);

    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String a = prefs.getString("app_theme", null);

        if (a == null)
            a = "";

        if (a.equals("red"))
            inflater.inflate(R.menu.memory_menu, menu);
        else if (a.equals("light"))
            inflater.inflate(R.menu.memory_menu, menu);
        else if (a.equals("dark"))
            inflater.inflate(R.menu.memory_menu_light, menu);
        else
            inflater.inflate(R.menu.memory_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_io_settings:
                loadIOParameter();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLowMemoryPref) {
            lowMemoryPrefClick();
        } else if (preference == mDynFSync) {
            boolean value = mDynFSync.isChecked();
            if (value) AeroActivity.shell.setRootInfo("1", DYANMIC_FSYNC);
            else AeroActivity.shell.setRootInfo("0", DYANMIC_FSYNC);
        } else if (preference == mZCache) {
            zCacheClick();
        } else if (preference == mWriteBackControl) {
            boolean value = mWriteBackControl.isChecked();
            if (value) AeroActivity.shell.setRootInfo("1", WRITEBACK);
            else AeroActivity.shell.setRootInfo("0", WRITEBACK);
        } else if (preference == mFSTrimToggle) {
            fsTrimToggleClick();
        } else if (preference == mDalvikSettings) {
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.content_frame, new MemoryDalvikFragment())
                    .addToBackStack("Memory")
                    .commit();
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        preference.getEditor().commit();
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mIOScheduler) {
            String value = (String) newValue;
            mIOScheduler.setSummary(value);
            AeroActivity.shell.setRootInfo(value, GOV_IO_FILE);

            // Kill everything with fire;
            if (PrefCat != null)
                root.removePreference(PrefCat);
        } else {
            return false;
        }
        preference.getEditor().commit();
        return true;
    }

    private void zCacheClick() {
        String getState = AeroActivity.shell.getInfo(CMDLINE_ZACHE);
        boolean value = mZCache.isChecked();
        AeroActivity.shell.remountSystem();
        if (value) {
            // If already on, we can bail out;
            if (getState.contains("zcache")) return;
            getState = getState + " zcache";
        } else {
            // bail out again, because its already how we want it;
            if (!getState.contains("zcache")) return;
            getState = getState.replace(" zcache", "");
        }
        // Set current State to path;
        AeroActivity.shell.setRootInfo(getState, CMDLINE_ZACHE);
        Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_LONG).show();
    }

    private void lowMemoryPrefClick() {
        String getState = null;
        boolean value = mLowMemoryPref.isChecked();
        AeroActivity.shell.remountSystem();
        try {
            final BufferedReader br = new BufferedReader(new FileReader(LOW_MEM));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }
                getState = sb.toString();
                if (value) {
                    // If already on, we can bail out;
                    if (getState.contains("ro.config.low_ram=true")) return;
                    getState = getState.replace("ro.config.low_ram=false", "ro.config.low_ram=true");
                } else {
                    // bail out again, because its already how we want it;
                    if (getState.contains("ro.config.low_ram=false")) return;
                    getState = getState.replace("ro.config.low_ram=true", "ro.config.low_ram=false");
                }
            } catch (IOException ignored) {
            }
        } catch (FileNotFoundException ignored) {
        }
        // Set current State to path;
        AeroActivity.shell.setRootInfo(getState, LOW_MEM);
        Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_LONG).show();
    }

    private void fsTrimToggleClick() {

        if (mFileSystem == null)
            mFileSystem = AeroActivity.shell.getRootInfo("mount", "");

        final CharSequence[] system = {" /system ", " /data ", " /cache "};
        final ArrayList<String> fs = new ArrayList<String>();

        int tmp;
        int count = 0;
        String temp;

        for (CharSequence a : system) {
            if (mFileSystem.contains(a)) {
                tmp = mFileSystem.indexOf(a.toString());
                temp = mFileSystem.substring(tmp, tmp + a.length() + 4).replace(a, "");

                if (temp.equals("ext3") || temp.equals("ext4")) {
                    fs.add(a.toString());
                    count++;
                }
            } else {
                continue;
            }
        }
        final CharSequence[] fsystem = fs.toArray(new CharSequence[0]);

        // If the device doesn't support trimable filesystems;
        if (count == 0) {
            Toast.makeText(getActivity(), R.string.unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ProgressDialog update = new ProgressDialog(getActivity());
        builder.setTitle(R.string.fstrim_header);
        builder.setIcon(R.drawable.gear_dark);
        builder.setItems(fsystem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final String b = (String)fsystem[item];
                update.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                update.setCancelable(false);
                update.setMax(100);
                update.setIndeterminate(true);
                update.show();
                AeroActivity.shell.remountSystem();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (update.getProgress()< 100) {
                                // Set up the root-command;
                                AeroActivity.shell.getRootInfo("fstrim -v", b);
                                update.setIndeterminate(false);
                                update.setProgress(100);
                                progressHandler.sendMessage(progressHandler.obtainMessage());
                                // Sleep the current thread and exit dialog;
                                Thread.sleep(2000);
                                update.dismiss();
                            }
                        } catch (Exception e) {
                            Log.e("Aero", "An error occurred while trimming.", e);
                        }
                    }
                };
                Thread trimThread = new Thread(runnable);
                if (!trimThread.isAlive()) trimThread.start();
            }
        }).show();
    }

    public void DrawFirstStart(int header, int content, String filename) {

        try {
            FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        mShowCase = ShowcaseView.insertShowcaseView(150, 730, getActivity(), header, content, mConfigOptions);
    }

    private void loadIOParameter() {

        try {
            String completeParamterList[] = AeroActivity.shell.getDirInfo(GOV_IO_PARAMETER, true);

            // If there are already some entries, kill them all (with fire)
            if (PrefCat != null)
                root.removePreference(PrefCat);

            PrefCat = new PreferenceCategory(getActivity());
            PrefCat.setTitle(R.string.pref_io_scheduler);
            root.addPreference(PrefCat);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("Aero", "Something interrupted the main Thread, try again.", e);
            }

            PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

            h.genPrefFromDictionary(completeParamterList, GOV_IO_PARAMETER);

            // Probably the wrong place, should be in getDirInfo ?
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Looks like there are no parameter for this governor?", Toast.LENGTH_LONG).show();
            Log.e("Aero", "There isn't any folder i can check. Does this governor has parameters?", e);
        }
    }
}

