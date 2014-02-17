package com.aero.control.helpers;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class CustomTextPreference extends EditTextPreference {

    private Context context;

    public CustomTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setContext(context);
    }

    public CustomTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setContext(context);
    }

    public CustomTextPreference(Context context) {
        super(context);
        this.setContext(context);
    }

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
