package com.aero.control.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Alexander Christ on 26.10.13.
 * Helper-Class for the Updater Fragment
 */
public class updateHelpers {

    // Buffer length;
    private static final int BUFF_LEN = 1024;

    /**
     * Method for copying files.
     *
     * @param original  => The original file (+Path)
     * @param copy      => The new file      (+Path)
     * @param timeStamp => (TimeStamp) subfolder of com.aero.control
     */
    public void copyFile(File original, File copy, String timeStamp) throws IOException {

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

        InputStream input = new FileInputStream(original);
        OutputStream output = new FileOutputStream(copy);

        byte[] buffer = new byte[BUFF_LEN];
        int len;
        while ((len = input.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }

        input.close();
        output.close();
    }
}
