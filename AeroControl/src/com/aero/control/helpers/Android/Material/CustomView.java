package com.aero.control.helpers.Android.Material;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

public abstract class CustomView extends RelativeLayout{

    protected final static String MATERIALDESIGNXML = "http://schemas.android.com/apk/res-auto";
    protected final static String ANDROIDXML = "http://schemas.android.com/apk/res/android";

    final int disabledBackgroundColor = Color.parseColor("#E2E2E2");

    protected int minWidth;
    protected int minHeight;

    protected int backgroundColor;
    protected int beforeBackground;
    protected int backgroundResId = -1;

    // Indicate if user touched this view the last time
    public boolean isLastTouch = false;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitDefaultValues();
    }

    protected abstract void onInitDefaultValues();


    public static int dpToPx(float dp, Resources resources){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static float dipOrDpToFloat(String value) {
        if (value.contains("dp")) {
            value = value.replace("dp", "");
        }
        else {
            value = value.replace("dip", "");
        }
        return Float.parseFloat(value);
    }

    public static int getRelativeTop(View myView) {
        Rect bounds = new Rect();
        myView.getGlobalVisibleRect(bounds);
        return bounds.top;
    }

    public static int getRelativeLeft(View myView) {
        if(myView.getId() == android.R.id.content)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    // Set atributtes of XML to View
    protected void setAttributes(AttributeSet attrs) {
        setMinimumHeight(dpToPx(minHeight, getResources()));
        setMinimumWidth(dpToPx(minWidth, getResources()));
        if (backgroundResId != -1 && !isInEditMode()) {
            setBackgroundResource(backgroundResId);
        }
        setBackgroundAttributes(attrs);
    }

    protected void setBackgroundAttributes(AttributeSet attrs) {
        int backgroundColor = attrs.getAttributeResourceValue(ANDROIDXML,"background",-1);
        if(backgroundColor != -1){
            setBackgroundColor(getResources().getColor(backgroundColor));
        }else{
            // Color by hexadecimal
            int background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
            if(background != -1 && !isInEditMode()) {
                setBackgroundColor(background);
            }else {
                setBackgroundColor(backgroundColor);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled)
            setBackgroundColor(beforeBackground);
        else
            setBackgroundColor(disabledBackgroundColor);
    }
}