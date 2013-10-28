package com.aero.control.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


/**
 * Created by Alexander Christ on 26.10.13.
 * Helper-Class for the Updater Fragment
 */
public class updateHelpers {

    /**
     * Method for copying files.
     *
     * @param original  => The original file (+Path)
     * @param copy      => The new file      (+Path)
     * @param timeStamp => (TimeStamp) subfolder of com.aero.control
     */
    public void copyFile(File original, File copy, String timeStamp) throws IOException {

        FileChannel input = null;
        FileChannel output = null;

        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                Log.e("Aero", "No Sdcard found!");
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
            input.close();
            output.close();
        }
    }
}
