package com.aero.control.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Alexander Christ on 28.09.14.
 */
public class GenericHelper {

    private static final int DEFAULT_DELAY = 200;
    private static final int BYTE = 1024;

    public GenericHelper() { }

    public final int getDefaultDelay() {
        return DEFAULT_DELAY;
    }

    public final boolean doesExist(String s) {
        return new File(s).exists();
    }

    public final File getNewFile(String s) {
        return new File(s);
    }

    /**
     * Copies one file another destination
     *
     * @param source      Source file + complete path
     * @param destination Destination file + complete path
     *
     * @throws java.io.IOException
     */
    public void copyFile(File source, File destination) throws IOException {

        InputStream input = new FileInputStream(source);
        OutputStream output = new FileOutputStream(destination);

        // Copy the input;
        byte[] buf = new byte[BYTE];
        int len;
        while ((len = input.read(buf)) > 0) {
            output.write(buf, 0, len);
        }
        input.close();
        output.close();
    }

}
