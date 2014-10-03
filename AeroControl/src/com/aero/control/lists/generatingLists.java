package com.aero.control.lists;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.aero.control.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Alexander Christ on 17.09.13.
 * Adds items to the Navigation Drawer (with icons)
 */
public class generatingLists {

    public ArrayList<PreferenceItem> ITEMS = new ArrayList<PreferenceItem>();
    private Context mContext;

    public generatingLists(Context context) {
        this.mContext = context;
        listItems();
    }

    public void listItems() {
        addItem(new PreferenceItem(R.string.slider_overview, R.drawable.overview));
        addItem(new PreferenceItem(R.string.slider_cpu_settings, R.drawable.cpu));
        addItem(new PreferenceItem(R.string.slider_statistics, R.drawable.clock));
        addItem(new PreferenceItem(R.string.slider_gpu_settings, R.drawable.gpu));
        addItem(new PreferenceItem(R.string.slider_memory_settings, R.drawable.memory));

        if (Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526"))
            addItem(new PreferenceItem(R.string.slider_defy_parts, R.drawable.mixer));
        else
            addItem(new PreferenceItem(R.string.slider_misc_settings, R.drawable.mixer));

        addItem(new PreferenceItem(R.string.slider_backup_restore, R.drawable.update));
        addItem(new PreferenceItem(R.string.slider_profile, R.drawable.profile));

        int output = 0;
        final byte[] buffer = new byte[1024];

        try {
            FileInputStream fis = mContext.openFileInput("testsuite");
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
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

