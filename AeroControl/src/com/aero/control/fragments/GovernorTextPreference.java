package com.aero.control.fragments;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class GovernorTextPreference extends EditTextPreference {

    private Context context;

    public GovernorTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public GovernorTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public GovernorTextPreference(Context context) {
        super(context);
        this.context = context;
    }

}
