package com.aero.control.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by Alexander Christ on 26.10.13.
 * Helper-Class for the Updater Fragment
 */
public class updateHelper {

    public static final String timeStamp = new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime());
    private static final String LOG_TAG = updateHelper.class.getName();
    private static final String[][] WHITE_LIST_DEVICES = {
            {"Nexus 4", FilePath.BACKUPPATH[0]},
            {"Nexus 5", FilePath.BACKUPPATH[0]},
            {"ASUS_T00N", FilePath.BACKUPPATH[0]},
            {"XT1032", FilePath.BACKUPPATH[0]},
            {"XT1033", FilePath.BACKUPPATH[0]},
            {"Nexus 7", FilePath.BACKUPPATH[1]}
    };

    /**
     * Method for copying files.
     *
     * @param original  => The original file (+Path)
     * @param copy      => The new file      (+Path)
     */
    public final void copyFile(File original, File copy, boolean rest) throws IOException {

        FileChannel input = null;
        FileChannel output = null;

        if (!rest) {
            try {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.e(LOG_TAG, "No Sdcard found!");
                    return;

                } else {
                    File file = new File(Environment.getExternalStorageDirectory()
                            +File.separator
                            +"com.aero.control" //folder name
                            +File.separator
                            +timeStamp); //file name
                    file.mkdirs();
                }

                input = new FileInputStream(original).getChannel();
                output = new FileOutputStream(copy).getChannel();
                output.transferFrom(input, 0, input.size());

            } finally {
                // Handle error case on different devices;
                if(input == null || output == null) {
                    Log.e(LOG_TAG, "Could not copy files or something went wrong.");
                } else {
                    input.close();
                    output.close();
                }
        }
        } else {
            input = new FileInputStream(original).getChannel();
            output = new FileOutputStream(copy).getChannel();
            output.transferFrom(input, 0, input.size());

            input.close();
            output.close();
        }
    }

    /**
     * Returns the backup path if this model is in the white list
     *
     * @param model String, contains the model to check
     * @return String Backuppath or NULL if not found
     */
    public String isWhiteListed(String model) {

        for (String[] s : WHITE_LIST_DEVICES) {
            if (s[0].equals(model)) {
                // We found a match!
                return s[1];
            }
        }

        return null;
    }
}
