package com.aero.control.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Alexander Christ on 12.06.15.
 * Allows the hold various generic stuff for all fragments.
 */
public class PlaceHolderFragment extends PreferenceFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tuneup the layout a bit;
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(10, 0, 10, 0);
            lv.setDivider(null);
            lv.setDividerHeight(10);
            // Set a nice on-touch ripple effect;
            lv.setDrawSelectorOnTop(true);
        }
        return v;
    }

}
