package com.aero.control.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Alexander Christ on 30.09.13.
 */
public class CustomTextPreference extends EditTextPreference {

    private Context context;
    private int style;
    public static final Typeface kitkatFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    public static final int STYLE_KITKAT = 10;
    public static final int STYLE_NORMAL = 20;

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

    private void setStyleKitkat(View view) {
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        titleView.setTypeface(kitkatFont);
        summaryView.setTypeface(kitkatFont);
    }

    private void setStyleNormal(View view) {
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        titleView.setTypeface(Typeface.DEFAULT);
        summaryView.setTypeface(Typeface.DEFAULT);
    }

    /*
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        switch (style) {
            case STYLE_KITKAT:
                setStyleKitkat(view);
                break;
            case STYLE_NORMAL:
                setStyleNormal(view);
                break;
            default:
                break;
        }

    } */

    public void setStyle(int style) {
        switch (style) {
            case STYLE_KITKAT:
                this.style = STYLE_KITKAT;
                break;
            case STYLE_NORMAL:
                this.style = STYLE_NORMAL;
                break;
            default:
                this.style = STYLE_NORMAL;
        }
    }

}
