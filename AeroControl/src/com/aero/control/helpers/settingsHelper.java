package com.aero.control.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aero.control.AeroActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Alexander Christ on 05.01.14.
 */
public class settingsHelper {
    public static final String PREF_CURRENT_GOV_AVAILABLE = "set_governor";
    public static final String PREF_CPU_MAX_FREQ = "max_frequency";
    public static final String PREF_CPU_MIN_FREQ = "min_frequency";
    public static final String PREF_CPU_COMMANDS = "cpu_commands";

    public static final String PREF_CURRENT_GPU_GOV_AVAILABLE = "set_gpu_governor";
    public static final String PREF_GPU_FREQ_MAX = "gpu_max_freq";
    public static final String PREF_GPU_CONTROL_ACTIVE = "gpu_control_enable";
    public static final String PREF_DISPLAY_COLOR = "display_control";
    public static final String PREF_SWEEP2WAKE = "sweeptowake";
    public static final String PREF_DOUBLETAP2WAKE = "doubletaptowake";

    public static final String PREF_GOV_IO_FILE = "io_scheduler_list";
    public static final String PREF_DYANMIC_FSYNC = "dynFsync";
    public static final String PREF_WRITEBACK = "writeback";

    private SharedPreferences prefs;
    private String gpu_file;
    public static final int mNumCpus = Runtime.getRuntime().availableProcessors();

    private static final shellHelper shell = new shellHelper();
    private static final ArrayList<String> defaultProfile = new ArrayList<String>();

    public void setSettings(final Context context, final String Profile) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (AeroActivity.files == null)
                    AeroActivity.files = new FilePath();

                // We need to sleep here for a short while for the kernel
                if (shell.setOverclockAddress()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e("Aero", "Something went really wrong...", e);
                    }
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
            shell.queueWork("echo " + voltage + " > " + AeroActivity.files.VOLTAGE_PATH);

        // GET GPU VALUES FROM PREFERENCES
        String gpu_gov = prefs.getString(PREF_CURRENT_GPU_GOV_AVAILABLE, null);
        String gpu_max = prefs.getString(PREF_GPU_FREQ_MAX, null);
        String display_color = prefs.getString(PREF_DISPLAY_COLOR, null);
        Boolean gpu_enb = prefs.getString(PREF_GPU_CONTROL_ACTIVE, "0").equals("1") ? true : false;
        Boolean sweep = prefs.getString(PREF_SWEEP2WAKE, "0").equals("1") ? true : false;
        Boolean doubletap = prefs.getString(PREF_DOUBLETAP2WAKE, "0").equals("1") ? true : false;
        String rgbValues = prefs.getString("rgbValues", null);
        // GET MEM VALUES FROM PREFERENCES
        String mem_ios = prefs.getString(PREF_GOV_IO_FILE, null);
        Boolean mem_dfs = prefs.getString(PREF_DYANMIC_FSYNC, "0").equals("1") ? true : false;
        Boolean mem_wrb = prefs.getString(PREF_WRITEBACK, "0").equals("1") ? true : false;
        // Get Misc Settings from preferences
        String misc_vib = prefs.getString(AeroActivity.files.MISC_VIBRATOR_CONTROL, null);
        String misc_thm = prefs.getString(AeroActivity.files.MISC_THERMAL_CONTROL, null);

        // ADD CPU COMMANDS TO THE ARRAY
        ArrayList<String> governorSettings = new ArrayList<String>();
        String max_freq = shell.getInfo(AeroActivity.files.CPU_BASE_PATH + 0 +  AeroActivity.files.CPU_MAX_FREQ);
        String min_freq = shell.getInfo(AeroActivity.files.CPU_BASE_PATH + 0 +  AeroActivity.files.CPU_MIN_FREQ);
        for (int k = 0; k < mNumCpus; k++) {
            if (cpu_max != null) {

                shell.queueWork("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
                shell.queueWork("chmod 0666 " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MAX_FREQ);

                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + max_freq + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MAX_FREQ);
                }

                shell.queueWork("echo " + cpu_max + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MAX_FREQ);
            }

            if (cpu_min != null) {

                shell.queueWork("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
                shell.queueWork("chmod 0666 " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MIN_FREQ);

                if (Profile != null) {
                    defaultProfile.add("echo 1 > " + AeroActivity.files.CPU_BASE_PATH + k + "/online");
                    defaultProfile.add("echo " + min_freq + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MIN_FREQ);
                }

                shell.queueWork("echo " + cpu_min + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CPU_MIN_FREQ);
            }

            if (cpu_gov != null) {

                governorSettings.add("chmod 0666 " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CURRENT_GOV_AVAILABLE);

                /*
                 * Needs to be executed first, otherwise we would get a NullPointer
                 * For safety reasons we sleep this thread later
                 */
                if (Profile != null)
                    defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.CPU_BASE_PATH + k +
                            AeroActivity.files.CURRENT_GOV_AVAILABLE) + " > " + AeroActivity.files.CPU_BASE_PATH +
                            k + AeroActivity.files.CURRENT_GOV_AVAILABLE);

                governorSettings.add("echo " + cpu_gov + " > " + AeroActivity.files.CPU_BASE_PATH + k + AeroActivity.files.CURRENT_GOV_AVAILABLE);
            }
        }
        if (mem_ios != null) {

            governorSettings.add("chmod 0666 " + AeroActivity.files.GOV_IO_FILE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfoString(shell.getInfo(AeroActivity.files.GOV_IO_FILE)) + " > " + AeroActivity.files.GOV_IO_FILE);

            governorSettings.add("echo " + mem_ios + " > " + AeroActivity.files.GOV_IO_FILE);
        }

        if (cpu_gov != null || mem_ios != null) {
            // Seriously, we need to set this first because of dependencies;
            shell.setRootInfo(governorSettings.toArray(new String[0]));
        }

        /* GPU Governor */
        if (gpu_gov != null) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.GPU_GOV_BASE + "governor");

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.GPU_GOV_BASE + "governor") + " > " + AeroActivity.files.GPU_GOV_BASE + "governor");

            shell.queueWork("echo " + gpu_gov + " > " + AeroActivity.files.GPU_GOV_BASE + "governor");
        }

        // ADD GPU COMMANDS TO THE ARRAY
        if (gpu_max != null) {

            for (String a : AeroActivity.files.GPU_FILES) {
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

        if(new File(AeroActivity.files.GPU_CONTROL_ACTIVE).exists()) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.GPU_CONTROL_ACTIVE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.GPU_CONTROL_ACTIVE) + " > " + AeroActivity.files.GPU_CONTROL_ACTIVE);

            shell.queueWork("echo " + (gpu_enb ? "1" : "0") + " > " + AeroActivity.files.GPU_CONTROL_ACTIVE);
        }

        if(new File(AeroActivity.files.SWEEP2WAKE).exists()) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.SWEEP2WAKE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.SWEEP2WAKE) + " > " + AeroActivity.files.SWEEP2WAKE);

            shell.queueWork("echo " + (sweep ? "1" : "0") + " > " + AeroActivity.files.SWEEP2WAKE);
        }

        if(new File(AeroActivity.files.DOUBLETAP2WAKE).exists()) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.DOUBLETAP2WAKE);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.DOUBLETAP2WAKE) + " > " + AeroActivity.files.DOUBLETAP2WAKE);

            shell.queueWork("echo " + (doubletap ? "1" : "0") + " > " + AeroActivity.files.DOUBLETAP2WAKE);
        }

        if (display_color != null) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.DISPLAY_COLOR);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.DISPLAY_COLOR) + " > " + AeroActivity.files.DISPLAY_COLOR);

            shell.queueWork("echo " + display_color + " > " + AeroActivity.files.DISPLAY_COLOR);
        }

        if (rgbValues != null) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.COLOR_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.COLOR_CONTROL) + " > " + AeroActivity.files.COLOR_CONTROL);

            shell.queueWork("echo " + rgbValues + " > " + AeroActivity.files.COLOR_CONTROL);
        }

        // ADD MEM COMMANDS TO THE ARRAY

        if (new File(AeroActivity.files.DYANMIC_FSYNC).exists()) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.DYANMIC_FSYNC);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.DYANMIC_FSYNC) + " > " + AeroActivity.files.DYANMIC_FSYNC);

            shell.queueWork("echo " + (mem_dfs ? "1" : "0") + " > " + AeroActivity.files.DYANMIC_FSYNC);
        }

        if (new File(AeroActivity.files.WRITEBACK).exists()) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.WRITEBACK);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.WRITEBACK) + " > " + AeroActivity.files.WRITEBACK);

            shell.queueWork("echo " + (mem_wrb ? "1" : "0") + " > " + AeroActivity.files.WRITEBACK);
        }

        // Add misc commands to array
        if (misc_vib != null) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.MISC_VIBRATOR_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.MISC_VIBRATOR_CONTROL) + " > " + AeroActivity.files.MISC_VIBRATOR_CONTROL);

            shell.queueWork("echo " + misc_vib + " > " + AeroActivity.files.MISC_VIBRATOR_CONTROL);
        }

        if (misc_thm != null) {

            shell.queueWork("chmod 0666 " + AeroActivity.files.MISC_THERMAL_CONTROL);

            if (Profile != null)
                defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.MISC_THERMAL_CONTROL) + " > " + AeroActivity.files.MISC_THERMAL_CONTROL);

            shell.queueWork("echo " + misc_thm + " > " + AeroActivity.files.MISC_THERMAL_CONTROL);
        }

        try {

            if (mem_ios != null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("Aero", "Something interrupted the main Thread, try again.", e);
                }
                String completeIOSchedulerSettings[] = shell.getDirInfo(AeroActivity.files.GOV_IO_PARAMETER, true);

                /* IO Scheduler Specific Settings at boot */

                for (String b : completeIOSchedulerSettings) {

                    final String ioSettings = prefs.getString(AeroActivity.files.GOV_IO_PARAMETER + "/" + b, null);

                    if (ioSettings != null) {

                        shell.queueWork("chmod 0666 " + AeroActivity.files.GOV_IO_PARAMETER + "/" + b);

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.GOV_IO_PARAMETER + "/" + b) + " > " + AeroActivity.files.GOV_IO_PARAMETER + "/" + b);

                        shell.queueWork("echo " + ioSettings + " > " + AeroActivity.files.GOV_IO_PARAMETER + "/" + b);

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

                final String completeGovernorSettingList[] = shell.getDirInfo(AeroActivity.files.CPU_GOV_BASE + cpu_gov, true);

                /* Governor Specific Settings at boot */

                for (String b : completeGovernorSettingList) {

                    final String governorSetting = prefs.getString(AeroActivity.files.CPU_GOV_BASE + cpu_gov + "/" + b, null);

                    if (governorSetting != null) {

                        shell.queueWork("chmod 0666 " + AeroActivity.files.CPU_GOV_BASE + cpu_gov + "/" + b);

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.CPU_GOV_BASE + cpu_gov + "/" + b) + " > " + AeroActivity.files.CPU_GOV_BASE + cpu_gov + "/" + b);

                        shell.queueWork("echo " + governorSetting + " > " + AeroActivity.files.CPU_GOV_BASE + cpu_gov + "/" + b);

                        //Log.e("Aero", "Output: " + "echo " + governorSetting + " > " + CPU_GOV_BASE + cpu_gov + "/" + b);
                    }
                }
            }

            final String completeVMSettings[] = shell.getDirInfo(AeroActivity.files.DALVIK_TWEAK, true);

            /* VM specific settings at boot */

            for (String c : completeVMSettings) {

                final String vmSettings = prefs.getString(AeroActivity.files.DALVIK_TWEAK + "/" + c, null);

                if (vmSettings != null) {

                    shell.queueWork("chmod 0666 " + AeroActivity.files.DALVIK_TWEAK + "/" + c);

                    if (Profile != null)
                        defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.DALVIK_TWEAK + "/" + c) + " > " + AeroActivity.files.DALVIK_TWEAK + "/" + c);

                    shell.queueWork("echo " + vmSettings + " > " + AeroActivity.files.DALVIK_TWEAK + "/" + c);

                    //Log.e("Aero", "Output: " + "echo " + vmSettings + " > " + DALVIK_TWEAK + "/" + c);
                }
            }

            if (new File(AeroActivity.files.HOTPLUG_PATH). exists()) {
                final String completeHotplugSettings[] = shell.getDirInfo(AeroActivity.files.HOTPLUG_PATH, true);

                /* Hotplug specific settings at boot */

                for (String d : completeHotplugSettings) {

                    final String hotplugSettings = prefs.getString(AeroActivity.files.HOTPLUG_PATH + "/" + d, null);

                    if (hotplugSettings != null) {

                        shell.queueWork("chmod 0666 " + AeroActivity.files.HOTPLUG_PATH + "/" + d);

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.HOTPLUG_PATH + "/" + d) + " > " + AeroActivity.files.HOTPLUG_PATH + "/" + d);

                        shell.queueWork("echo " + hotplugSettings + " > " + AeroActivity.files.HOTPLUG_PATH + "/" + d);

                        //Log.e("Aero", "Output: " + "echo " + hotplugSettings + " > " + PREF_HOTPLUG + "/" + d);
                    }
                }
            }

            if (new File(AeroActivity.files.GPU_GOV_PATH).exists()) {
                final String completeGPUGovSettings[] = shell.getDirInfo(AeroActivity.files.GPU_GOV_PATH, true);

                /* GPU Governor specific settings at boot */

                for (String e : completeGPUGovSettings) {

                    final String gpugovSettings = prefs.getString(AeroActivity.files.GPU_GOV_PATH + "/" + e, null);

                    if (gpugovSettings != null) {

                        shell.queueWork("chmod 0666 " + AeroActivity.files.GPU_GOV_PATH + "/" + e);

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.GPU_GOV_PATH + "/" + e) + " > " + AeroActivity.files.GPU_GOV_PATH + "/" + e);

                        shell.queueWork("echo " + gpugovSettings + " > " + AeroActivity.files.GPU_GOV_PATH + "/" + e);

                        //Log.e("Aero", "Output: " + "echo " + gpugovSettings + " > " + PREF_GPU_GOV + "/" + e);
                    }
                }
            }

            /* GPU Governor Parameters */
            if (gpu_gov != null) {

                final String completeGPUGovernorSetting[] = shell.getDirInfo(AeroActivity.files.GPU_GOV_BASE + gpu_gov, true);

                /* Governor Specific Settings at boot */

                for (String b : completeGPUGovernorSetting) {

                    final String governorSetting = prefs.getString(AeroActivity.files.GPU_GOV_BASE + gpu_gov + "/" + b, null);

                    if (governorSetting != null) {

                        shell.queueWork("chmod 0666 " + AeroActivity.files.GPU_GOV_BASE + gpu_gov + "/" + b);

                        if (Profile != null)
                            defaultProfile.add("echo " + shell.getInfo(AeroActivity.files.GPU_GOV_BASE + gpu_gov + "/" + b) + " > " + AeroActivity.files.GPU_GOV_BASE + gpu_gov + "/" + b);

                        shell.queueWork("echo " + governorSetting + " > " + AeroActivity.files.GPU_GOV_BASE + gpu_gov + "/" + b);

                        Log.e("Aero", "Output: " + "echo " + governorSetting + " > " + AeroActivity.files.GPU_GOV_BASE + gpu_gov + "/" + b);
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
