package com.aero.control.helpers;

import android.graphics.Typeface;
import android.os.Environment;

/**
 * Created by Alexander Christ on 05.08.14.
 *
 * Contains most file paths for the individual fragments
 *
 */
public final class FilePath {

    private FilePath() {}

    /* Used by; AeroFragment, GPUFragment, MemoryFragment */
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV_IO_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String[] GPU_FILES = {"/sys/kernel/gpu_control/max_freq", /* Defy 2.6 Kernel */
                                                "/sys/class/kgsl/kgsl-3d0/max_gpuclk", /* Adreno GPUs */
                                                "/sys/devices/platform/omap/pvrsrvkm.0/sgx_fck_rate" /* Defy 3.0 Kernel */};
    public static final String[] GPU_FILES_RATE = {"/sys/class/kgsl/kgsl-3d0/gpuclk", /* Adreno GPUs */
                                                "/sys/devices/platform/omap/pvrsrvkm.0/sgx_fck_rate" /* Defy 3.0 Kernel */,
                                                "/proc/gpu/cur_rate" /* Defy 2.6 Kernel */};


    public static final String FILENAME_PROC_MEMINFO = "/proc/meminfo";


    /* Used by; CPUFragment */
    public static final String CPU_BASE_PATH = "/sys/devices/system/cpu/cpu";
    public static final String CPU_AVAILABLE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String ALL_GOV_AVAILABLE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String CURRENT_GOV_AVAILABLE = "/cpufreq/scaling_governor";
    public static final String CPU_MAX_FREQ = "/cpufreq/scaling_max_freq";
    public static final String CPU_MIN_FREQ = "/cpufreq/scaling_min_freq";
    public static final String CPU_GOV_BASE = "/sys/devices/system/cpu/cpufreq/";
    public static final String CPU_VSEL = "/proc/overclock/mpu_opps";
    public static final String CPU_VSEL_MAX = "/proc/overclock/max_vsel";
    public static final String CPU_MAX_RATE = "/proc/overclock/max_rate";
    public static final String CPU_FREQ_TABLE = "/proc/overclock/freq_table";

    /* Used by; HotplugFragment */
    public static final String[] HOTPLUG_PATH = { "/sys/kernel/hotplug_control",
                                                  "/sys/class/misc/mako_hotplug_control"};

    /* Used by; DefyPartsFragment */
    public static final String PROP_CHARGE_LED_MODE = "persist.sys.charge_led";
    public static final String PROP_TOUCH_POINTS = "persist.sys.multitouch";
    public static final String PROP_BUTTON_BRIGHTNESS = "persist.sys.button_brightness";

    /* Used by; GPUFragment */
    public static final String GPU_CONTROL_ACTIVE = "/sys/kernel/gpu_control/gpu_control_active";
    public static final String DISPLAY_COLOR ="/sys/class/misc/mDisplayControl/display_brightness_value";
    public static final String GPU_FREQ_NEXUS4_VALUES = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
    public static final String GPU_GOV_BASE = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/devfreq/";
    public static final String SWEEP2WAKE = "/sys/android_touch/sweep2wake";
    public static final String DOUBLETAP2WAKE = "/sys/android_touch/doubletap2wake";
    public static final String COLOR_CONTROL = "/sys/devices/platform/kcal_ctrl.0/kcal";
    public static final String COLOR_CONTROL_BIT = "/sys/devices/platform/kcal_ctrl.0/kcal_ctrl";

    /* Used by; GPUGovernorFragment */
    public static final String GPU_GOV_PATH = "/sys/module/msm_kgsl_core/parameters";

    /* Used by; MemoryDalvikFragment */
    public static final String DALVIK_TWEAK = "/proc/sys/vm";

    /* Used by; MemoryFragment */
    public static final String FSYNC = "/sys/module/sync/parameters/fsync_enabled";
    public static final String DYANMIC_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String CMDLINE_ZACHE = "/system/bootstrap/2nd-boot/cmdline";
    public static final String WRITEBACK = "/sys/devices/virtual/misc/writeback/writeback_enabled";
    public static final String LOW_MEM = "/system/build.prop";
    public static final String FILENAME = "firstrun_trim";
    public static final String GOV_IO_PARAMETER = "/sys/block/mmcblk0/queue/iosched";
    public static final String READAHEAD_PARAMETER = "/sys/block/mmcblk0/queue/read_ahead_kb";
    public static final String KSM_SETTINGS = "/sys/kernel/mm/ksm/run";

    /* Used by; MiscSettingsFragment */
    public static final String MISC_VIBRATOR_CONTROL = "/sys/devices/virtual/timed_output/vibrator";
    public static final String MISC_THERMAL_CONTROL = "/sys/module/msm_thermal/parameters";
    public static final String MISC_VIBRATOR_CONTROL_FILE = "/sys/devices/virtual/timed_output/vibrator/vtg_level";
    public static final String MISC_VIBRATOR_CONTROL_FILEAMP = "/sys/devices/virtual/timed_output/vibrator/amp";
    public static final String MISC_THERMAL_CONTROL_FILE = "/sys/module/msm_thermal/parameters/temp_threshold";
    public static final String MISC_TCP_CONGESTION_AVAILABLE = "/proc/sys/net/ipv4/tcp_available_congestion_control";
    public static final String MISC_TCP_CONGESTION_CURRENT = "/proc/sys/net/ipv4/tcp_congestion_control";
    public static final String MISC_HEADSET_VOLUME_BOOST = "/sys/class/misc/soundcontrol";
    public static final String MISC_HEADSET_VOLUME_BOOST_FILE = "/sys/class/misc/soundcontrol/volume_boost";

    /* Used by; ProfileFragment */
    public static final String sharedPrefsPath = "/data/data/com.aero.control/shared_prefs/";
    public static final String EXTERNAL_PATH = Environment.getExternalStorageDirectory().getPath();

    /* Used by; StatisticsFragment */
    public static final String[] color_code = {
            "#009688", /* Turquoise */
            "#ff5722", /* Orange */
            "#8bc34a", /* Midnight Blue */
            "#03a9f4", /* Nephritis */
            "#e51c23", /* Monza */
            "#00bcd4", /* Wisteria */
            "#607d8b", /* Peter River */
            "#e91e63", /* Pomegrante */
    };
    public static final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    public static final String OFFSET_STAT = "/data/data/com.aero.control/files/offset_stat";
    public static final Typeface kitkatFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    /* Used by; UpdaterFragment */
    public static final String zImage = "/system/bootstrap/2nd-boot/zImage";
    public static final String[] BACKUPPATH = { "/dev/block/platform/msm_sdcc.1/by-name/boot", // MSM devices
                                                "/dev/block/platform/sdhci-tegra.3/by-name/LNX", // Tegra devices
                                                "/dev/block/platform/omap/omap_hsmmc.0/by-name/boot" // OMAP devices
    };

    /* Used by; VoltageFrament */
    public static final String VOLTAGE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
}
