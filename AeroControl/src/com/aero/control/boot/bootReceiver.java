package com.aero.control.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aero.control.helpers.shellHelper;


public class bootReceiver extends BroadcastReceiver 
{
    public static final String LAST_KMSG = "/proc/last_kmsg";
    private SharedPreferences prefs;
    private shellHelper shell = new shellHelper();


    public void onReceive(Context context, Intent intent) 
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	Boolean setOnBoot = prefs.getBoolean("checkbox_preference", false);
        if (setOnBoot) {
        	Intent i = new Intent(context, bootService.class);
        	context.startService(i);
        }

        // Kernel panic receiver:
        if (!shell.getRootInfo("cat", LAST_KMSG).equals("Unavailable")) {
            Intent trIntent = new Intent("android.intent.action.BOOT");
            trIntent.setClass(context, com.aero.control.boot.RebootActivity.class);
            trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(trIntent);
        }
    }
}