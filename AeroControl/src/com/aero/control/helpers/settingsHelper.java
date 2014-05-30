package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Alexander Christ on 05.01.14.
 */
public class settingsHelper {

    public static final String CPU_BASE_PATH = "/sys/devices/system/cpu/cpu";
    public static final String CPU_GOV_BASE = "/sys/devices/system/cpu/cpufreq/";
    public static final String CURRENT_GOV_AVAILABLE = "/cpufreq/scaling_governor";
    public static final String CPU_MAX_FREQ = "/cpufreq/scaling_max_freq";
    public static final String CPU_MIN_FREQ = "/cpufreq/scaling_min_freq";

    public static final String GPU_FREQ_MAX = "/sys/kernel/gpu_control/max_freq";
    public static final String GPU_CONTROL_ACTIVE = "/sys/kernel/gpu_control/gpu_control_active";
    public static final String DISPLAY_COLOR = "/sys/class/misc/display_control/display_brightness_value";
    public static final String SWEEP2WAKE = "/sys/android_touch/sweep2wake";

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String DYANMIC_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String WRITEBACK = "/sys/devices/virtual/misc/writeback/writeback_enabled";
    public static final String DALVIK_TWEAK = "/proc/sys/vm";

    public static final String PREF_CURRENT_GOV_AVAILABLE = "set_governor";
    public static final String PREF_CPU_MAX_FREQ = "max_frequency";
    public static final String PREF_CPU_MIN_FREQ = "min_frequency";
    public static final String PREF_CPU_COMMANDS = "cpu_commands";

    public static final String PREF_GPU_FREQ_MAX = "gpu_max_freq";
    public static final String PREF_GPU_CONTROL_ACTIVE = "gpu_control_enable";
    public static final String PREF_DISPLAY_COLOR = "display_control";
    public static final String PREF_SWEEP2WAKE = "sweeptowake";
    public static final String PERF_COLOR_CONTROL = "/sys/devices/platform/kcal_ctrl.0/kcal";

    public static final String PREF_GOV_IO_FILE = "io_scheduler_list";
    public static final String PREF_DYANMIC_FSYNC = "dynFsync";
    public static final String PREF_WRITEBACK = "writeback";

    public static final String PREF_HOTPLUG = "/sys/kernel/hotplug_control";
    public static final String PREF_GPU_GOV = "/sys/module/msm_kgsl_core/parameters";

    public static final String MISC_SETTINGS_PATH = "/sys/devices/virtual/timed_output/vibrator/vtg_level";

    private SharedPreferences prefs;
    public static final int mNumCpus = Runtime.getRuntime().availableProcessors();

    private static final shellHelper shell = new shellHelper();
    private static final ArrayList<String> defaultProfile = new ArrayList<String>();

    public void setSettings(final Context context, final String Profile) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // We need to sleep here for a short while for the kernel
                shell.setOverclockAddress();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e("Aero", "Something went really wrong...", e);
                }

                // Apply all our saved values;
                doBackground(context, Profile);

            }
        }).start();

    }

    private void doBackground(Context context, String Profile) {


        if (Profile == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        else
            prefs = context.getSharedPreferences(Profile, Context.MODE_PRIVATE);

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

        // GET GPU VALUES FROM PREFERENCES
        String gpu_max = prefs.getString(PREF_GPU_FREQ_MAX, null);
        String display_color = prefs.getString(PREF_DISPLAY_COLOR, null);
        Boolean gpu_enb = prefs.getBoolean(PREF_GPU_CONTROL_ACTIVE, false);
        Boolean sweep = prefs.getBoolean(PREF_SWEEP2WAKE, false);
        String rgbValues = prefs.getString("rgbValues", null);
        // GET MEM VALUES FROM PREFERENCES
        String mem_ios = prefs.getString(PREF_GOV_IO_FILE, null);
        Boolean mem_dfs = prefs.getBoolean(PREF_DYANMIC_FSYNC, false);
        Boolean mem_wrb = prefs.getBoolean(PREF_WRITEBACK, false);
        // Get Misc Settings from preferences
        String misc_vib = prefs.getString(MISC_SETTINGS_PATH, null);

        // ADD CPU COMMANDS TO THE ARRAY
        ArrayList<String> governorSettings = new ArrayList<String>();
        for (int k = 0; k < mNumCpus; k++) {
            if (cpu_max != null) {
                if (Profile != null)
                    defaultProfile.add("echo " + shell.getInfo(CPU_BASE_PATH + k + CPU_MAX_FREQ) + " > " + CPU_BASE_PATH + k + CPU_MAX_FREQ);

                shell.queueWork("echo " + cpu_max + " > " + CPU_BASE_PATH + k + CPU_MAX_FREQ);
            }

            if (cpu_min != null) {
                if (Profile != null)
                    defaultProfile.add("echo " + shell.getInfo(CPU_BASE_PATH + k + CPU_MIN_FREQ) + " > " + CPU_BASE_PATH + k + CPU_MIN_FREQ);

                shell.queueWork("echo " + cpu_min + " > " + CPU_BASE_PATH + k + CPU_MIN_FREQ);
            }

            if (cpu_gov != null) {

                /*
                 * Needs to be executed first, otherwise we would get a NullPointer
                 * For safety reasons we sleep this thread later
                 */
                if (Profile != null)
                    defaultProfile.add("echo " + shell.getInfo(CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE) + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);

                governorSettings.add("echo " + cpu_gov + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);
            }
        }
        if (cpu_gov != null) {
            // Seriously, we need to set this first because of dependencies;
            shell.setRootInfo(governorSettings.toArray(new String[0]));
        }

        // ADD GPU COMMANDS TO THE ARRAY
        if (gpu_max != null) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(GPU_FREQ_MAX) + " > " + GPU_FREQ_MAX);

            shell.queueWork("echo " + gpu_max + " > " + GPU_FREQ_MAX);
        }

        if(new File(GPU_CONTROL_ACTIVE).exists()) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(GPU_CONTROL_ACTIVE) + " > " + GPU_CONTROL_ACTIVE);

            shell.queueWork("echo " + (gpu_enb ? "1" : "0") + " > " + GPU_CONTROL_ACTIVE);
        }

        if(new File(SWEEP2WAKE).exists()) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(SWEEP2WAKE) + " > " + SWEEP2WAKE);

            shell.queueWork("echo " + (sweep ? "1" : "0") + " > " + SWEEP2WAKE);
        }

        if (display_color != null) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(DISPLAY_COLOR) + " > " + DISPLAY_COLOR);

            shell.queueWork("echo " + display_color + " > " + DISPLAY_COLOR);
        }

        if (rgbValues != null) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(PERF_COLOR_CONTROL) + " > " + PERF_COLOR_CONTROL);

            shell.queueWork("echo " + rgbValues + " > " + PERF_COLOR_CONTROL);
        }

        // ADD MEM COMMANDS TO THE ARRAY
        if (mem_ios != null) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfoString(shell.getInfo(GOV_IO_FILE)) + " > " + GOV_IO_FILE);

            shell.queueWork("echo " + mem_ios + " > " + GOV_IO_FILE);
        }

        if (new File(DYANMIC_FSYNC).exists()) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(DYANMIC_FSYNC) + " > " + DYANMIC_FSYNC);

            shell.queueWork("echo " + (mem_dfs ? "1" : "0") + " > " + DYANMIC_FSYNC);
        }

        if (new File(WRITEBACK).exists()) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(WRITEBACK) + " > " + WRITEBACK);

            shell.queueWork("echo " + (mem_wrb ? "1" : "0") + " > " + WRITEBACK);
        }

        // Add misc commands to array
        if (misc_vib != null) {
            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(MISC_SETTINGS_PATH) + " > " + MISC_SETTINGS_PATH);

            shell.queueWork("echo " + misc_vib + " > " + MISC_SETTINGS_PATH);
        }

        try {

            if (cpu_gov != null) {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                }

                final String completeGovernorSettingList[] = shell.getDirInfo(CPU_GOV_BASE + cpu_gov, true);

                /* Governor Specific Settings at boot */

                for (String b : completeGovernorSettingList) {

                    final String governorSetting = prefs.getString(CPU_GOV_BASE + cpu_gov + "/" + b, null);

                    if (governorSetting != null) {
                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(CPU_GOV_BASE + cpu_gov + "/" + b) + " > " + CPU_GOV_BASE + cpu_gov + "/" + b);

                        shell.queueWork("echo " + governorSetting + " > " + CPU_GOV_BASE + cpu_gov + "/" + b);

                        //Log.e("Aero", "Output: " + "echo " + governorSetting + " > " + CPU_GOV_BASE + cpu_gov + "/" + b);
                    }
                }
            }

            final String completeVMSettings[] = shell.getDirInfo(DALVIK_TWEAK, true);

            /* VM specific settings at boot */

            for (String c : completeVMSettings) {

                final String vmSettings = prefs.getString(DALVIK_TWEAK + "/" + c, null);

                if (vmSettings != null) {
                    if (Profile != null)
                        defaultProfile.add("echo " + shell.getInfo(DALVIK_TWEAK + "/" + c) + " > " + DALVIK_TWEAK + "/" + c);

                    shell.queueWork("echo " + vmSettings + " > " + DALVIK_TWEAK + "/" + c);

                    //Log.e("Aero", "Output: " + "echo " + vmSettings + " > " + DALVIK_TWEAK + "/" + c);
                }
            }

            if (new File(PREF_HOTPLUG). exists()) {
                final String completeHotplugSettings[] = shell.getDirInfo(PREF_HOTPLUG, true);

                /* Hotplug specific settings at boot */

                for (String d : completeHotplugSettings) {

                    final String hotplugSettings = prefs.getString(PREF_HOTPLUG + "/" + d, null);

                    if (hotplugSettings != null) {
                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(PREF_HOTPLUG + "/" + d) + " > " + PREF_HOTPLUG + "/" + d);

                        shell.queueWork("echo " + hotplugSettings + " > " + PREF_HOTPLUG + "/" + d);

                        //Log.e("Aero", "Output: " + "echo " + hotplugSettings + " > " + PREF_HOTPLUG + "/" + d);
                    }
                }
            }

            if (new File(PREF_GPU_GOV).exists()) {
                final String completeGPUGovSettings[] = shell.getDirInfo(PREF_GPU_GOV, true);

                /* GPU Governor specific settings at boot */

                for (String e : completeGPUGovSettings) {

                    final String gpugovSettings = prefs.getString(PREF_GPU_GOV + "/" + e, null);

                    if (gpugovSettings != null) {
                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(PREF_GPU_GOV + "/" + e) + " > " + PREF_GPU_GOV + "/" + e);

                        shell.queueWork("echo " + gpugovSettings + " > " + PREF_GPU_GOV + "/" + e);

                        //Log.e("Aero", "Output: " + "echo " + gpugovSettings + " > " + PREF_GPU_GOV + "/" + e);
                    }
                }
            }

        } catch (NullPointerException e) {
            Log.e("Aero", "This shouldn't happen.. Maybe a race condition. ", e);
        }

        // EXECUTE ALL THE COMMANDS COLLECTED
        shell.execWork();
        shell.flushWork();
    }

    public void executeDefault() {

        String[] abc = defaultProfile.toArray(new String[0]);
        shell.setRootInfo(abc);

        defaultProfile.clear();

    }
}
