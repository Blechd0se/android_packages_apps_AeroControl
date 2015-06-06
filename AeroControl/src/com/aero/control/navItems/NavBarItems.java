package com.aero.control.navItems;

import android.content.Context;
import android.os.Build;

import com.aero.control.AeroActivity;
import com.aero.control.R;

import java.util.ArrayList;

/**
 * Created by Alexander Christ on 17.09.13.
 * Adds items to the Navigation Drawer (with icons)
 */
public class NavBarItems {

    public ArrayList<PreferenceItem> ITEMS = new ArrayList<PreferenceItem>();
    private Context mContext;

    public NavBarItems(Context context) {
        this.mContext = context;
        listItems();
    }

    public void listItems() {
        addItem(new PreferenceItem(R.string.slider_overview, R.drawable.overview));
        addItem(new PreferenceItem(R.string.slider_cpu_settings, R.drawable.cpu));
        addItem(new PreferenceItem(R.string.slider_statistics, R.drawable.clock));
        addItem(new PreferenceItem(R.string.slider_gpu_settings, R.drawable.gpu));
        addItem(new PreferenceItem(R.string.slider_memory_settings, R.drawable.memory));
        addItem(new PreferenceItem(R.string.slider_misc_settings, R.drawable.mixer));

        if (Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526"))
            addItem(new PreferenceItem(R.string.slider_defy_parts, R.drawable.gear));


        addItem(new PreferenceItem(R.string.slider_backup_restore, R.drawable.update));
        addItem(new PreferenceItem(R.string.slider_profile, R.drawable.profile));
        addItem(new PreferenceItem(R.string.slider_app_monitor, R.drawable.appmonitor));

        // Set up our file;
        int output = 0;

        if (AeroActivity.genHelper.doesExist(mContext.getFilesDir().getAbsolutePath() + "/" + "testsuite")) {
            output = 1;
        }

        if (output > 0)
            addItem(new PreferenceItem(R.string.slider_testsuite_settings, R.drawable.dashboard));
    }

    public static class PreferenceItem {
        public int content;
        public int drawable;

        public PreferenceItem(int content, int drawable) {
            this.content = content;
            this.drawable = drawable;
        }
    }

    private void addItem(PreferenceItem item) {
        ITEMS.add(item);
    }
}

