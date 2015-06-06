package com.aero.control.helpers.PerApp.AppMonitor.model;

/**
 * Created by Alexander Christ on 01.06.15.
 * Data model object to store sub-data of AppElement.
 */
public class AppElementDetail {

    private String mTitle;
    private String mContent;

    public AppElementDetail(final String title, final String content) {
        this.mTitle = title;
        this.mContent = content;
    }

    /**
     * Returns the stored title data.
     * @return String (usually the module prefix (e.g. "CPU FREQ"))
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the stored content data.
     * @return String (usually a average value with some strings (e.g.. 1200 MHz))
     */
    public String getContent() {
        return mContent;
    }

}
