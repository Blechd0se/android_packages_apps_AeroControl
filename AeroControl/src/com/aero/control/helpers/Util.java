package com.aero.control.helpers;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import com.aero.control.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Christ on 25.05.15.
 *
 * Utility methods
 */
public class Util {

    /**
     * Returns a random CharSeqeuence
     * @param context Context, the current Context
     * @return CharSeqeuence
     */
    public static CharSequence getRandomLoadingText(Context context) {

        ArrayList<Integer> randomData = new ArrayList<Integer>();

        randomData.add(R.string.random_programming_flux);
        randomData.add(R.string.random_nsa_loading);
        randomData.add(R.string.random_data_somewhere);
        randomData.add(R.string.random_shovelling_coal);
        randomData.add(R.string.random_testing_patience);
        randomData.add(R.string.random_prepare_awesomeness);
        randomData.add(R.string.random_working_you_know);

        return context.getText(randomData.get(new Random().nextInt(randomData.size())));
    }

    /**
     * Shows different output depending on the passed value.
     * @param milliseconds long, Time in milliseconds.
     * @return String
     */
    public static String getFormatedTimeString(long milliseconds) {
        if (milliseconds < 60000) {
            return String.format("%02d secs",
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        } else if (milliseconds < 3600000) {
            return String.format("%02d min %02d secs",
                    TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        } else {
            return String.format("%02d h %02d min %02d secs",
                    TimeUnit.MILLISECONDS.toHours(milliseconds),
                    TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        }
    }

    /**
     * Returns the last file from a given path.
     * @param syspath The system path (e.g. /sys/path/to/file)
     * @return String (for example "file")
     */
    public static String getLastSysValue(String syspath) {

        if (syspath == null)
            return null;

        String[] values = syspath.split("/");

        return values[values.length - 1];
    }

    /**
     * Returns the current usage stats list, if the user enable the option in the settings menu.
     * @param context Context, the current context
     * @return List<UsageStats>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<UsageStats> getUsageStatsList(Context context){
        UsageStatsManager usm = getUsageStatsManager(context);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        return usageStatsList;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static UsageStatsManager getUsageStatsManager(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        return usm;
    }

}
