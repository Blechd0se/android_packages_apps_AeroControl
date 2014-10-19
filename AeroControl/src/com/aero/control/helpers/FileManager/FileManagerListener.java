package com.aero.control.helpers.FileManager;

import java.io.File;

/**
 * Created by Alexander Christ on 21.09.14.
 */
public interface FileManagerListener {

    /*
     * The clicked file in the file manager is not readable
     */
    void OnCannotFileRead(File file);

    /*
     * The file is readable and we clicked on it
     */
    void OnFileClicked(File file);

}