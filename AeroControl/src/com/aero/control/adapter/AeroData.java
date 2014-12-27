package com.aero.control.adapter;

import android.graphics.drawable.Drawable;

/**
 * Created by Alexander Christ on 18.09.13.
 * Used on various places and defines the
 * aero data objects.
 */
public class AeroData {

    public String name;
    public String right_name;
    public String content;
    public int file;
    public Drawable image;
    public boolean isChecked = false;

    public AeroData(String name, String content, String right_name) {
        super();
        this.name = name;
        this.content = content;
        this.right_name = right_name;
    }

    public AeroData(int file, String content){
        super();
        this.file = file;
        this.content = content;
    }

    public AeroData(Drawable image, String name){
        super();
        this.image = image;
        this.name = name;
    }
}
