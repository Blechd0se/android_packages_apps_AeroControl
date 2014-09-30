package com.aero.control.helpers;

import java.io.File;

/**
 * Created by Alexander Christ on 28.09.14.
 */
public class GenericHelper {

    private static final int DEFAULT_DELAY = 200;

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

}
