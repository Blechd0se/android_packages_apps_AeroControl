package com.aero.control.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.shell.shellScripts;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Memory Fragment
 */
public class MemoryFragment extends PreferenceFragment {

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String SWAPPNIESS_FILE = "/proc/sys/vm/swappiness";

    public Handler progressHandler;

    shellScripts shell = new shellScripts();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.memory_fragment);


        final PreferenceScreen root = this.getPreferenceScreen();
        // I don't like the following, can we simplify it?

        // Declare our entries;
        final EditTextPreference swappiness = (EditTextPreference)root.findPreference("swappiness");
        final EditTextPreference swap = (EditTextPreference)root.findPreference("swap");
        final EditTextPreference compaction = (EditTextPreference)root.findPreference("compaction");
        final EditTextPreference zcache = (EditTextPreference)root.findPreference("zcache");
        final EditTextPreference zcacheCompression = (EditTextPreference)root.findPreference("zcache_compression");

        // Swappiness:
        swappiness.setText(shell.getInfo(SWAPPNIESS_FILE));
        // Only show numbers in input field;
        swappiness.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        // Find our ListPreference (max_frequency);
        final ListPreference io_scheduler = (ListPreference) root.findPreference("io_scheduler_list");
        // Just throw in our frequencies;
        io_scheduler.setEntries(shell.getInfoArray(GOV_IO_FILE, 0, 1));
        io_scheduler.setEntryValues(shell.getInfoArray(GOV_IO_FILE, 0, 1));
        io_scheduler.setValue(shell.getInfoArray(GOV_IO_FILE, 0, 1)[0]);
        io_scheduler.setSummary(shell.getInfoArray(GOV_IO_FILE, 0, 1)[0]);
        io_scheduler.setDialogIcon(R.drawable.memory_dark);

        final Preference fstrim_toggle = root.findPreference("fstrim_toggle");


        fstrim_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                final CharSequence[] system = {"/data", "/cache"};


                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final ProgressDialog update = new ProgressDialog(getActivity());
                builder.setTitle("Trim Options");
                builder.setItems(system, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        final String b = (String)system[item];

                        update.setTitle("Trimming...");
                        update.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        update.setCancelable(false);
                        update.setMax(100);
                        update.show();


                        Thread background = new Thread (new Runnable() {
                            public void run() {
                                try {


                                    while (update.getProgress()<= update.getMax()) {
                                        // wait 500ms between each update
                                        //Thread.sleep(500);

                                        shell.getRootInfo("fstrim -v", b);

                                        update.setProgress(100);
                                        // active the update handler

                                        if(update.getProgress()== 100)
                                        {
                                            update.setTitle("Successful!");
                                            Thread.sleep(1000);
                                            update.dismiss();
                                            Thread.interrupted();
                                        }
                                        progressHandler.sendMessage(progressHandler.obtainMessage());

                                    }

                                } catch (Exception e) {
                                    // if something fails do something smart
                                }
                            }
                        });


                        // start the background thread
                        background.start();

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

        swap.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Changeing Swap is currently not supported.", Toast.LENGTH_LONG).show();

                return true;
            };
        });

        compaction.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Changeing compaction is currently not supported.", Toast.LENGTH_LONG).show();

                return true;
            };
        });

        zcache.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Changeing zCache is currently not supported.", Toast.LENGTH_LONG).show();

                return true;
            };
        });

        zcacheCompression.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Changeing zCache Compression is currently not supported.", Toast.LENGTH_LONG).show();

                return true;
            };
        });
    }

}

