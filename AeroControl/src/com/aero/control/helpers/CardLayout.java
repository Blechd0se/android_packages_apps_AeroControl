package com.aero.control.helpers;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;

/**
 * Created by Alexander Christ on 11.02.14.
 */
public class CardLayout extends LinearLayout implements OnGlobalLayoutListener {

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
        return;
    }

}