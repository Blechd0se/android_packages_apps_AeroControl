package com.aero.control.boot;

import java.util.ArrayList;
import java.util.HashSet;

import com.aero.control.helpers.shellHelper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;


public class bootService extends Service
{
    public static final String CURRENT_GOV_AVAILABLE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String CPU_MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String CPU_MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";

    public static final String GPU_FREQ_MAX = "/sys/kernel/gpu_control/max_freq";
    public static final String GPU_CONTROL_ACTIVE = "/sys/kernel/gpu_control/gpu_control_active";

    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String SWAPPNIESS_FILE = "/proc/sys/vm/swappiness";
    public static final String DYANMIC_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String CMDLINE_ZCACHE = "/system/bootmenu/2nd-boot/cmdline";
    public static final String WRITEBACK = "/sys/devices/virtual/misc/writeback/writeback_enabled";
    public static final String MIN_FREE = "/proc/sys/vm/extra_free_kbytes";


    public static final String PREF_CURRENT_GOV_AVAILABLE = "set_governor";
    public static final String PREF_CPU_MAX_FREQ = "max_frequency";
    public static final String PREF_CPU_MIN_FREQ = "min_frequency";
    public static final String PREF_CPU_COMMANDS = "cpu_commands";

    public static final String PREF_GPU_FREQ_MAX = "gpu_max_freq";
    public static final String PREF_GPU_CONTROL_ACTIVE = "gpu_control_enable";

    public static final String PREF_GOV_IO_FILE = "io_scheduler";
    public static final String PREF_SWAPPINESS_FILE = "swappiness";
    public static final String PREF_DYANMIC_FSYNC = "dynFsync";
    public static final String PREF_CMDLINE_ZCACHE = "zcache";
    public static final String PREF_WRITEBACK = "writeback";
    public static final String PREF_MIN_FREE = "min_free";


    private SharedPreferences prefs;


    private shellHelper shell = new shellHelper();


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    public void onDestroy()
    {
        // service stopped
    }


    @Override
    public void onStart(Intent intent, int startid)
    {
        // service started

    	prefs = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
    	ArrayList<String> al = new ArrayList<String>();

    	// GET CPU VALUES AND COMMANDS FROM PREFERENCES
    	String cpu_max = prefs.getString(PREF_CPU_MAX_FREQ, null);
    	String cpu_min = prefs.getString(PREF_CPU_MIN_FREQ, null);
    	String cpu_gov = prefs.getString(PREF_CURRENT_GOV_AVAILABLE, null);
    	HashSet<String> cpu_cmd = (HashSet<String>) prefs.getStringSet(PREF_CPU_COMMANDS, null);

    	// ADD CPU COMMANDS TO THE ARRAY
    	if (cpu_max != null)
    	{
    		al.add("echo " + cpu_max.substring(0, cpu_max.length()-4) + "000" + " > " + CPU_MAX_FREQ);
    	}
    	if (cpu_min != null)
    	{
    		al.add("echo " + cpu_min.substring(0, cpu_min.length()-4) + "000" + " > " + CPU_MIN_FREQ);
    	}
    	if (cpu_gov != null)
    	{
    		al.add("echo " + cpu_gov + " > " + CURRENT_GOV_AVAILABLE);
    	}
    	if (cpu_cmd != null)
    	{
    		for (String cmd : cpu_cmd) { al.add(cmd); }
    	}

    	// GET GPU VALUES FROM PREFERENCES
    	String gpu_max = prefs.getString(PREF_GPU_FREQ_MAX, null);
    	Boolean gpu_enb = prefs.getBoolean(PREF_GPU_CONTROL_ACTIVE, false);

    	// ADD GPU COMMANDS TO THE ARRAY
    	if (gpu_max != null)
    	{
    		al.add("echo " + gpu_max + " > " + GPU_FREQ_MAX);
    	}
    	al.add("echo " + (gpu_enb ? "1" : "0") + " > " + GPU_FREQ_MAX);

    	// GET MEM VALUES FROM PREFERENCES
    	String mem_ios = prefs.getString(PREF_GOV_IO_FILE, null);
    	String mem_swp = prefs.getString(PREF_SWAPPINESS_FILE, null);
    	Boolean mem_dfs = prefs.getBoolean(PREF_DYANMIC_FSYNC, false);
    	Boolean mem_zch = prefs.getBoolean(PREF_CMDLINE_ZCACHE, false);
        String zcontent = shell.getInfo(CMDLINE_ZCACHE);
    	Boolean mem_wrb = prefs.getBoolean(PREF_WRITEBACK, false);
        String mem_min = prefs.getString(PREF_MIN_FREE, null);

        // ADD MEM COMMANDS TO THE ARRAY
    	if (mem_ios != null)
    	{
    		al.add("echo " + mem_ios + " > " + GOV_IO_FILE);
    	}
    	if (mem_swp != null)
    	{
    		al.add("echo " + mem_swp + " > " + GPU_FREQ_MAX);
    	}
    	al.add("echo " + (mem_dfs ? "1" : "0") + " > " + DYANMIC_FSYNC);
        if (mem_zch && !zcontent.contains("zcache"))
        {
        	al.add("echo " + "\"" + zcontent + " zcache" + "\"" + " > " + CMDLINE_ZCACHE);
        }
        if (!mem_zch && zcontent.contains("zcache"))
        {
        	al.add("echo " + "\"" + zcontent.replace(" zcache", "") + "\"" + " > " + GPU_FREQ_MAX);
        }
    	al.add("echo " + (mem_wrb ? "1" : "0") + " > " + WRITEBACK);
    	if (mem_min != null)
    	{
    		al.add("echo " + mem_min + " > " + MIN_FREE);
    	}

    	// EXECUTE ALL THE COMMANDS COLLECTED
        String[] commands = al.toArray(new String[0]);
    	shell.setRootInfo(commands);
    }
}