package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.helpers.Android.CustomEditText;
import com.aero.control.helpers.Android.CustomListPreference;
import com.aero.control.helpers.Android.CustomPreference;
import com.aero.control.helpers.PreferenceHandler;
import com.espian.showcaseview.ShowcaseView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Alexander Christ on 03.10.13.
 *
 */
public class CPUFragment extends PreferenceFragment {

    private static final String FILENAME = "firstrun_cpu";

    private PreferenceScreen root;
    private PreferenceCategory PrefCat;
    private CustomListPreference mCPUGovernor;
    private CustomListPreference mMinFrequency;
    private CustomListPreference mMaxFrequency;
    private boolean mVisible = true;
    private ShowcaseView.ConfigOptions mConfigOptions;
    private ShowcaseView mShowCase;

    private CPUHotplugFragment mHotplugFragment;
    private VoltageFragment mVoltageFragment;

    private static final ArrayList<String> mVselList = new ArrayList<String>();

    private static final int mNumCpus = Runtime.getRuntime().availableProcessors();

    @Override
    final public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // We have to load the xml layout first;
        addPreferencesFromResource(R.layout.cpu_fragment);

        root = this.getPreferenceScreen();
        // Remove until back in Kernel;
        final PreferenceCategory cpuCategory = (PreferenceCategory) findPreference("cpu_settings");
        final PreferenceCategory cpuGovernor = (PreferenceCategory) findPreference("cpu_governor");

        // I don't like the following, can we simplify it?

        // Create our custom list preference (mMaxFrequency);
        mMaxFrequency = new CustomListPreference(getActivity());
        mMaxFrequency.setName("max_frequency");
        mMaxFrequency.setTitle(R.string.pref_cpu_freqmax);
        mMaxFrequency.setDialogTitle(R.string.pref_cpu_freqmax);
        mMaxFrequency.setSummary(R.string.pref_cpu_freqmax);
        updateMaxFreq();
        mMaxFrequency.setDialogIcon(R.drawable.lightning);
        mMaxFrequency.setOrder(0);
        cpuCategory.addPreference(mMaxFrequency);

        // Create our custom list preference (mMinFrequency);
        mMinFrequency = new CustomListPreference(getActivity());
        mMinFrequency.setName("min_frequency");
        mMinFrequency.setTitle(R.string.pref_cpu_freqmin);
        mMinFrequency.setDialogTitle(R.string.pref_cpu_freqmin);
        mMinFrequency.setSummary(R.string.pref_cpu_freqmin);
        updateMinFreq();
        mMinFrequency.setDialogIcon(R.drawable.lightning);
        mMaxFrequency.setOrder(1);
        cpuCategory.addPreference(mMinFrequency);

        final Preference cpu_hotplug = root.findPreference("hotplug_control");
        if (AeroActivity.genHelper.doesExist("/sys/kernel/hotplug_control")) {
            cpu_hotplug.setOrder(10);
            cpu_hotplug.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (mHotplugFragment == null)
                            mHotplugFragment = new CPUHotplugFragment();

                    AeroActivity.mHandler.postDelayed(new Runnable()  {
                        @Override
                        public void run() {
                            getFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                    .replace(R.id.content_frame, mHotplugFragment)
                                    .addToBackStack("Hotplug")
                                    .commit();
                            mHotplugFragment.setTitle();
                        }
                    },AeroActivity.genHelper.getDefaultDelay());
                    return true;
                }
            });
        } else {
            cpuCategory.removePreference(cpu_hotplug);
        }

        final CustomPreference voltage_control = (CustomPreference)root.findPreference("voltage_values");
        if (AeroActivity.genHelper.doesExist("/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table")) {
            voltage_control.setOrder(15);
            voltage_control.setLookUpDefault(AeroActivity.files.VOLTAGE_PATH);
            voltage_control.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (mVoltageFragment == null)
                        mVoltageFragment = new VoltageFragment();

                    AeroActivity.mHandler.postDelayed(new Runnable()  {
                        @Override
                        public void run() {
                            getFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                    .replace(R.id.content_frame, mVoltageFragment)
                                    .addToBackStack("Voltage")
                                    .commit();
                            mVoltageFragment.setTitle();
                        }
                    },AeroActivity.genHelper.getDefaultDelay());
                    return true;
                }
            });
        } else {
            cpuCategory.removePreference(voltage_control);
        }


        final Preference cpu_oc_uc = (Preference) root.findPreference("cpu_commands");

        if (AeroActivity.shell.getInfo(AeroActivity.files.CPU_VSEL).equals("Unavailable"))
            cpuCategory.removePreference(cpu_oc_uc);
        else
            cpu_oc_uc.setOrder(20);

        cpu_oc_uc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final String overclockOutput = AeroActivity.shell.getRootInfo("cat", AeroActivity.files.CPU_VSEL);
                final String[] cpufreq = AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_AVAILABLE_FREQ, 0, 0);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final LayoutInflater inflater = getActivity().getLayoutInflater();
                final View layout = inflater.inflate(R.layout.cpu_oc_uc, null);
                final ViewGroup cpuOCGroup = (ViewGroup)layout.findViewById(R.id.cpu_container);
                int i = 0;

                // Maybe it was filled before, better save than sorry;
                mVselList.clear();

                CustomEditText cpuValues;
                ViewGroup.MarginLayoutParams cpuMargins;
                RelativeLayout.LayoutParams cpuLayout;

                // Get position and content of vsel-values;
                for (int k = -1; (k = overclockOutput.indexOf(" vsel=", k + 1)) != -1;) {
                    mVselList.add(overclockOutput.substring(k + 6, k + 8));
                }

                for (String a : cpufreq) {
                    for (int j = 0; j < 2; j++) {
                        cpuValues = new CustomEditText(getActivity());

                        if (j == 0)
                            cpuValues.setText(a);
                        else
                            cpuValues.setText(mVselList.toArray(new String[0])[i]);

                        // Add the view, we can change its layout afterwards;
                        cpuOCGroup.addView(cpuValues);

                        cpuMargins = new ViewGroup.MarginLayoutParams(cpuValues.getLayoutParams());
                        // Ensure first row is bound to left, second is bound to right;
                        cpuMargins.setMargins(0, (i * 75), (j * 30), 0);
                        cpuLayout = new RelativeLayout.LayoutParams(cpuMargins);

                        if (j > 0) {
                            cpuLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            cpuLayout.width = 100;
                        } else
                            cpuLayout.width = 200;

                        cpuValues.setLayoutParams(cpuLayout);
                    }
                    i++;
                }
                builder.setIcon(R.drawable.calculator);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ArrayList<Integer> cpuFreqs = new ArrayList<Integer>();
                        ArrayList<Integer> vselValues = new ArrayList<Integer>();
                        int t = cpuOCGroup.getChildCount();

                        // Get Frequencies- and Vsel-Values back;
                        for (int l = 0; l < t; l++) {
                            CustomEditText editText = (CustomEditText)cpuOCGroup.getChildAt(l);
                            int tmp;

                            // Check if its a valid number, if not bail out;
                            try {
                                tmp = Integer.parseInt(editText.getText().toString());
                            } catch (NumberFormatException e) {
                                Log.e("Aero", "An Error occured! ", e);
                                return;
                            }

                            // Check if vsel- or frequency value;
                            if (l % 2 > 0) {
                                if (l > 1) {
                                    // Run the sanity checks;
                                    if (vselValues.get(vselValues.size() - 1) < tmp
                                            && tmp > 15 && tmp < 80) {
                                        // If a wrong vsel input is detected, bail out;
                                        Log.e("Aero", "Invalid input: " + tmp + " Last input: " +
                                                vselValues.get(vselValues.size() - 1));
                                            return;
                                    }
                                }
                                vselValues.add(tmp);

                            } else {
                                if (l > 1) {
                                    // Run the sanity checks;
                                    if (cpuFreqs.get(cpuFreqs.size() - 1) < tmp
                                            && tmp > 1500000 && tmp > 300000) {
                                        // If a wrong vsel input is detected, bail out;
                                        Log.e("Aero", "Invalid input: " + tmp + " Last input: "
                                                + cpuFreqs.get(cpuFreqs.size() - 1));
                                        return;
                                    }
                                }
                                cpuFreqs.add(tmp);
                            }
                        }

                        Integer[] newFrequencies = cpuFreqs.toArray(new Integer[0]);
                        // Previous array is cloned, we reuse it;
                        mVselList.clear();
                        int listLength = newFrequencies.length;
                        i = 0;

                        // Puzzle the values together;
                        mVselList.add("echo " + vselValues.get(0) + " > " + AeroActivity.files.CPU_VSEL_MAX);
                        for (Integer freq : newFrequencies) {
                            mVselList.add("echo " + listLength + " " + freq + "000" + " " + vselValues.get(i) + " > " + AeroActivity.files.CPU_VSEL);
                            mVselList.add("echo " + i + " " + freq + " > " + AeroActivity.files.CPU_FREQ_TABLE);

                            Log.e("Aero", "echo " + listLength + " " + freq + "000" + " " + vselValues.get(i) + " > " + AeroActivity.files.CPU_VSEL);
                            listLength--;
                            i++;
                        }
                        mVselList.add("echo " + newFrequencies[0] + " > " + AeroActivity.files.CPU_MAX_RATE);
                        mVselList.add("echo " + newFrequencies[newFrequencies.length - 1] + " > "
                                + AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CPU_MIN_FREQ);

                        String[] commands = mVselList.toArray(new String[0]);
                        // Throw them all in!
                        AeroActivity.shell.setRootInfo(commands);

                        /*
                         * store preferences
                         * note that this time we put to preferences commands instead of single values,
                         * rebuild the commands in the bootService would have been a little expensive
                         */
                        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                        // We still want to write it here, since we can disable it later;
                        preference.edit().putStringSet("cpu_commands", new HashSet<String>(Arrays.asList(commands))).commit();


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
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing
                    }
                });
                builder.setView(layout).setTitle(R.string.perf_live_oc_uc).show();

                return false;
            }
        });

        // Find ourmCPUGovernoerence (governor_settings);
        mCPUGovernor = new CustomListPreference(getActivity());
        mCPUGovernor.setName("set_governor");
        mCPUGovernor.setTitle(R.string.pref_cpu_governor);
        mCPUGovernor.setDialogTitle(R.string.pref_cpu_governor);
        // Just throw in our frequencies;
        mCPUGovernor.setEntries(AeroActivity.shell.getInfoArray(AeroActivity.files.ALL_GOV_AVAILABLE, 0, 0));
        mCPUGovernor.setEntryValues(AeroActivity.shell.getInfoArray(AeroActivity.files.ALL_GOV_AVAILABLE, 0, 0));
        mCPUGovernor.setValue(AeroActivity.shell.getInfo(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CURRENT_GOV_AVAILABLE));
        mCPUGovernor.setSummary(AeroActivity.shell.getInfo(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CURRENT_GOV_AVAILABLE));
        mCPUGovernor.setDialogIcon(R.drawable.cpu);

        cpuGovernor.addPreference(mCPUGovernor);

        // If there are already some entries, kill them all (with fire)
        if (PrefCat != null)
            root.removePreference(PrefCat);


        //different listener for each element
        mCPUGovernor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
                    Thread.sleep(450);
                } catch (InterruptedException e) {
                    Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                }
                mCPUGovernor.setSummary(AeroActivity.shell.getInfo(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CURRENT_GOV_AVAILABLE));

                return true;
            }

            ;
        });

        mMaxFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                /*
                 * Its pretty much the same like on the governor, except we only deal with numbers
                 * Also this should make no problems when the user is using different
                 * Clocks than default...
                 */
                String a = (String) o;
                ArrayList<String> array = new ArrayList<String>();

                try {
                    if (Integer.parseInt(a) < Integer.parseInt(mMinFrequency.getValue()))
                        return false;
                } catch (NumberFormatException e) {
                    return false;
                }

                for (int k = 0; k < mNumCpus; k++) {
                    array.add("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
                    array.add("echo " + a + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MAX_FREQ);
                }
                mMaxFrequency.setSummary(AeroActivity.shell.toMHz(a));
                String[] commands = array.toArray(new String[0]);

                AeroActivity.shell.setRootInfo(commands);
                return true;
            };
        });

        mMinFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String a = (String) o;
                ArrayList<String> array = new ArrayList<String>();

                try {
                    if (Integer.parseInt(a) > Integer.parseInt(mMaxFrequency.getValue()))
                        return false;
                } catch (NumberFormatException e) {
                    return false;
                }

                for (int k = 0; k < mNumCpus; k++) {
                    array.add("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
                    array.add("echo " + a + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MIN_FREQ);
                }
                mMinFrequency.setSummary(AeroActivity.shell.toMHz(a));
                String[] commands = array.toArray(new String[0]);

                AeroActivity.shell.setRootInfo(commands);
                return true;
            };
        });


    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.cpu_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_governor_settings:

                final String complete_path = AeroActivity.files.CPU_GOV_BASE + mCPUGovernor.getValue();
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
                        al.add("chmod 0666 " + complete_path + "/" + b);
                        al.add("chown system:root " + complete_path + "/" + b);
                    }
                    String[] commands = al.toArray(new String[0]);
                    AeroActivity.shell.setRootInfo(commands);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                    }

                    final PreferenceHandler h = new PreferenceHandler(getActivity(), PrefCat, getPreferenceManager());

                    h.genPrefFromDictionary(completeParamterList, complete_path);

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
            array.add("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
            array.add("echo " + s + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CURRENT_GOV_AVAILABLE);
        }
        String[] commands = array.toArray(new String[0]);

        AeroActivity.shell.setRootInfo(commands);

    }

    public void updateMinFreq() {
        // Just throw in our frequencies;
        mMinFrequency.setEntries(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_AVAILABLE_FREQ, 1, 0));
        mMinFrequency.setEntryValues(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_AVAILABLE_FREQ, 0, 0));
        try {
            mMinFrequency.setValue(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CPU_MIN_FREQ, 0, 0)[0]);
            mMinFrequency.setSummary(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CPU_MIN_FREQ, 1, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            mMinFrequency.setValue("Unavailable");
            mMinFrequency.setSummary("Unavailable");
        }
    }

    public void updateMaxFreq() {
        // Just throw in our frequencies;
        mMaxFrequency.setEntries(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_AVAILABLE_FREQ, 1, 0));
        mMaxFrequency.setEntryValues(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_AVAILABLE_FREQ, 0, 0));
        try {
            mMaxFrequency.setValue(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CPU_MAX_FREQ, 0, 0)[0]);
            mMaxFrequency.setSummary(AeroActivity.shell.getInfoArray(AeroActivity.files.CPU_BASE_PATH + 0 + AeroActivity.files.CPU_MAX_FREQ, 1, 0)[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            mMaxFrequency.setValue("Unavailable");
            mMaxFrequency.setSummary("Unavailable");
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

        boolean tableUpdate = false;

        @Override
        public void handleMessage(Message msg) {

            if (msg.what >= 1) {
                if (isVisible() && mVisible) {
                    updateMaxFreq();
                    updateMinFreq();
                    if (!tableUpdate)
                        tableUpdate = AeroActivity.shell.setOverclockAddress();
                    mVisible = true;
                } else {
                    // Do nothing
                }

            }
        }
    };
}
