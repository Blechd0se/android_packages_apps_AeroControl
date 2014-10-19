package com.aero.control.helpers.PerApp;

/**
 * Created by Alexander Christ on 05.10.14.
 */
public interface PerAppListener {

    /*
     * Position and state of the clicked app;
     */
    void OnAppItemClicked(int position, boolean isChecked);

}