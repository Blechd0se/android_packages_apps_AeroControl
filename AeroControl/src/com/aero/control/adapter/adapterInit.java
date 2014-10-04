package com.aero.control.adapter;

import android.graphics.drawable.Drawable;

/**
 * Created by Alexander Christ on 18.09.13.
 * Adapter Init. Determines which Parameter AeroAdapter has.
 */
public class adapterInit {

    public String name;
    public String content;
    public int file;
    public Drawable image;
    public boolean isChecked = false;

    public adapterInit(String name, String content) {
        super();
        this.name = name;
        this.content = content;
    }

    public adapterInit(int file, String content){
        super();
        this.file = file;
        this.content = content;
    }

    public adapterInit(Drawable image, String name){
        super();
        this.image = image;
        this.name = name;
    }
}
