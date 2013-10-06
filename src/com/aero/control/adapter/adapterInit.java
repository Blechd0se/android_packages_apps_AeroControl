package com.aero.control.adapter;

/**
 * Created by Alexander Christ on 18.09.13.
 * Adapter Init. Determines which Parameter AeroAdapter has.
 */
public class adapterInit {

    public int icon;
    public String name;
    public String content;

    public adapterInit() {
        super();
    }

    public adapterInit(int icon, String name, String content) {
        super();
        this.icon = icon;
        this.name = name;
        this.content = content;
    }

}
