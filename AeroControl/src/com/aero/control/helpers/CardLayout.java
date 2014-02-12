package com.aero.control.helpers;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.aero.control.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alexander Christ on 11.02.14.
 */
public class CardLayout extends LinearLayout implements OnGlobalLayoutListener {

    private String FILENAME = "cardanimation";

    public CardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayoutObserver();

    }

    public CardLayout(Context context) {
        super(context);
        initLayoutObserver();
    }

    private void initLayoutObserver() {
        setOrientation(LinearLayout.VERTICAL);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {

        final int heightPx = getContext().getResources().getDisplayMetrics().heightPixels;
        final int childCount = getChildCount();
        int output = 0;
        final byte[] buffer = new byte[1024];

        try {
            final FileInputStream fis = getContext().openFileInput(FILENAME);
            output = fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            Log.e("Aero", "Couldn't open File... " + output);
        }

        if (output != 0)
            return;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            int[] location = new int[2];

            child.getLocationOnScreen(location);


            if (location[1] > heightPx) {
                break;
            }

            child.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.slide_up_left));
            disableAnimation();
        }

    }

    public void disableAnimation() {

        try {
            final FileOutputStream fos = getContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write("1".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

    }
    public void enableAnimation() {

        try {
            final FileOutputStream fos = getContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write("0".getBytes());
            fos.close();
        }
        catch (IOException e) {
            Log.e("Aero", "Could not save file. ", e);
        }

    }

}