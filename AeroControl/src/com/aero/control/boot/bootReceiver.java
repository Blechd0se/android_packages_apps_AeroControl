package com.aero.control.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aero.control.service.PerAppServiceHelper;

import java.io.File;


public class bootReceiver extends BroadcastReceiver 
{
    public static final String LAST_KMSG = "/proc/last_kmsg";
    private SharedPreferences prefs;
    public static PerAppServiceHelper perAppService;


    public void onReceive(Context context, Intent intent) 
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	Boolean setOnBoot = prefs.getBoolean("checkbox_preference", false);
        if (setOnBoot) {
        	Intent i = new Intent(context, bootService.class);
        	context.startService(i);
        }

        File last_kmsg = new File (LAST_KMSG);
        Boolean rebootChecker = prefs.getBoolean("reboot_checker", false);

        // Kernel panic receiver:
        if (rebootChecker) {
            if (last_kmsg.exists()) {
                Intent trIntent = new Intent("android.intent.action.BOOT");
                trIntent.setClass(context, com.aero.control.boot.RebootActivity.class);
                trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(trIntent);
            }
        }

        // Start our service on boot-up;
        perAppService = new PerAppServiceHelper(context);
        if (perAppService.shouldBeStarted())
            perAppService.startService();

    }
}