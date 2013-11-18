package com.aero.control.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class bootReceiver extends BroadcastReceiver 
{
    private SharedPreferences prefs;


    public void onReceive(Context context, Intent intent) 
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	Boolean setOnBoot = prefs.getBoolean("checkbox_preference", false);
        if (setOnBoot)
        {
        	Intent i = new Intent(context, bootService.class);
        	context.startService(i);
        }
    }
}