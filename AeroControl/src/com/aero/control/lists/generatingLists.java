package com.aero.control.lists;

import android.os.Build;

import com.aero.control.R;

import java.util.ArrayList;

/**
 * Created by Alexander Christ on 17.09.13.
 * Adds items to the Navigation Drawer (with icons)
 */
public class generatingLists {

    public ArrayList<PreferenceItem> ITEMS = new ArrayList<PreferenceItem>();

    public generatingLists() {
        listItems();
    }

    public void listItems() {
        addItem(new PreferenceItem(R.string.slider_overview, R.drawable.overview));
        addItem(new PreferenceItem(R.string.slider_cpu_settings, R.drawable.cpu));
        addItem(new PreferenceItem(R.string.slider_statistics, R.drawable.clock));
        addItem(new PreferenceItem(R.string.slider_gpu_settings, R.drawable.gpu));
        addItem(new PreferenceItem(R.string.slider_memory_settings, R.drawable.memory));

        if (Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526"))
            addItem(new PreferenceItem(R.string.slider_defy_parts, R.drawable.gear));
        else
            addItem(new PreferenceItem(R.string.slider_misc_settings, R.drawable.gear));

        addItem(new PreferenceItem(R.string.slider_updater, R.drawable.update));
        addItem(new PreferenceItem(R.string.slider_profile, R.drawable.profile));
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

