package com.aero.control.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.EditText;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.PreferenceHandler;
import com.espian.showcaseview.ShowcaseView;

/**
 * Created by ac on 03.10.13.
 *
 * TODO: Simplify those static strings
 *
 */
public class CPUFragment extends PreferenceFragment {


    public static final String CPU_BASE_PATH = "/sys/devices/system/cpu/cpu";
    public static final String CPU_AVAILABLE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String ALL_GOV_AVAILABLE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String CURRENT_GOV_AVAILABLE = "/cpufreq/scaling_governor";
    public static final String CPU_MAX_FREQ = "/cpufreq/scaling_max_freq";
    public static final String CPU_MIN_FREQ = "/cpufreq/scaling_min_freq";
    public static final String CPU_GOV_SET_BASE = "/sys/devices/system/cpu/cpufreq/";
    public static final String CPU_VSEL = "/proc/overclock/mpu_opps";
    public static final String CPU_VSEL_MAX = "/proc/overclock/max_vsel";
    public static final String CPU_MAX_RATE = "/proc/overclock/max_rate";
    public static final String CPU_FREQ_TABLE = "/proc/overclock/freq_table";
    public static final String FILENAME = "firstrun_cpu";

    public PreferenceScreen root;
    public PreferenceCategory PrefCat;
    public ListPreference listPref;
    public ListPreference min_frequency;
    public ListPreference max_frequency;
    public boolean mVisible = true;
    private SharedPreferences prefs;
    public ShowcaseView.ConfigOptions mConfigOptions;
    public ShowcaseView mShowCase;

    public static final int mNumCpus = Runtime.getRuntime().availableProcessors();

    @Override
    final public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.cpu_fragment);

        root = this.getPreferenceScreen();
        // Remove until back in Kernel;
        PreferenceCategory cpuCategory = (PreferenceCategory) findPreference("cpu_settings");

        // I don't like the following, can we simplify it?

        // Find our ListPreference (max_frequency);
        max_frequency = (ListPreference) root.findPreference("max_frequency");
        updateMaxFreq();
        max_frequency.setDialogIcon(R.drawable.lightning_dark);

        // Find our ListPreference (min_frequency);
        min_frequency = (ListPreference) root.findPreference("min_frequency");
        updateMinFreq();
        min_frequency.setDialogIcon(R.drawable.lightning_dark);

        final Preference cpu_hotplug = root.findPreference("hotplug_control");
        if (new File("/sys/kernel/hotplug_control").exists()) {
            cpu_hotplug.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager()
                        .beginTransaction()
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .replace(R.id.content_frame, new CPUHotplugFragment())
                            .addToBackStack("Hotplug")
                            .commit();

                    return true;
                }
            });
        } else {
            cpuCategory.removePreference(cpu_hotplug);
        }

        final Preference cpu_oc_uc = (Preference) root.findPreference("cpu_oc_uc");

        if (AeroActivity.shell.getInfo(CPU_VSEL).equals("Unavailable")) {
            cpu_oc_uc.setEnabled(false);

            cpuCategory.removePreference(cpu_oc_uc);
        }

        cpu_oc_uc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Set up Alert Dialog and view;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.cpu_oc_uc, null);
                builder.setIcon(R.drawable.lightbulb_dark);
                String[] cpufreq = AeroActivity.shell.getInfoArray(CPU_AVAILABLE_FREQ, 0, 0);


                // Our cpu values list;
                final ArrayList<String> cpu_list = new ArrayList<String>();
                final String overclockOutput = AeroActivity.shell.getRootInfo("cat", CPU_VSEL);

                // Set up our EditText fields;
                final EditText value1 = (EditText) layout.findViewById(R.id.value1);
                final EditText value2 = (EditText) layout.findViewById(R.id.value2);
                final EditText value3 = (EditText) layout.findViewById(R.id.value3);
                final EditText value4 = (EditText) layout.findViewById(R.id.value4);
                final EditText value5 = (EditText) layout.findViewById(R.id.value5);
                final EditText value6 = (EditText) layout.findViewById(R.id.value6);
                final EditText value7 = (EditText) layout.findViewById(R.id.value7);
                final EditText value8 = (EditText) layout.findViewById(R.id.value8);

                // Left side (cpu frequencies);
                value1.setText(cpufreq[0]);
                value3.setText(cpufreq[1]);
                value5.setText(cpufreq[2]);
                value7.setText(cpufreq[3]);

                // Substring is not ideal, but it gets the job done;
                value2.setText(overclockOutput.substring(42, 44));

                // In case somebody uses very high frequencies;
                if (Integer.parseInt(cpufreq[1]) >= 1000000) {
                        value4.setText(overclockOutput.substring(105, 107));
                        value6.setText(overclockOutput.substring(167, 169));
                        value8.setText(overclockOutput.substring(229, 231));
                } else {
                        value4.setText(overclockOutput.substring(104, 106));
                        value6.setText(overclockOutput.substring(166, 168));
                        value8.setText(overclockOutput.substring(228, 230));
                }

                    // Inflate and set the layout for the dialog
                    // Pass null as the parent view because its going in the dialog layout
                builder.setView(layout)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            AeroActivity.shell.setOverclockAddress();
                            // Objects;
                            final Object f = (value1.getText());
                            final Object g = (value3.getText());
                            final Object h = (value5.getText());
                            final Object i = (value7.getText());

                            Runnable runnable = new Runnable() {
                                int a, b, c, d;
                                int vsel1, vsel2, vsel3, vsel4;

                                @Override
                                public void run() {
                                    // Cast objects to integer (frequencies;
                                    a = Integer.parseInt(f.toString());
                                    b = Integer.parseInt(g.toString());
                                    c = Integer.parseInt(h.toString());
                                    d = Integer.parseInt(i.toString());

                                    try {
                                        // Cast voltages;
                                        vsel1 = Integer.parseInt(value2.getText().toString());
                                        vsel2 = Integer.parseInt(value4.getText().toString());
                                        vsel3 = Integer.parseInt(value6.getText().toString());
                                        vsel4 = Integer.parseInt(value8.getText().toString());
                                    } catch (NumberFormatException e) {

                                        // Go back to default, if wrong values;
                                        vsel1 = 62;
                                        vsel2 = 58;
                                        vsel3 = 48;
                                        vsel4 = 33;
                                        Log.e("Aero", "These are wrong values, back to default.", e);
                                    }

                                    // Check if there are valid values;
                                    if (a <= 1500000 && a > b && b > c && c > d
                                        && vsel1 < 80 && vsel1 > vsel2 && vsel2 > vsel3 && vsel3 > vsel4 && vsel4 >= 15) {

                                        if (a > 300000 && b > 300000 && c > 300000 && d >= 300000) {
                                            try {
                                                // Set our max vsel after we changed the max freq
                                                cpu_list.add("echo " + vsel1 + " > " + CPU_VSEL_MAX);
                                                cpu_list.add("echo " + "4" + " " + a + "000" + " " + vsel1 + " > " + CPU_VSEL);
                                                cpu_list.add("echo " + "3" + " " + b + "000" + " " + vsel2 + " > " + CPU_VSEL);
                                                cpu_list.add("echo " + "2" + " " + c + "000" + " " + vsel3 + " > " + CPU_VSEL);
                                                cpu_list.add("echo " + "1" + " " + d + "000" + " " + vsel4 + " > " + CPU_VSEL);
                                                cpu_list.add("echo " + "0" + " " + a + " > " + CPU_FREQ_TABLE);
                                                cpu_list.add("echo " + "1" + " " + b + " > " + CPU_FREQ_TABLE);
                                                cpu_list.add("echo " + "2" + " " + c + " > " + CPU_FREQ_TABLE);
                                                cpu_list.add("echo " + "3" + " " + d + " > " + CPU_FREQ_TABLE);
                                                cpu_list.add("echo " + f + " > " + CPU_MAX_RATE);
                                                cpu_list.add("echo " + i + " > " + CPU_BASE_PATH + 0 + CPU_MIN_FREQ);

                                                String[] commands = cpu_list.toArray(new String[0]);

                                                // Throw them all in!
                                                AeroActivity.shell.setRootInfo(commands);

                                                //** store preferences
                                                //** note that this time we put to preferences commands instead of single values,
                                                //** rebuild the commands in the bootService would have been a little expensive
                                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                                                preference.edit().putStringSet("cpu_commands", new HashSet<String>(Arrays.asList(commands))).commit();

                                            } catch (Exception e) {
                                                Log.e("Aero", "An Error occurred while setting values", e);
                                                Toast.makeText(getActivity(), "An Error occurred, check logcat!", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.e("Aero", "The frequencies you have set are to low!");
                                        }
                                    } else {
                                        Log.e("Aero", "Cannot apply values, they are not in range.");

                                    }
                                }
                            };
                            try {
                                Thread cpuUpdateThread = new Thread(runnable);
                                cpuUpdateThread.start();
                            } catch (Exception e) {
                                Log.e("Aero", "Something went completely wrong.", e);
                            }

                            // Start our background refresher Task;
                            try {
                                // Only start if not already alive
                                if (!mRefreshThread.isAlive()) {
                                    mRefreshThread.start();
                                    mRefreshThread.setPriority(Thread.MIN_PRIORITY);
                                }
                            } catch (NullPointerException e) {
                                Log.e("Aero", "Couldn't start Refresher Thread.", e);
                            }
                        }
                    })

                    .setNeutralButton(R.string.default_values, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {

                                AeroActivity.shell.setOverclockAddress();

                                // Set our max vsel after we changed the max freq
                                cpu_list.add("echo " + "62" + " > " + CPU_VSEL_MAX);
                                cpu_list.add("echo " + "4" + " 1000000000" + " 62" + " > " + CPU_VSEL);
                                cpu_list.add("echo " + "3" + " 800000000" + " 58" + " > " + CPU_VSEL);
                                cpu_list.add("echo " + "2" + " 600000000" + " 48" + " > " + CPU_VSEL);
                                cpu_list.add("echo " + "1" + " 300000000" + " 33" + " > " + CPU_VSEL);
                                cpu_list.add("echo " + "0" + " 1000000" + " > " + CPU_FREQ_TABLE);
                                cpu_list.add("echo " + "1" + " 800000" + " > " + CPU_FREQ_TABLE);
                                cpu_list.add("echo " + "2" + " 600000" + " > " + CPU_FREQ_TABLE);
                                cpu_list.add("echo " + "3" + " 300000" + " > " + CPU_FREQ_TABLE);
                                cpu_list.add("echo " + "1000000" + " > " + CPU_MAX_RATE);
                                cpu_list.add("echo " + "300000" + " > " + CPU_MIN_FREQ);

                                String[] commands = cpu_list.toArray(new String[0]);
                                AeroActivity.shell.setRootInfo(commands);

                                try {
                                    Thread.sleep(350);
                                } catch (InterruptedException e) {
                                    Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                                }

                                //** store preferences
                                //** note that this time we put to preferences commands instead of single values,
                                //** rebuild the commands in the bootService would have been a little expensive
                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                                preference.edit().putStringSet("cpu_commands", new HashSet<String>(Arrays.asList(commands))).commit();

                            } catch (Exception e) {
                                Log.e("Aero", "An Error occurred while setting values", e);
                                Toast.makeText(getActivity(), "An Error occurred, check logcat!", Toast.LENGTH_SHORT).show();
                            }

                            // Start our background refresher Task;
                            try {
                                // Only start if not already alive
                                if (!mRefreshThread.isAlive()) {
                                    mRefreshThread.start();
                                    mRefreshThread.setPriority(Thread.MIN_PRIORITY);
                                }
                            } catch (NullPointerException e) {
                                Log.e("Aero", "Couldn't start Refresher Thread.", e);
                            }

                        }
                    })

                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                builder.setTitle(R.string.perf_live_oc_uc).show();



                return false;
            }
        });

        // Find our ListPreference (governor_settings);
        listPref = (ListPreference) root.findPreference("set_governor");
        // Just throw in our frequencies;
        listPref.setEntries(AeroActivity.shell.getInfoArray(ALL_GOV_AVAILABLE, 0, 0));
        listPref.setEntryValues(AeroActivity.shell.getInfoArray(ALL_GOV_AVAILABLE, 0, 0));
        listPref.setValue(AeroActivity.shell.getInfo(CPU_BASE_PATH + 0 + CURRENT_GOV_AVAILABLE));
        listPref.setSummary(AeroActivity.shell.getInfo(CPU_BASE_PATH + 0 + CURRENT_GOV_AVAILABLE));
        listPref.setDialogIcon(R.drawable.cpu_dark);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);


        //different listener for each element
        listPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * I need to cast the object to a string first, but setRootInfo
                 * will take the object instead (its casted again there).
                 * The intention behind this is, is to solve the slow UI reaction
                 * and the slow file write process. Otherwise the UI would show the
                 * value _before_ the value actually was changed.
                 */
                String a = (String) o;

                // If there are already some entries, kill them all (with fire)
                if (PrefCat != null)
                    root.removePreference(PrefCat);

                // Change governor for each available CPU;
                setGovernor(a);

                        /*
                         * Probably the kernel takes a while to update the dictionaries
                         * and therefore we sleep for a short interval;
                         */
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                }
                listPref.setSummary(AeroActivity.shell.getInfo(CPU_BASE_PATH + 0 + CURRENT_GOV_AVAILABLE));

                // store preferences
                preference.getEditor().commit();

                return true;
            }

            ;
        });

        max_frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * Its pretty much the same like on the governor, except we only deal with numbers
                 * Also this should make no problems when the user is using different
                 * Clocks than default...
                 */
                String a = (String) o;
                ArrayList<String> array = new ArrayList<String>();

                for (int k = 0; k < mNumCpus; k++) {

                    array.add("echo " + a + " > " + CPU_BASE_PATH + k + CPU_MAX_FREQ);

                    //** store preferences
                    preference.getEditor().commit();
                }
                max_frequency.setSummary(AeroActivity.shell.toMHz(a));
                String[] commands = array.toArray(new String[0]);

                AeroActivity.shell.setRootInfo(commands);
                return true;
            };
        });

        min_frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;
                ArrayList<String> array = new ArrayList<String>();

                for (int k = 0; k < mNumCpus; k++) {

                    array.add("echo " + a + " > " + CPU_BASE_PATH + k + CPU_MIN_FREQ);

                    //** store preferences
                    preference.getEditor().commit();
                }
                min_frequency.setSummary(AeroActivity.shell.toMHz(a));
                String[] commands = array.toArray(new String[0]);

                AeroActivity.shell.setRootInfo(commands);
                return true;
            };
        });


    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String a = prefs.getString("app_theme", null);

        if (a == null)
            a = "";

        if (a.equals("red"))
            inflater.inflate(R.menu.cpu_menu, menu);
        else if (a.equals("light"))
            inflater.inflate(R.menu.cpu_menu, menu);
        else if (a.equals("dark"))
            inflater.inflate(R.menu.cpu_menu_light, menu);
        else
            inflater.inflate(R.menu.cpu_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_governor_settings:

                final String complete_path = CPU_GOV_SET_BASE + listPref.getValue();
                ArrayList<String> al = new ArrayList<String>();

                /*
                 * Before we can get governor specific parameters,
                 * we need permissions first. Since we don't want to use "su"
                 * here, we change the current governor shortly to performance.
                 */

                try {

                    String completeParamterList[] = AeroActivity.shell.getDirInfo(complete_path, true);

                    // If there are already some entries, kill them all (with fire)
                    if (PrefCat != null)
                        root.removePreference(PrefCat);

                    PrefCat = new PreferenceCategory(getActivity());
                    PrefCat.setTitle(R.string.pref_gov_set);
                    root.addPreference(PrefCat);

                    /*
                     * Sometimes its just all about permissions;
                     */

                    for (String b : completeParamterList) {
                        al.add("chmod 644 " + complete_path + "/" + b);
                        al.add("chown system:root " + complete_path + "/" + b);
                    }
                    String[] commands = al.toArray(new String[0]);
                    AeroActivity.shell.setRootInfo(commands);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                    }

                    final PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

                    for (String b : completeParamterList)
                        h.generateSettings(b, complete_path);

                    // Probably the wrong place, should be in getDirInfo ?
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), "Looks like there are no parameter for this governor?", Toast.LENGTH_LONG).show();
                    Log.e("Aero", "There isn't any folder i can check. Does this governor has parameters?", e);

                    return true;
                }


                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        mVisible = false;
        // To clean up the UI;
        if (PrefCat != null)
            root.removePreference(PrefCat);
    }

    @Override
    public void onResume() {
        super.onResume();

        mVisible = true;
    }

    public void setGovernor(String s) {

        ArrayList<String> array = new ArrayList<String>();

        // Change governor for each available CPU;
        for (int k = 0; k < mNumCpus; k++) {
            // To ensure we get proper permissions, change the governor to performance first;
            //array.add("echo " + "performance" + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);
            array.add("echo " + s + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);
        }
        String[] commands = array.toArray(new String[0]);

        AeroActivity.shell.setRootInfo(commands);

    }

    public void updateMinFreq() {
        // Just throw in our frequencies;
        min_frequency.setEntries(AeroActivity.shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        min_frequency.setEntryValues(AeroActivity.shell.getInfoArray(CPU_AVAILABLE_FREQ, 0, 0));
        try {
            min_frequency.setValue(AeroActivity.shell.getInfoArray(CPU_BASE_PATH + 0 + CPU_MIN_FREQ, 0, 0)[0]);
            min_frequency.setSummary(AeroActivity.shell.getInfoArray(CPU_BASE_PATH + 0 + CPU_MIN_FREQ, 1, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            min_frequency.setValue("Unavailable");
            min_frequency.setSummary("Unavailable");
        }

        //** store preferences
        min_frequency.getEditor().commit();

    }

    public void updateMaxFreq() {
        // Just throw in our frequencies;
        max_frequency.setEntries(AeroActivity.shell.getInfoArray(CPU_AVAILABLE_FREQ, 1, 0));
        max_frequency.setEntryValues(AeroActivity.shell.getInfoArray(CPU_AVAILABLE_FREQ, 0, 0));
        try {
            max_frequency.setValue(AeroActivity.shell.getInfoArray(CPU_BASE_PATH + 0 + CPU_MAX_FREQ, 0, 0)[0]);
            max_frequency.setSummary(AeroActivity.shell.getInfoArray(CPU_BASE_PATH + 0 + CPU_MAX_FREQ, 1, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            max_frequency.setValue("Unavailable");
            max_frequency.setSummary("Unavailable");
        }

        //** store preferences
        max_frequency.getEditor().commit();

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
            final FileInputStream fis = getActivity().openFileInput(FILENAME);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        // Only show showcase once;
        if (output == 0)
            DrawFirstStart(R.string.showcase_cpu_fragment_governor, R.string.showcase_cpu_fragment_governor_sum);

    }

    public void DrawFirstStart(int header, int content) {

        try {
            final FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

        mShowCase = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM , R.id.action_governor_settings, getActivity(), header, content, mConfigOptions);
    }

    private class RefreshThread extends Thread {

        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }
        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(1000);
                    mRefreshHandler.sendEmptyMessage(1);
                }
            } catch (InterruptedException e) {

            }
        }
    };

    private RefreshThread mRefreshThread = new RefreshThread();

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what >= 1) {
                if (isVisible() && mVisible) {
                    updateMaxFreq();
                    updateMinFreq();
                    mVisible = true;
                } else {
                    // Do nothing
                }

            }
        }
    };
}
