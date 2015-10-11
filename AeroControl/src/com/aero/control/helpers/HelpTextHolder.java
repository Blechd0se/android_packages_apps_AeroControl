package com.aero.control.helpers;

import android.content.Context;

import com.aero.control.R;

import java.util.HashMap;

/**
 * Created by Alexander Christ on 18.08.15.
 */
public class HelpTextHolder {

    private static HelpTextHolder mHelpTextHolder;
    private HashMap<String, String> mDataVault;
    private Context mContext;

    private HelpTextHolder(Context context) {
        this.mDataVault = new HashMap<String, String>();
        this.mContext = context;
        loadData();
    }

    public static synchronized HelpTextHolder instance(Context context) {

        if (mHelpTextHolder == null) {
            mHelpTextHolder = new HelpTextHolder(context);
        }
        return mHelpTextHolder;
    }

    private void putInMap(String key, int value) {

        // If the key does already exits, we friendly inform the developer about it;
        if (mDataVault.containsKey(key)) {
            throw new RuntimeException("This key " + key + " does already exits in our map. Did you choose the right one?");
        }

        mDataVault.put(key, mContext.getResources().getString(value));

    }

    private void loadData() {

        // CPU Fragment helptexts;
        putInMap("max_frequency", R.string.helptext_max_freq_cpu);
        putInMap("min_frequency", R.string.helptext_min_freq_cpu);
        putInMap("hotplug_control", R.string.helptext_hotplug_control);
        putInMap("voltage_values", R.string.helptext_voltage_values);
        putInMap("set_governor", R.string.helptext_set_governor);
        putInMap("cpu_commands", R.string.helptext_live_oc_uc);

        // GPU Fragment helptexts;
        putInMap("gpu_max_freq", R.string.helptext_gpu_max_freq);
        putInMap("rgbValues", R.string.helptext_rgbValues);
        putInMap("set_gpu_governor", R.string.helptext_set_gpu_governor);
        putInMap("gpu_gov_settings", R.string.helptext_gpu_gov_settings);

        // Memory Fragment helptexts;
        putInMap("read_ahead", R.string.helptext_read_ahead);
        putInMap("fsync", R.string.helptext_fsync);
        putInMap("entropy_settings", R.string.helptext_entropy_settings);
        putInMap("fstrim_toggle", R.string.helptext_fstrim_toggle);
        putInMap("dalvik_settings", R.string.helptext_dalvik_settings);
        putInMap("io_scheduler_list", R.string.helptext_io_scheduler_list);
        putInMap("ksm", R.string.helptext_ksm);

        // MemoryDalvik Fragment helptexts;
        putInMap("block_dump", R.string.helptext_block_dump);
        putInMap("dirty_background_bytes", R.string.helptext_dirty_background_bytes);
        putInMap("dirty_background_ratio", R.string.helptext_dirty_background_ratio);
        putInMap("dirty_bytes", R.string.helptext_dirty_bytes);
        putInMap("dirty_expire_centisecs", R.string.helptext_dirty_expire_centisecs);
        putInMap("dirty_ratio", R.string.helptext_dirty_ratio);
        putInMap("dirty_writeback_centisecs", R.string.helptext_dirty_writeback_centisecs);
        putInMap("drop_caches", R.string.helptext_drop_caches);
        putInMap("extfrag_threshold", R.string.helptext_extfrag_threshold);
        putInMap("extra_free_kbytes", R.string.helptext_extra_free_kbytes);
        putInMap("highmem_is_dirtyable", R.string.helptext_highmem_is_dirtyable);
        putInMap("laptop_mode", R.string.helptext_laptop_mode);
        putInMap("legacy_va_layout", R.string.helptext_legacy_va_layout);
        putInMap("lowmem_reserve_ratio", R.string.helptext_lowmem_reserve_ratio);
        putInMap("max_map_count", R.string.helptext_max_map_count);
        putInMap("min_free_kbytes", R.string.helptext_min_free_kbytes);
        putInMap("oom_dump_tasks", R.string.helptext_oom_dump_tasks);
        putInMap("oom_kill_allocating_task", R.string.helptext_oom_kill_allocating_task);
        putInMap("overcommit_memory", R.string.helptext_overcommit_memory);
        putInMap("overcommit_ratio", R.string.helptext_overcommit_ratio);
        putInMap("panic_on_oom", R.string.helptext_panic_on_oom);
        putInMap("percpu_pagelist_fraction", R.string.helptext_percpu_pagelist_fraction);
        putInMap("stat_interval", R.string.helptext_stat_interval);
        putInMap("swappiness", R.string.helptext_swappiness);
        putInMap("vfs_cache_pressure", R.string.helptext_vfs_cache_pressure);

        // Misc Fragment helptexts;
        putInMap("vtg_level", R.string.helptext_vtg_level);
        putInMap("tcp_congestion", R.string.helptext_tcp_congestion);
        putInMap("temp_threshold", R.string.helptext_temp_threshold);
        putInMap("volume_boost", R.string.helptext_volume_boost);
        putInMap("amp", R.string.helptext_amp);

        // CPU Hotplug Fragment helptexts;
        putInMap("all_cpus_threshold", R.string.helptext_all_cpus_threshold);
        putInMap("battery_saver", R.string.helptext_battery_saver);
        putInMap("debug", R.string.helptext_debug);
        putInMap("hotplug_sampling", R.string.helptext_hotplug_sampling);
        putInMap("low_latency", R.string.helptext_low_latency);
        putInMap("min_online_time", R.string.helptext_min_online_time);
        putInMap("single_core_threshold", R.string.helptext_single_core_threshold);
        putInMap("up_frequency", R.string.helptext_up_frequency);

        // CPU Fragment interactive tunables helptexts;
        putInMap("above_hispeed_delay", R.string.helptext_above_hispeed_delay);
        putInMap("align_windows", R.string.helptext_align_windows);
        putInMap("boostpulse_duration", R.string.helptext_boostpulse_duration);
        putInMap("go_hispeed_load", R.string.helptext_go_hispeed_load);
        putInMap("hispeed_freq", R.string.helptext_hispeed_freq);
        putInMap("input_boost_freq", R.string.helptext_input_boost_freq);
        putInMap("io_is_busy", R.string.helptext_io_is_busy);
        putInMap("max_freq_hysteresis", R.string.helptext_max_freq_hysteresis);
        putInMap("min_sample_time", R.string.helptext_min_sample_time);
        putInMap("target_loads", R.string.helptext_target_loads);
        putInMap("timer_rate", R.string.helptext_timer_rate);
        putInMap("timer_slack", R.string.helptext_timer_slack);

        // CPU Fragment ondemand tunables helptexts;
        putInMap("sampling_rate", R.string.helptext_sampling_rate);
        putInMap("up_threshold", R.string.helptext_up_threshold);
        putInMap("ignore_nice_load", R.string.helptext_ignore_nice_load);
        putInMap("sampling_down_factor", R.string.helptext_sampling_down_factor);
        putInMap("powersave_bias", R.string.helptext_powersave_bias);

    }

    public String getText(String key) {

        if (mDataVault.containsKey(key))
            return mDataVault.get(key);

        return mContext.getResources().getString(R.string.helptext_no_data_found);
    }

}
