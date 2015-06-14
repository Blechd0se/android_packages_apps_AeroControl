package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import com.aero.control.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Alexander Christ on 05.01.14.
 */
public class settingsHelper {
    private static final String PREF_CURRENT_GOV_AVAILABLE = "set_governor";
    private static final String PREF_CPU_MAX_FREQ = "max_frequency";
    private static final String PREF_CPU_MIN_FREQ = "min_frequency";
    private static final String PREF_CPU_COMMANDS = "cpu_commands";

    private static final String PREF_CURRENT_GPU_GOV_AVAILABLE = "set_gpu_governor";
    private static final String PREF_GPU_FREQ_MAX = "gpu_max_freq";
    private static final String PREF_GPU_CONTROL_ACTIVE = "gpu_control_enable";
    private static final String PREF_DISPLAY_COLOR = "display_control";
    private static final String PREF_SWEEP2WAKE = "sweeptowake";
    private static final String PREF_DOUBLETAP2WAKE = "doubletaptowake";

    private static final String PREF_GOV_IO_FILE = "io_scheduler_list";
    private static final String PREF_DYANMIC_FSYNC = "dynFsync";
    private static final String PREF_FSYNC = "fsync";
    private static final String PREF_KSM = "ksm";
    private static final String PREF_READAHEAD = "read_ahead";
    private static final String PREF_ENTROPY_SETTINGS = "entropy_settings";
    private static final String PREF_WRITEBACK = "writeback";
    private static final String PREF_TCP_CONGESTION = "tcp_congestion";
    private static final String PREF_TIMER_DELAY = "boot_delay";
    private static final String MISC_SETTINGS_STORAGE = "miscSettingsStorage";

    private SharedPreferences prefs;
    private SharedPreferences mMiscSettings;
    private String gpu_file;
    private String mHotplugPath;
    private String mGPUGov;
    private static final int mNumCpus = Runtime.getRuntime().availableProcessors();

    private static final shellHelper shell = new shellHelper();
    private static final shellHelper shellPara = new shellHelper();
    private static final ArrayList<String> defaultProfile = new ArrayList<String>();
    private static final GenericHelper genHelper = new GenericHelper();

    public void setSettings(final Context context, final String Profile, final boolean onboot) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // We need to sleep here for a short while for the kernel
                if (shell.setOverclockAddress()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e("Aero", "Something went really wrong...", e);
                    }
                }
                // Apply all our saved values;
                doBackground(context, Profile, onboot);

                if (onboot) {
                    Handler h = new Handler(context.getMainLooper());

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, context.getText(R.string.app_name) + ": " +
                                    context.getText(R.string.finishing_settings), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        }).start();

    }

    private void doBackground(final Context context, final String Profile, final boolean onboot) {


        if (Profile == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        else
            prefs = context.getSharedPreferences(Profile, Context.MODE_PRIVATE);

        mMiscSettings = context.getSharedPreferences(MISC_SETTINGS_STORAGE, Context.MODE_PRIVATE);

        if (onboot) {
            String timer_delay = prefs.getString(PREF_TIMER_DELAY, null);
            if (timer_delay != null) {
                int i = Integer.parseInt(timer_delay);
                try {
                    Thread.sleep((i * 1000 * 60));
                } catch (InterruptedException e) {}
            }

            Handler h = new Handler(context.getMainLooper());

            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getText(R.string.app_name) + ": " +
                            context.getText(R.string.setonboot_settings), Toast.LENGTH_LONG).show();
                }
            });

        }

        // GET CPU VALUES AND COMMANDS FROM PREFERENCES
        String cpu_max = prefs.getString(PREF_CPU_MAX_FREQ, null);
        String cpu_min = prefs.getString(PREF_CPU_MIN_FREQ, null);
        String cpu_gov = prefs.getString(PREF_CURRENT_GOV_AVAILABLE, null);


        // Overclocking....
        try {
            HashSet<String> hashcpu_cmd = (HashSet<String>) prefs.getStringSet(PREF_CPU_COMMANDS, null);
            if (hashcpu_cmd != null) {
                for (String cmd : hashcpu_cmd) {
                    shell.queueWork(cmd);
                }
            }
        } catch (ClassCastException e) {
            // HashSet didn't work, so we make a fallback;
            String cpu_cmd = prefs.getString(PREF_CPU_COMMANDS, null);

            if (cpu_cmd != null) {
                // Since we can't cast to hashmap, little workaround;
                String[] array = cpu_cmd.substring(1, cpu_cmd.length() - 1).split(",");
                for (String cmd : array) {
                    shell.queueWork(cmd);
                }
            }
        }

        // Entropy settings...
        try {
            HashSet<String> hashent_cmd = (HashSet<String>) prefs.getStringSet(PREF_ENTROPY_SETTINGS, null);
            if (hashent_cmd != null) {
                for (String cmd : hashent_cmd) {
                    shell.queueWork(cmd);
                }
            }
        } catch (ClassCastException e) {
            // HashSet didn't work, so we make a fallback;
            String ent_cmd = prefs.getString(PREF_ENTROPY_SETTINGS, null);

            if (ent_cmd != null) {
                // Since we can't cast to hashmap, little workaround;
                String[] array = ent_cmd.substring(1, ent_cmd.length() - 1).split(",");
                for (String cmd : array) {
                    shell.queueWork(cmd);
                }
            }
        }

        String voltage = prefs.getString("voltage_values", null);

        if (voltage != null)
            shell.queueWork("echo " + voltage + " > " + FilePath.VOLTAGE_PATH);

        // GET GPU VALUES FROM PREFERENCES
        String gpu_gov = prefs.getString(PREF_CURRENT_GPU_GOV_AVAILABLE, null);
        String gpu_max = prefs.getString(PREF_GPU_FREQ_MAX, null);
        String display_color = prefs.getString(PREF_DISPLAY_COLOR, null);
        Boolean gpu_enb = getSaveBoolean(PREF_GPU_CONTROL_ACTIVE);
        Boolean sweep = getSaveBoolean(PREF_SWEEP2WAKE);
        Boolean doubletap = getSaveBoolean(PREF_DOUBLETAP2WAKE);
        String rgbValues = prefs.getString("rgbValues", null);
        // GET MEM VALUES FROM PREFERENCES
        String mem_ios = prefs.getString(PREF_GOV_IO_FILE, null);
        String mem_rah = prefs.getString(PREF_READAHEAD, null);
        Boolean mem_dfs = getSaveBoolean(PREF_DYANMIC_FSYNC);
        Boolean mem_wrb = getSaveBoolean(PREF_WRITEBACK);
        Boolean mem_fsy = getSaveBoolean(PREF_FSYNC) ;
        Boolean mem_ksm = getSaveBoolean(PREF_KSM);
        // Get Misc Settings from preferences
        String misc_vib = prefs.getString(FilePath.MISC_VIBRATOR_CONTROL_FILE, null);
        String misc_amp = prefs.getString(FilePath.MISC_VIBRATOR_CONTROL_FILEAMP, null);
        String misc_thm = prefs.getString(FilePath.MISC_THERMAL_CONTROL_FILE, null);
        String misc_tcp = prefs.getString(PREF_TCP_CONGESTION, null);
        String misc_vol = prefs.getString(FilePath.MISC_HEADSET_VOLUME_BOOST_FILE, null);

        // ADD CPU COMMANDS TO THE ARRAY
        ArrayList<String> governorSettings = new ArrayList<String>();
        String max_freq = shell.getInfo(FilePath.CPU_BASE_PATH + 0 +  FilePath.CPU_MAX_FREQ);
        String min_freq = shell.getInfo(FilePath.CPU_BASE_PATH + 0 +  FilePath.CPU_MIN_FREQ);
        for (int k = 0; k < mNumCpus; k++) {
            if (cpu_max != null) {

                shell.queueWork("echo 1 > " + FilePath.CPU_BASE_PATH + k + "/online");
                shell.queueWork("chmod 0666 " + FilePath.CPU_BASE_PATH + k + FilePath.CPU_MAX_FREQ);

                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + FilePath.CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + max_freq + " > " + FilePath.CPU_BASE_PATH + k + FilePath.CPU_MAX_FREQ);
                }

                shell.queueWork("echo " + cpu_max + " > " + FilePath.CPU_BASE_PATH + k + FilePath.CPU_MAX_FREQ);
            }

            if (cpu_min != null) {

                shell.queueWork("echo 1 > " + FilePath.CPU_BASE_PATH + k + "/online");
                shell.queueWork("chmod 0666 " + FilePath.CPU_BASE_PATH + k + FilePath.CPU_MIN_FREQ);

                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + FilePath.CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + min_freq + " > " + FilePath.CPU_BASE_PATH + k + FilePath.CPU_MIN_FREQ);
                }

                shell.queueWork("echo " + cpu_min + " > " + FilePath.CPU_BASE_PATH + k + FilePath.CPU_MIN_FREQ);
            }

            if (cpu_gov != null) {

                shell.queueWork("chmod 0666 " + FilePath.CPU_BASE_PATH + k + FilePath.CURRENT_GOV_AVAILABLE);

                /*
                 * Needs to be executed first, otherwise we would get a NullPointer
                 * For safety reasons we sleep this thread later
                 */
                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + FilePath.CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + shell.getInfo(FilePath.CPU_BASE_PATH + k +
                            FilePath.CURRENT_GOV_AVAILABLE) + " > " + FilePath.CPU_BASE_PATH +
                            k + FilePath.CURRENT_GOV_AVAILABLE);
                }

                shell.queueWork("echo 1 > " + FilePath.CPU_BASE_PATH + k + "/online");
                shell.queueWork("echo " + cpu_gov + " > " + FilePath.CPU_BASE_PATH + k + FilePath.CURRENT_GOV_AVAILABLE);
            }
        }

        if (mem_ios != null) {

            governorSettings.add("chmod 0666 " + FilePath.GOV_IO_FILE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfoString(shell.getInfo(FilePath.GOV_IO_FILE)) + " > " + FilePath.GOV_IO_FILE);

            governorSettings.add("echo " + mem_ios + " > " + FilePath.GOV_IO_FILE);
        }

        if (mem_rah != null) {
            shell.queueWork("chmod 0666 " + FilePath.READAHEAD_PARAMETER);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.READAHEAD_PARAMETER) + " > " + FilePath.READAHEAD_PARAMETER);

            shell.queueWork("echo " + mem_rah + " > " + FilePath.READAHEAD_PARAMETER);
        }

        if (cpu_gov != null || mem_ios != null) {
            // Seriously, we need to set this first because of dependencies;
            shell.setRootInfo(governorSettings.toArray(new String[0]));
        }

        /* GPU Governor */
        if (gpu_gov != null) {

            for (String s: FilePath.GPU_GOV_ARRAY) {
                if (genHelper.doesExist(s))
                    mGPUGov = s;
            }

            shell.queueWork("chmod 0666 " + mGPUGov + "governor");

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(mGPUGov + "governor") + " > " + mGPUGov + "governor");

            shell.queueWork("echo " + gpu_gov + " > " + mGPUGov + "governor");
        }

        // ADD GPU COMMANDS TO THE ARRAY
        if (gpu_max != null) {

            for (String a : FilePath.GPU_FILES) {
                if (genHelper.doesExist(a)) {
                    gpu_file = a;
                    break;
                }
            }
            if (gpu_file != null) {

                shell.queueWork("chmod 0666 " + gpu_file);

                if (Profile != null)
                    defaultProfile.add("echo " + shell.getInfo(gpu_file) + " > " + gpu_file);

                shell.queueWork("echo " + gpu_max + " > " + gpu_file);
            }
        }

        if(genHelper.doesExist(FilePath.GPU_CONTROL_ACTIVE)) {

            shell.queueWork("chmod 0666 " + FilePath.GPU_CONTROL_ACTIVE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.GPU_CONTROL_ACTIVE) + " > " + FilePath.GPU_CONTROL_ACTIVE);

            shell.queueWork("echo " + (gpu_enb ? "1" : "0") + " > " + FilePath.GPU_CONTROL_ACTIVE);
        }

        if(genHelper.doesExist(FilePath.SWEEP2WAKE)) {

            shell.queueWork("chmod 0666 " + FilePath.SWEEP2WAKE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.SWEEP2WAKE) + " > " + FilePath.SWEEP2WAKE);

            shell.queueWork("echo " + (sweep ? "1" : "0") + " > " + FilePath.SWEEP2WAKE);
        }

        if(genHelper.doesExist(FilePath.DOUBLETAP2WAKE)) {

            shell.queueWork("chmod 0666 " + FilePath.DOUBLETAP2WAKE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.DOUBLETAP2WAKE) + " > " + FilePath.DOUBLETAP2WAKE);

            shell.queueWork("echo " + (doubletap ? "1" : "0") + " > " + FilePath.DOUBLETAP2WAKE);
        }

        if (display_color != null) {

            shell.queueWork("chmod 0666 " + FilePath.DISPLAY_COLOR);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.DISPLAY_COLOR) + " > " + FilePath.DISPLAY_COLOR);

            shell.queueWork("echo " + display_color + " > " + FilePath.DISPLAY_COLOR);
        }

        if (rgbValues != null) {

            shell.queueWork("chmod 0666 " + FilePath.COLOR_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.COLOR_CONTROL) + " > " + FilePath.COLOR_CONTROL);

            shell.queueWork("echo " + rgbValues + " > " + FilePath.COLOR_CONTROL);
        }

        // ADD MEM COMMANDS TO THE ARRAY

        if (genHelper.doesExist(FilePath.DYANMIC_FSYNC)) {

            shell.queueWork("chmod 0666 " + FilePath.DYANMIC_FSYNC);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.DYANMIC_FSYNC) + " > " + FilePath.DYANMIC_FSYNC);

            shell.queueWork("echo " + (mem_dfs ? "1" : "0") + " > " + FilePath.DYANMIC_FSYNC);
        }

        if (genHelper.doesExist(FilePath.WRITEBACK)) {

            shell.queueWork("chmod 0666 " + FilePath.WRITEBACK);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.WRITEBACK) + " > " + FilePath.WRITEBACK);

            shell.queueWork("echo " + (mem_wrb ? "1" : "0") + " > " + FilePath.WRITEBACK);
        }

        if (genHelper.doesExist(FilePath.FSYNC)) {

            shell.queueWork("chmod 0666 " + FilePath.FSYNC);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.FSYNC) + " > " + FilePath.FSYNC);

            shell.queueWork("echo " + (mem_fsy ? "Y" : "N") + " > " + FilePath.FSYNC);
        }

        if (genHelper.doesExist(FilePath.KSM_SETTINGS)) {

            shell.queueWork("chmod 0666 " + FilePath.KSM_SETTINGS);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.KSM_SETTINGS) + " > " + FilePath.KSM_SETTINGS);

            shell.queueWork("echo " + (mem_ksm ? "1" : "0") + " > " + FilePath.KSM_SETTINGS);
        }

        // Add misc commands to array
        if (misc_vib != null) {

            shell.queueWork("chmod 0666 " + FilePath.MISC_VIBRATOR_CONTROL_FILE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.MISC_VIBRATOR_CONTROL_FILE) + " > " + FilePath.MISC_VIBRATOR_CONTROL_FILE);

            shell.queueWork("echo " + misc_vib + " > " + FilePath.MISC_VIBRATOR_CONTROL_FILE);
        }

        if (misc_amp != null) {

            shell.queueWork("chmod 0666 " + FilePath.MISC_VIBRATOR_CONTROL_FILEAMP);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.MISC_VIBRATOR_CONTROL_FILEAMP) + " > " + FilePath.MISC_VIBRATOR_CONTROL_FILEAMP);

            shell.queueWork("echo " + misc_amp + " > " + FilePath.MISC_VIBRATOR_CONTROL_FILEAMP);
        }

        if (misc_thm != null) {

            shell.queueWork("chmod 0666 " + FilePath.MISC_THERMAL_CONTROL_FILE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.MISC_THERMAL_CONTROL_FILE) + " > " + FilePath.MISC_THERMAL_CONTROL_FILE);

            shell.queueWork("echo " + misc_thm + " > " + FilePath.MISC_THERMAL_CONTROL_FILE);
        }

        if (misc_tcp != null) {

            shell.queueWork("chmod 0666 " + FilePath.MISC_TCP_CONGESTION_CURRENT);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.MISC_TCP_CONGESTION_CURRENT) + " > " + FilePath.MISC_TCP_CONGESTION_CURRENT);

            shell.queueWork("echo " + misc_tcp + " > " + FilePath.MISC_TCP_CONGESTION_CURRENT);
        }

        if (misc_vol != null) {

            shell.queueWork("chmod 0666 " + FilePath.MISC_HEADSET_VOLUME_BOOST_FILE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(FilePath.MISC_HEADSET_VOLUME_BOOST_FILE) + " > " + FilePath.MISC_HEADSET_VOLUME_BOOST_FILE);

            shell.queueWork("echo " + misc_vol + " > " + FilePath.MISC_HEADSET_VOLUME_BOOST_FILE);
        }

        // Generic Misc Settings;
        if (mMiscSettings != null) {
            final Map<String,?> misc_keys = mMiscSettings.getAll();
            final Map<String,?> aero_keys = prefs.getAll();
            for (final Map.Entry<String,?> a : misc_keys.entrySet()) {

                for (final Map.Entry<String,?> b : aero_keys.entrySet()) {

                    if (a.getKey().equals(b.getKey())) {
                        shell.queueWork("chmod 0666 " + b.getKey());

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(b.getKey()) + " > " + b.getKey());

                        shell.queueWork("echo " + b.getValue() + " > " + b.getKey());
                    }

                }

            }
        }


        // EXECUTE ALL THE COMMANDS COLLECTED
        shell.execWork();
        shell.flushWork();

        // Sleep here to avoid race conditions;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e("Aero", "Something interrupted the main Thread, try again.", e);
        }

        try {
            setSubParameters(mem_ios, Profile, gpu_gov);
        } catch (NullPointerException e) {
            Log.e("Aero", "This shouldn't happen.. Maybe a race condition. ", e);
        }
    }

    public void executeDefault() {

        String[] defaultValues = defaultProfile.toArray(new String[0]);
        shell.setRootInfo(defaultValues);

        defaultProfile.clear();

    }

    /*
     * Fallback-method for getting *old* boolean values
     */
    private Boolean getSaveBoolean(final String s) {

        try {
            return prefs.getString(s, "0").equals("1") ? true : false;
        } catch (ClassCastException e) {
            return prefs.getBoolean(s, false);
        }
    }


    private void setSubParameters(String mem_ios, String Profile, String gpu_gov) throws NullPointerException {

        shellPara.queueWork("sleep 1");
        final String completeVMSettings[] = shellPara.getDirInfo(FilePath.DALVIK_TWEAK, true);
        final String cpu_governor = shell.getInfo(FilePath.CPU_BASE_PATH + 0 + FilePath.CURRENT_GOV_AVAILABLE);
        final String completeGovernorSettingList[] = shellPara.getDirInfo(FilePath.CPU_GOV_BASE + cpu_governor, true);


        if (mem_ios != null) {
            String completeIOSchedulerSettings[] = shellPara.getDirInfo(FilePath.GOV_IO_PARAMETER, true);

            /* IO Scheduler Specific Settings at boot */
            for (String b : completeIOSchedulerSettings) {

                final String ioSettings = prefs.getString(FilePath.GOV_IO_PARAMETER + "/" + b, null);
                if (ioSettings != null) {

                    shellPara.queueWork("chmod 0666 " + FilePath.GOV_IO_PARAMETER + "/" + b);

                    if (Profile != null)
                        defaultProfile.add("echo " + shellPara.getInfo(FilePath.GOV_IO_PARAMETER + "/" + b) + " > " + FilePath.GOV_IO_PARAMETER + "/" + b);

                    shellPara.queueWork("echo " + ioSettings + " > " + FilePath.GOV_IO_PARAMETER + "/" + b);
                    //Log.e("Aero", "Output: " + "echo " + ioSettings + " > " + FilePath.GOV_IO_PARAMETER + "/" + b);
                }
            }
        }

        /* VM specific settings at boot */
        for (String c : completeVMSettings) {

            final String vmSettings = prefs.getString(FilePath.DALVIK_TWEAK + "/" + c, null);
            if (vmSettings != null) {
                shellPara.queueWork("chmod 0666 " + FilePath.DALVIK_TWEAK + "/" + c);

                if (Profile != null)
                    defaultProfile.add("echo " + shellPara.getInfo(FilePath.DALVIK_TWEAK + "/" + c) + " > " + FilePath.DALVIK_TWEAK + "/" + c);

                shellPara.queueWork("echo " + vmSettings + " > " + FilePath.DALVIK_TWEAK + "/" + c);
                //Log.e("Aero", "Output: " + "echo " + vmSettings + " > " + FilePath.DALVIK_TWEAK + "/" + c);
            }
        }

        for (String s : FilePath.HOTPLUG_PATH) {
            if (genHelper.doesExist(s))
                mHotplugPath = s;
        }

        if (genHelper.doesExist(mHotplugPath)) {
            final String completeHotplugSettings[] = shellPara.getDirInfo(mHotplugPath, true);

            /* Hotplug specific settings at boot */
            for (String d : completeHotplugSettings) {

                final String hotplugSettings = prefs.getString(mHotplugPath + "/" + d, null);
                if (hotplugSettings != null) {

                    shellPara.queueWork("chmod 0666 " + mHotplugPath + "/" + d);

                    if (Profile != null)
                        defaultProfile.add("echo " + shellPara.getInfo(mHotplugPath + "/" + d) + " > " + mHotplugPath + "/" + d);

                    shellPara.queueWork("echo " + hotplugSettings + " > " + mHotplugPath + "/" + d);

                    //Log.e("Aero", "Output: " + "echo " + hotplugSettings + " > " + mHotplugPath + "/" + d);
                }
            }
        }

        if (genHelper.doesExist(FilePath.GPU_GOV_PATH)) {
            final String completeGPUGovSettings[] = shellPara.getDirInfo(FilePath.GPU_GOV_PATH, true);

            /* GPU Governor specific settings at boot */
            for (String e : completeGPUGovSettings) {
                final String gpugovSettings = prefs.getString(FilePath.GPU_GOV_PATH + "/" + e, null);
                if (gpugovSettings != null) {
                    shellPara.queueWork("chmod 0666 " + FilePath.GPU_GOV_PATH + "/" + e);

                    if (Profile != null)
                        defaultProfile.add("echo " + shellPara.getInfo(FilePath.GPU_GOV_PATH + "/" + e) + " > " + FilePath.GPU_GOV_PATH + "/" + e);

                    shellPara.queueWork("echo " + gpugovSettings + " > " + FilePath.GPU_GOV_PATH + "/" + e);
                    //Log.e("Aero", "Output: " + "echo " + gpugovSettings + " > " + FilePath.GPU_GOV_PATH + "/" + e);
                }
            }
        }

        /* Governor Specific Settings at boot */

        if (completeGovernorSettingList != null) {

            for (String b : completeGovernorSettingList) {
                final String governorSetting = prefs.getString(FilePath.CPU_GOV_BASE + cpu_governor + "/" + b, null);
                if (governorSetting != null) {

                    shellPara.queueWork("sleep 1");
                    shellPara.queueWork("chmod 0666 " + FilePath.CPU_GOV_BASE + cpu_governor + "/" + b);

                    if (Profile != null) {
                        defaultProfile.add("sleep 1");
                        defaultProfile.add("echo " + shell.getInfo(FilePath.CPU_GOV_BASE + cpu_governor + "/" + b) + " > " + FilePath.CPU_GOV_BASE + cpu_governor + "/" + b);
                    }

                    shellPara.queueWork("echo " + governorSetting + " > " + FilePath.CPU_GOV_BASE + cpu_governor + "/" + b);
                    //Log.e("Aero", "Output: " + "echo " + governorSetting + " > " + FilePath.CPU_GOV_BASE + cpu_governor + "/" + b);
                }
            }
        }

        /* GPU Governor Parameters */
        if (gpu_gov != null) {

            if (mGPUGov == null) {
                for (String s: FilePath.GPU_GOV_ARRAY) {
                    if (genHelper.doesExist(s))
                        mGPUGov = s;
                }
            }

            final String completeGPUGovernorSetting[] = shell.getDirInfo(mGPUGov + gpu_gov, true);

            /* Governor Specific Settings at boot */
            for (String b : completeGPUGovernorSetting) {
                final String governorSetting = prefs.getString(mGPUGov + gpu_gov + "/" + b, null);
                if (governorSetting != null) {

                    shellPara.queueWork("chmod 0666 " + mGPUGov + gpu_gov + "/" + b);

                    if (Profile != null)
                        defaultProfile.add("echo " + shellPara.getInfo(mGPUGov + gpu_gov + "/" + b) + " > " + mGPUGov + gpu_gov + "/" + b);

                    shellPara.queueWork("echo " + governorSetting + " > " + mGPUGov + gpu_gov + "/" + b);
                    //Log.e("Aero", "Output: " + "echo " + governorSetting + " > " + FilePath.GPU_GOV_BASE + gpu_gov + "/" + b);
                }
            }
        }

        // EXECUTE ALL THE COMMANDS COLLECTED
        shellPara.execWork();
        shellPara.flushWork();

    }

}
