package com.aero.control.adapter;

/**
 * Created by Alexander Christ on 18.09.13.
 * Adapter Init. Determines which Parameter AeroAdapter has.
 */
public class adapterInit {

    public String name;
    public String content;
    public int file;

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

}
