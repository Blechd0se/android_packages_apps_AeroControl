package com.aero.control.helpers.Android.Material;

import com.aero.control.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class CheckBox extends CustomView {

    private int backgroundColor = Color.parseColor("#4CAF50");
    // Indicate step in check animation
    private int step = 0;

    private Check checkView;

    private boolean press = false;
    private boolean check = false;

    private Paint mPaint;

    private OnCheckListener onCheckListener;

    public CheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    @Override
    protected void onInitDefaultValues() {
        minWidth = 48;
        minHeight = 48;
        backgroundColor = Color.parseColor("#4CAF50");// default color
        backgroundResId = R.drawable.background_checkbox;
    }

    // Set atributtes of XML to View
    protected void setAttributes(AttributeSet attrs) {

        setBackgroundResource(R.drawable.background_checkbox);

        // Set size of view
        setMinimumHeight(dpToPx(48, getResources()));
        setMinimumWidth(dpToPx(48, getResources()));

        // Set background Color
        // Color by resource
        int backgroundColor = attrs.getAttributeResourceValue(ANDROIDXML,
                "background", -1);
        if (backgroundColor != -1) {
            setBackgroundColor(getResources().getColor(backgroundColor));
        } else {
            // Color by hexadecimal
            int background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
            if (background != -1)
                setBackgroundColor(background);
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#446D6D6D"));

        checkView = new Check(getContext());
        RelativeLayout.LayoutParams params = new LayoutParams(dpToPx(20,
                getResources()), dpToPx(20, getResources()));
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        checkView.setLayoutParams(params);
        addView(checkView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            isLastTouch = true;
            final int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    changeBackgroundColor((check) ? makePressColor() : Color
                            .parseColor("#446D6D6D"));
                    break;
                case MotionEvent.ACTION_UP:
                    changeBackgroundColor(getResources().getColor(
                            android.R.color.transparent));
                    press = false;

                    if ((event.getX() <= getWidth() && event.getX() >= 0)
                            && (event.getY() <= getHeight() && event.getY() >= 0)) {
                        isLastTouch = false;
                        check = !check;
                        if (onCheckListener != null)
                            onCheckListener.onCheck(check);
                        if (check) {
                            step = 0;
                        }
                        if (check)
                            checkView.changeBackground();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    /*
                     * We have to use the cancel-event here since ACTION_MOVE would need
                     * some calculations to find out if we are outside of our view. Otherwise
                     * we would have a grey circle around our view until touched again.
                     * Let it handle by the parent.
                     */
                    changeBackgroundColor(getResources().getColor(
                            android.R.color.transparent));
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (press) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2,
                    mPaint);
        }
    }

    private void changeBackgroundColor(int color) {
        LayerDrawable layer = (LayerDrawable) getBackground();
        GradientDrawable shape = (GradientDrawable) layer
                .findDrawableByLayerId(R.id.shape_background);
        shape.setColor(color);
    }

    /**
     * Make a dark color to press effect
     *
     * @return
     */
    protected int makePressColor() {
        int r = (this.backgroundColor >> 16) & 0xFF;
        int g = (this.backgroundColor >> 8) & 0xFF;
        int b = (this.backgroundColor) & 0xFF;
        r = (r - 30 < 0) ? 0 : r - 30;
        g = (g - 30 < 0) ? 0 : g - 30;
        b = (b - 30 < 0) ? 0 : b - 30;
        return Color.argb(70, r, g, b);
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (isEnabled())
            beforeBackground = backgroundColor;
        changeBackgroundColor(color);
    }

    public void setChecked(boolean check) {
        invalidate();
        this.check = check;
        setPressed(false);
        changeBackgroundColor(getResources().getColor(
                android.R.color.transparent));
        if (check) {
            step = 0;
        }
        if (check)
            checkView.changeBackground();

    }

    public boolean isCheck() {
        return check;
    }

    // View that contains checkbox
    private class Check extends View {

        private Bitmap sprite;
        private Rect mSrc;
        private Rect mDst;
        private boolean needDrawBackground = true;
        private boolean needReDraw = false;
        private boolean forceReDraw = false;
        private BitmapFactory.Options mOpt = new BitmapFactory.Options();

        public Check(Context context) {
            super(context);
            mOpt.inPreferredConfig = Bitmap.Config.RGB_565;
            setBackgroundResource(R.drawable.background_checkbox_uncheck);
            sprite = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sprite_check, mOpt);
        }

        public void changeBackground() {
            if (check) {
                setBackgroundResource(R.drawable.background_checkbox_check);
                LayerDrawable layer = (LayerDrawable) getBackground();
                GradientDrawable shape = (GradientDrawable) layer
                        .findDrawableByLayerId(R.id.shape_background);
                shape.setColor(backgroundColor);
            } else {
                setBackgroundResource(R.drawable.background_checkbox_uncheck);
            }
        }
        private void drawRect() {
            /*
             * Avoid useless memory allocation;
             */
            if (mSrc != null && mDst != null) {
                mSrc.set(40 * step, 0, (40 * step) + 40, 40);
                mDst.set(0, 0, this.getWidth() - 2, this.getHeight());
            } else {
                mSrc = new Rect(40 * step, 0, (40 * step) + 40, 40);
                mDst = new Rect(0, 0, this.getWidth() - 2, this.getHeight());
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (check) {
                if (step < 11) {
                    step++;

                    needDrawBackground = true;
                    needReDraw = true;
                }

                if (!needReDraw)
                    forceReDraw = true;
            } else {
                if (step >= 0) {
                    step--;

                    needDrawBackground = true;
                    needReDraw = true;
                }
                if (step == -1) {
                    if (needDrawBackground) {
                        changeBackground();

                        needDrawBackground = false;
                    }
                }
            }
            drawRect();
            canvas.drawBitmap(sprite, mSrc, mDst, null);
            if (needReDraw || forceReDraw && !needReDraw) {
                invalidate();
                needReDraw = false;
                forceReDraw = false;
            }
        }

    }

    public void setOncheckListener(OnCheckListener onCheckListener) {
        this.onCheckListener = onCheckListener;
    }

    public interface OnCheckListener {
        public void onCheck(boolean check);
    }

}