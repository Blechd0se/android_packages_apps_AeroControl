package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.helpers.shellHelper;
import com.espian.showcaseview.ShowcaseView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Memory Fragment
 */
public class MemoryFragment extends PreferenceFragment {

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String SWAPPNIESS_FILE = "/proc/sys/vm/swappiness";
    public static final String DYANMIC_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String CMDLINE_ZACHE = "/system/bootmenu/2nd-boot/cmdline";
    public static final String WRITEBACK = "/sys/devices/virtual/misc/writeback/writeback_enabled";
    public static final String MIN_FREE = "/proc/sys/vm/extra_free_kbytes";

    public ShowcaseView.ConfigOptions mConfigOptions;
    public ShowcaseView mShowCase;

    public boolean showDialog = true;

    public boolean checkDynFsync;
    public boolean checkDynWriteback;

    public Handler progressHandler;

    shellHelper shell = new shellHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.memory_fragment);


        final PreferenceScreen root = this.getPreferenceScreen();
        // I don't like the following, can we simplify it?

        // Declare our entries;
        final EditTextPreference swappiness = (EditTextPreference)root.findPreference("swappiness");
        final CheckBoxPreference dynFsync = (CheckBoxPreference)root.findPreference("dynFsync");
        final CheckBoxPreference zcache = (CheckBoxPreference)root.findPreference("zcache");
        final CheckBoxPreference writeback_control = (CheckBoxPreference)root.findPreference("writeback");
        final EditTextPreference min_free_ram = (EditTextPreference)root.findPreference("min_free");

        // Swappiness:
        swappiness.setText(shell.getInfo(SWAPPNIESS_FILE));
        // Only show numbers in input field;
        swappiness.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        // Min free ram:
        min_free_ram.setText(shell.getInfo(MIN_FREE));
        // Only show numbers in input field;
        min_free_ram.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        if (shell.getInfo(CMDLINE_ZACHE).equals("Unavailable"))
            zcache.setEnabled(false);

        if (shell.getInfo(MIN_FREE).equals("Unavailable"))
            min_free_ram.setEnabled(false);


        // Check if enabled or not;
        if (shell.getInfo(DYANMIC_FSYNC).equals("1")) {
            checkDynFsync = true;
        }
        else if (shell.getInfo(DYANMIC_FSYNC).equals("0")) {
            checkDynFsync = false;
        }
        else {
            dynFsync.setEnabled(false); // If dyn fsync is not supported
        }

        dynFsync.setChecked(checkDynFsync);

        final String fileCMD = shell.getInfo(CMDLINE_ZACHE);
        final boolean zcacheEnabled = fileCMD.length() == 0 ? false : fileCMD.contains("zcache");
        zcache.setChecked(zcacheEnabled);

        // Check if enabled or not;
        if (shell.getInfo(WRITEBACK).equals("1")) {
            checkDynWriteback = true;
        }
        else if (shell.getInfo(WRITEBACK).equals("0")) {
            checkDynWriteback = false;
        }
        else {
            writeback_control.setEnabled(false); // If dyn writeback is not supported
        }
        writeback_control.setChecked(checkDynWriteback);


        writeback_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                String a =  o.toString();

                if (a.equals("true"))
                    shell.setRootInfo("1", WRITEBACK);
                else if (a.equals("false"))
                    shell.setRootInfo("0", WRITEBACK);

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        if (showDialog) {
        // Ensure only devices with this special path are checked;
        final String fileMount = shell.getRootInfo("mount", "");
        final boolean fileMountCheck = fileMount.length() == 0 ? false : fileMount.contains("/dev/block/mmcblk1p25");
            showDialog = false;

            if (fileMountCheck) {
                final String fileJournal = shell.getRootInfo("tune2fs -l", "/dev/block/mmcblk1p25");
                final boolean fileSystemCheck = fileJournal.length() == 0 ? false : fileJournal.contains("has_journal");
                if (!fileSystemCheck){

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    // Just reuse aboutScreen, because its Linear and has a TextView
                    View layout = inflater.inflate(R.layout.about_screen, null);
                    TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                    builder.setTitle(R.string.has_journal_dialog_header);
                    aboutText.setText(getText(R.string.has_journal_dialog));
                    aboutText.setTextSize(13);

                    builder.setView(layout)
                            .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
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

        // Find our ListPreference (max_frequency);
        final ListPreference io_scheduler = (ListPreference) root.findPreference("io_scheduler_list");
        // Just throw in our frequencies;
        io_scheduler.setEntries(shell.getInfoArray(GOV_IO_FILE, 0, 1));
        io_scheduler.setEntryValues(shell.getInfoArray(GOV_IO_FILE, 0, 1));
        io_scheduler.setValue(shell.getInfoString(shell.getInfo(GOV_IO_FILE)));
        io_scheduler.setSummary(shell.getInfoString(shell.getInfo(GOV_IO_FILE)));
        io_scheduler.setDialogIcon(R.drawable.memory_dark);

        final Preference fstrim_toggle = root.findPreference("fstrim_toggle");


        fstrim_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                final CharSequence[] system = {"/system", "/data", "/cache"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final ProgressDialog update = new ProgressDialog(getActivity());
                builder.setTitle(R.string.fstrim_header);
                builder.setIcon(R.drawable.gear_dark);

                builder.setItems(system, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        final String b = (String)system[item];


                        update.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        update.setCancelable(true);
                        update.setMax(100);
                        update.setIndeterminate(true);
                        update.show();
                        shell.remountSystem();


                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    while (update.getProgress()<= 100) {

                                        // Set up the root-command;
                                        shell.getRootInfo("fstrim -v", b);

                                        update.setIndeterminate(false);
                                        update.setProgress(100);

                                        progressHandler.sendMessage(progressHandler.obtainMessage());

                                    }

                                } catch (Exception e) {
                                    Log.e("Aero", "Either an error occurred or trimming was successful.", e);
                                }
                            }
                        };
                        Thread trimThread = new Thread(runnable);
                        trimThread.start();

                    }
                }).show();


                return true;
            };

        });


        // Start our custom change listener;
        io_scheduler.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;

                shell.setRootInfo(a, GOV_IO_FILE);
                io_scheduler.setSummary(a);

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        swappiness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;


                shell.setRootInfo(a, SWAPPNIESS_FILE);
                swappiness.setText(shell.getInfo(SWAPPNIESS_FILE));

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        min_free_ram.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;


                shell.setRootInfo(a, MIN_FREE);
                swappiness.setText(shell.getInfo(MIN_FREE));

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        dynFsync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                String a =  o.toString();

                if (a.equals("true"))
                    shell.setRootInfo("1", DYANMIC_FSYNC);
                else if (a.equals("false"))
                    shell.setRootInfo("0", DYANMIC_FSYNC);

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
        });

        zcache.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String getState = shell.getInfo(CMDLINE_ZACHE);
                String a =  o.toString();

                shell.remountSystem();

                // It's checked, so we can enable zcache;
                if (a.equals("true")) {

                    // If already on, we can bail out;
                    if (getState.contains("zcache"))
                        return true;

                    getState = getState + " zcache";

                }
                else if (a.equals("false")) {

                    // bail out again, because its already how we want it;
                    if (!getState.contains("zcache"))
                        return true;

                    getState = getState.replace(" zcache", "");

                }

                // Set current State to path;
                shell.setRootInfo(getState, CMDLINE_ZACHE);
                Toast.makeText(getActivity(), R.string.need_reboot, Toast.LENGTH_LONG).show();

                //** store preferences
                preference.getEditor().commit();

                return true;
            };
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
        String FILENAME = "firstrun_trim";
        int output = 0;
        byte[] buffer = new byte[1024];

        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_memory_fragment_trim, R.string.showcase_memory_fragment_trim_sum);

    }

    public void DrawFirstStart(int header, int content) {

        String FILENAME = "firstrun_trim";
        String string = "1";

        try {
            FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        mDrawerLayout.openDrawer(mDrawerLayout)
        mShowCase = ShowcaseView.insertShowcaseView(130, 600, getActivity(), header, content, mConfigOptions);
    }

}

