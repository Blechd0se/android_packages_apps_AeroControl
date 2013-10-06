package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.aero.control.R;
import com.aero.control.shell.shellScripts;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Memory Fragment
 */
public class MemoryFragment extends PreferenceFragment {

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";

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

        // Find our ListPreference (max_frequency);
        final ListPreference io_scheduler = (ListPreference) root.findPreference("io_scheduler_list");
        // Just throw in our frequencies;
        io_scheduler.setEntries(shell.getInfoArray(GOV_IO_FILE, 0, 1));
        io_scheduler.setEntryValues(shell.getInfoArray(GOV_IO_FILE, 0, 1));
        io_scheduler.setValue(shell.getInfoArray(GOV_IO_FILE, 0, 1)[0]);
        io_scheduler.setSummary(shell.getInfoArray(GOV_IO_FILE, 0, 1)[0]);
        io_scheduler.setDialogIcon(R.drawable.memory_dark);


        // Start our custom change listener;
        io_scheduler.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Changeing the IO Scheduler is currently not supported.", Toast.LENGTH_LONG).show();

                return true;
            };
        });

        swappiness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {


                Toast.makeText(getActivity(), "Changeing swappiness is currently not supported.", Toast.LENGTH_LONG).show();

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

