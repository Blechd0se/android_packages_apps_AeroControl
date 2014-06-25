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
    public static final String[] GPU_FILES = {"/sys/kernel/gpu_control/max_freq", /* Defy 2.6 Kernel */
                                                "/sys/class/kgsl/kgsl-3d0/max_gpuclk", /* Adreno GPUs */
                                                "/sys/devices/platform/omap/pvrsrvkm.0/sgx_fck_rate" /* Defy 3.0 Kernel */};
    public static final String GPU_CONTROL_ACTIVE = "/sys/kernel/gpu_control/gpu_control_active";
    public static final String DISPLAY_COLOR = "/sys/class/misc/display_control/display_brightness_value";
    public static final String SWEEP2WAKE = "/sys/android_touch/sweep2wake";

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String GOV_IO_PARAMETER = "/sys/block/mmcblk0/queue/iosched";
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
    public static final String PREF_VOLTAGE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";

    public static final String MISC_VIBRATOR_CONTROL = "/sys/devices/virtual/timed_output/vibrator/vtg_level";
    private static final String MISC_THERMAL_CONTROL = "/sys/module/msm_thermal/parameters/temp_threshold";

    private SharedPreferences prefs;
    private String gpu_file;
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
        String voltage = prefs.getString("voltage_values", null);

        if (voltage != null)
            shell.queueWork("echo " + voltage + " > " + PREF_VOLTAGE_PATH);

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
        String misc_vib = prefs.getString(MISC_VIBRATOR_CONTROL, null);
        String misc_thm = prefs.getString(MISC_THERMAL_CONTROL, null);

        // ADD CPU COMMANDS TO THE ARRAY
        ArrayList<String> governorSettings = new ArrayList<String>();
        String max_freq = shell.getInfo(CPU_BASE_PATH + 0 +  CPU_MAX_FREQ);
        String min_freq = shell.getInfo(CPU_BASE_PATH + 0 +  CPU_MIN_FREQ);
        for (int k = 0; k < mNumCpus; k++) {
            if (cpu_max != null) {

                shell.queueWork("echo 1 > " + CPU_BASE_PATH + k + "/online");
                shell.queueWork("chmod 0666 " + CPU_BASE_PATH + k + CPU_MAX_FREQ);

                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + max_freq + " > " + CPU_BASE_PATH + k + CPU_MAX_FREQ);
                }

                shell.queueWork("echo " + cpu_max + " > " + CPU_BASE_PATH + k + CPU_MAX_FREQ);
            }

            if (cpu_min != null) {

                shell.queueWork("echo 1 > " + CPU_BASE_PATH + k + "/online");
                shell.queueWork("chmod 0666 " + CPU_BASE_PATH + k + CPU_MIN_FREQ);

                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + min_freq + " > " + CPU_BASE_PATH + k + CPU_MIN_FREQ);
                }

                shell.queueWork("echo " + cpu_min + " > " + CPU_BASE_PATH + k + CPU_MIN_FREQ);
            }

            if (cpu_gov != null) {

                governorSettings.add("chmod 0666 " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);

                /*
                 * Needs to be executed first, otherwise we would get a NullPointer
                 * For safety reasons we sleep this thread later
                 */
                if (Profile != null)
                    defaultProfile.add("echo " + shell.getInfo(CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE) + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);

                governorSettings.add("echo " + cpu_gov + " > " + CPU_BASE_PATH + k + CURRENT_GOV_AVAILABLE);
            }
        }
        if (mem_ios != null) {

            governorSettings.add("chmod 0666 " + GOV_IO_FILE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfoString(shell.getInfo(GOV_IO_FILE)) + " > " + GOV_IO_FILE);

            governorSettings.add("echo " + mem_ios + " > " + GOV_IO_FILE);
        }

        if (cpu_gov != null || mem_ios != null) {
            // Seriously, we need to set this first because of dependencies;
            shell.setRootInfo(governorSettings.toArray(new String[0]));
        }

        // ADD GPU COMMANDS TO THE ARRAY
        if (gpu_max != null) {

            for (String a : GPU_FILES) {
                if (new File(a).exists()) {
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

        if(new File(GPU_CONTROL_ACTIVE).exists()) {

            shell.queueWork("chmod 0666 " + GPU_CONTROL_ACTIVE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(GPU_CONTROL_ACTIVE) + " > " + GPU_CONTROL_ACTIVE);

            shell.queueWork("echo " + (gpu_enb ? "1" : "0") + " > " + GPU_CONTROL_ACTIVE);
        }

        if(new File(SWEEP2WAKE).exists()) {

            shell.queueWork("chmod 0666 " + SWEEP2WAKE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(SWEEP2WAKE) + " > " + SWEEP2WAKE);

            shell.queueWork("echo " + (sweep ? "1" : "0") + " > " + SWEEP2WAKE);
        }

        if (display_color != null) {

            shell.queueWork("chmod 0666 " + DISPLAY_COLOR);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(DISPLAY_COLOR) + " > " + DISPLAY_COLOR);

            shell.queueWork("echo " + display_color + " > " + DISPLAY_COLOR);
        }

        if (rgbValues != null) {

            shell.queueWork("chmod 0666 " + PERF_COLOR_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(PERF_COLOR_CONTROL) + " > " + PERF_COLOR_CONTROL);

            shell.queueWork("echo " + rgbValues + " > " + PERF_COLOR_CONTROL);
        }

        // ADD MEM COMMANDS TO THE ARRAY

        if (new File(DYANMIC_FSYNC).exists()) {

            shell.queueWork("chmod 0666 " + DYANMIC_FSYNC);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(DYANMIC_FSYNC) + " > " + DYANMIC_FSYNC);

            shell.queueWork("echo " + (mem_dfs ? "1" : "0") + " > " + DYANMIC_FSYNC);
        }

        if (new File(WRITEBACK).exists()) {

            shell.queueWork("chmod 0666 " + WRITEBACK);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(WRITEBACK) + " > " + WRITEBACK);

            shell.queueWork("echo " + (mem_wrb ? "1" : "0") + " > " + WRITEBACK);
        }

        // Add misc commands to array
        if (misc_vib != null) {

            shell.queueWork("chmod 0666 " + MISC_VIBRATOR_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(MISC_VIBRATOR_CONTROL) + " > " + MISC_VIBRATOR_CONTROL);

            shell.queueWork("echo " + misc_vib + " > " + MISC_VIBRATOR_CONTROL);
        }

        if (misc_thm != null) {

            shell.queueWork("chmod 0666 " + MISC_THERMAL_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(MISC_THERMAL_CONTROL) + " > " + MISC_THERMAL_CONTROL);

            shell.queueWork("echo " + misc_thm + " > " + MISC_THERMAL_CONTROL);
        }

        try {

            if (mem_ios != null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                }
                String completeIOSchedulerSettings[] = shell.getDirInfo(GOV_IO_PARAMETER, true);

                /* IO Scheduler Specific Settings at boot */

                for (String b : completeIOSchedulerSettings) {

                    final String ioSettings = prefs.getString(GOV_IO_PARAMETER + "/" + b, null);

                    if (ioSettings != null) {

                        shell.queueWork("chmod 0666 " + GOV_IO_PARAMETER + "/" + b);

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(GOV_IO_PARAMETER + "/" + b) + " > " + GOV_IO_PARAMETER + "/" + b);

                        shell.queueWork("echo " + ioSettings + " > " + GOV_IO_PARAMETER + "/" + b);

                        //Log.e("Aero", "Output: " + "echo " + ioSettings + " > " + GOV_IO_PARAMETER + "/" + b);
                    }
                }
            }

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

                        shell.queueWork("chmod 0666 " + CPU_GOV_BASE + cpu_gov + "/" + b);

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

                    shell.queueWork("chmod 0666 " + DALVIK_TWEAK + "/" + c);

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

                        shell.queueWork("chmod 0666 " + PREF_HOTPLUG + "/" + d);

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

                        shell.queueWork("chmod 0666 " + PREF_GPU_GOV + "/" + e);

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
