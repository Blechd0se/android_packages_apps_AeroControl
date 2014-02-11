package com.aero.control.helpers;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 11.02.14.
 */
public class CardLayout extends LinearLayout implements OnGlobalLayoutListener {

    private int mVisible = 0;

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
        getViewTreeObserver().removeGlobalOnLayoutListener(this);

        final int heightPx = getContext().getResources().getDisplayMetrics().heightPixels;

        boolean inversed = false;
        final int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            int[] location = new int[2];

            child.getLocationOnScreen(location);

            if (location[1] > heightPx) {
                break;
            }

            // Just turn off fancy animations for the moment;
            if (mVisible == 0) {
                if (!inversed) {
                    child.startAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.slide_up_left));
                            mVisible = 1;
                } else {
                    child.startAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.slide_up_right));
                            //mVisible = 0;
                }
            }

            inversed = !inversed;
        }

    }

}