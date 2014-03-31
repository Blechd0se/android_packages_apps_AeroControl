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
}
