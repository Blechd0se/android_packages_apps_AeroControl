package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

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

    public static final String PREF_GOV_IO_FILE = "io_scheduler";
    public static final String PREF_DYANMIC_FSYNC = "dynFsync";
    public static final String PREF_WRITEBACK = "writeback";

    public static final String PREF_HOTPLUG = "/sys/kernel/hotplug_control";
    public static final String PREF_GPU_GOV = "/sys/module/msm_kgsl_core/parameters";

    public static final String MISC_SETTINGS_PATH = "/sys/devices/virtual/timed_output/vibrator/vtg_level";

    private SharedPreferences prefs;
    public static final int mNumCpus = Runtime.getRuntime().availableProcessors();

    private static final shellHelper shell = new shellHelper();

    public void setSettings(final Context context) {

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
                doBackground(context);

            }
        }).start();

    }

    private void doBackground(Context context) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // GET CPU VALUES AND COMMANDS FROM PREFERENCES
        String cpu_max = prefs.getString(PREF_CPU_MAX_FREQ, null);
        String cpu_min = prefs.getString(PREF_CPU_MIN_FREQ, null);
        String cpu_gov = prefs.getString(PREF_CURRENT_GOV_AVAILABLE, null);

        try {
            HashSet<String> hashcpu_cmd = (HashSet<String>) prefs.getStringSet(PREF_CPU_COMMANDS, null);
            if (hashcpu_cmd != null) {
                for (String cmd : hashcpu_cmd)
                    shell.queueWork(cmd);
            }
        } catch (ClassCastException e) {
            // HashSet didn't work, so we make a fallback;
            String cpu_cmd = (String) prefs.getString(PREF_CPU_COMMANDS, null);

            if (cpu_cmd != null) {
                // Since we can't cast to hashmap, little workaround;
                String[] array = cpu_cmd.substring(1, cpu_cmd.length() - 1).split(",");
                for (String cmd : array)
                    shell.queueWork(cmd);
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
        for (int k = 0; k < mNumCpus; k++) {
            if (cpu_max != null)
                shell.queueWork("echo " + cpu_max + " > " + CPU_BASE_PATH + k + CPU_MAX_FREQ);

            if (cpu_min != null)
                shell.queueWork("echo " + cpu_min + " > " + CPU_BASE_PATH + k + CPU_MIN_FREQ);

            if (cpu_gov != null) {

                /*
                 * Needs to be executed first, otherwise we would get a NullPointer
                 * For safety reasons we sleep this thread later
                 */

                shell.queueWork("echo " + cpu_gov + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);
                shell.queueWork("sleep 0.5");
            }
        }

        // ADD GPU COMMANDS TO THE ARRAY
        if (gpu_max != null)
            shell.queueWork("echo " + gpu_max + " > " + GPU_FREQ_MAX);

        if(new File(GPU_CONTROL_ACTIVE).exists())
            shell.queueWork("echo " + (gpu_enb ? "1" : "0") + " > " + GPU_CONTROL_ACTIVE);

        if(new File(SWEEP2WAKE).exists())
            shell.queueWork("echo " + (sweep ? "1" : "0") + " > " + SWEEP2WAKE);

        if (display_color != null)
            shell.queueWork("echo " + display_color + " > " + DISPLAY_COLOR);

        if (rgbValues != null)
            shell.queueWork("echo " + rgbValues + " > " + PERF_COLOR_CONTROL);

        // ADD MEM COMMANDS TO THE ARRAY
        if (mem_ios != null)
            shell.queueWork("echo " + mem_ios + " > " + GOV_IO_FILE);

        shell.queueWork("echo " + (mem_dfs ? "1" : "0") + " > " + DYANMIC_FSYNC);

        shell.queueWork("echo " + (mem_wrb ? "1" : "0") + " > " + WRITEBACK);

        // Add misc commands to array
        if (misc_vib != null)
            shell.queueWork("echo " + misc_vib + " > " + MISC_SETTINGS_PATH);

        try {

            shell.queueWork("sleep 0.5");

            if (cpu_gov != null) {

                final String completeGovernorSettingList[] = shell.getDirInfo(CPU_GOV_BASE + cpu_gov, true);

                /* Governor Specific Settings at boot */

                for (String b : completeGovernorSettingList) {

                    final String governorSetting = prefs.getString(CPU_GOV_BASE + cpu_gov + "/" + b, null);

                    if (governorSetting != null) {
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
                    shell.queueWork("echo " + vmSettings + " > " + DALVIK_TWEAK + "/" + c);

                    //Log.e("Aero", "Output: " + "echo " + vmSettings + " > " + DALVIK_TWEAK + "/" + c);
                }
            }

            final String completeHotplugSettings[] = shell.getDirInfo(PREF_HOTPLUG, true);

            /* Hotplug specific settings at boot */

            for (String d : completeHotplugSettings) {

                final String hotplugSettings = prefs.getString(PREF_HOTPLUG + "/" + d, null);

                if (hotplugSettings != null) {
                    shell.queueWork("echo " + hotplugSettings + " > " + PREF_HOTPLUG + "/" + d);

                    //Log.e("Aero", "Output: " + "echo " + hotplugSettings + " > " + PREF_HOTPLUG + "/" + d);
                }
            }

            final String completeGPUGovSettings[] = shell.getDirInfo(PREF_GPU_GOV, true);

            /* GPU Governor specific settings at boot */

            for (String e : completeGPUGovSettings) {

                final String gpugovSettings = prefs.getString(PREF_GPU_GOV + "/" + e, null);

                if (gpugovSettings != null) {
                    shell.queueWork("echo " + gpugovSettings + " > " + PREF_GPU_GOV + "/" + e);

                    //Log.e("Aero", "Output: " + "echo " + gpugovSettings + " > " + PREF_GPU_GOV + "/" + e);
                }
            }

        } catch (NullPointerException e) {
            Log.e("Aero", "This shouldn't happen.. Maybe a race condition. ", e);
        }

        // EXECUTE ALL THE COMMANDS COLLECTED
        shell.execWork();
        shell.flushWork();
    }

}
