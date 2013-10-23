package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.helpers.shellHelper;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Memory Fragment
 */
public class MemoryFragment extends PreferenceFragment {

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String SWAPPNIESS_FILE = "/proc/sys/vm/swappiness";
    public static final String DYANMIC_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String CMDLINE_ZACHE = "/system/bootmenu/2nd-boot/cmdline";

    public boolean showDialog = false;

    public boolean checkDynFsync;

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

        // Swappiness:
        swappiness.setText(shell.getInfo(SWAPPNIESS_FILE));
        // Only show numbers in input field;
        swappiness.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        if (shell.getInfo(CMDLINE_ZACHE).equals("Unavailable"))
            zcache.setEnabled(false);

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

        // Ensure only devices with this special path are checked;
        final String fileMount = shell.getRootInfo("mount", "");
        final boolean fileMountCheck = fileMount.length() == 0 ? false : fileMount.contains("/dev/block/mmcblk1p25");

        if (!showDialog && fileMountCheck) {
            final String fileJournal = shell.getRootInfo("tune2fs -l", "/dev/block/mmcblk1p25");
            final boolean fileSystemCheck = fileJournal.length() == 0 ? false : fileJournal.contains("has_journal");
            if (!fileSystemCheck){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                // Just reuse aboutScreen, because its Linear and has a TextView
                View layout = inflater.inflate(R.layout.about_screen, null);
                TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

                builder.setTitle("Problem detected!");
                aboutText.setText(Html.fromHtml("Looks like your /data partition doesn't have the 'has_journal' Feature enabled. " +
                        "Please reboot into recovery and run the following command via adb shell: <br> <br>" +
                        "<b>tune2fs -O has_journal /dev/block/mmcblk1p25</b>"));
                aboutText.setTextSize(13);

                builder.setView(layout)
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showDialog = true;
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showDialog = true;
                            }
                        });
                builder.show();
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

                final CharSequence[] system = {"/data", "/cache"};


                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final ProgressDialog update = new ProgressDialog(getActivity());
                builder.setTitle("Trim Options");
                builder.setIcon(R.drawable.gear_dark);

                builder.setItems(system, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        final String b = (String)system[item];

                        update.setTitle("Trim");
                        update.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        update.setCancelable(true);
                        update.setMax(100);
                        update.setIndeterminate(true);
                        update.show();


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


                       // Toast.makeText(getActivity(), "Trimming in progress...", Toast.LENGTH_LONG).show();
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

                return true;
            };
        });

        swappiness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;


                shell.setRootInfo(a, SWAPPNIESS_FILE);
                swappiness.setText(shell.getInfo(SWAPPNIESS_FILE));

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
                Toast.makeText(getActivity(), "This may require a reboot.", Toast.LENGTH_LONG).show();

                return true;
            };
        });

    }

}

