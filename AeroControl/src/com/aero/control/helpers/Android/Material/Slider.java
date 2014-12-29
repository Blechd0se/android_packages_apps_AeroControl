package com.aero.control.helpers.Android.Material;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aero.control.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Alexander Christ on 29.12.14.
 */
public class Slider extends CustomView {

    // Event when slider change value
    public interface OnValueChangedListener {
        public void onValueChanged(int value);
    }

    private Ball ball;
    public NumberIndicator numberIndicator;

    private Paint mPaint;
    private Paint mTransPaint;
    private Paint mEmptyPaint;
    private PorterDuffXfermode mPorterDuffXfermode;
    private Bitmap mBitmap;
    private Canvas mTemp;

    private boolean showNumberIndicator = false;
    private boolean press = false;

    private int value = 0;
    private int max = 100;
    private int min = 0;

    private OnValueChangedListener onValueChangedListener;

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (mPaint == null)
            mPaint = new Paint();

        if (mTransPaint == null)
            mTransPaint = new Paint();

        if (mEmptyPaint == null)
            mEmptyPaint = new Paint();

        if (mPorterDuffXfermode == null)
            mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        setAttributes(attrs);
    }

    @Override
    protected void onInitDefaultValues() {
        minWidth = 80;// size of view
        minHeight = 48;
        backgroundColor = Color.parseColor("#4CAF50");
        backgroundResId = R.drawable.background_transparent;
    }

    @Override
    protected void setAttributes(AttributeSet attrs) {
        super.setAttributes(attrs);
        if (!isInEditMode()) {
            getBackground().setAlpha(0);
        }
        showNumberIndicator = attrs.getAttributeBooleanValue(MATERIALDESIGNXML,"showNumberIndicator", false);
        min = attrs.getAttributeIntValue(MATERIALDESIGNXML, "min", 0);
        max = attrs.getAttributeIntValue(MATERIALDESIGNXML, "max", 100);// max > min
        value = attrs.getAttributeIntValue(MATERIALDESIGNXML, "value", min);

        float size = 20;
        String thumbSize = attrs.getAttributeValue(MATERIALDESIGNXML, "thumbSize");
        if (thumbSize != null) {
            size = dipOrDpToFloat(thumbSize);
        }

        ball = new Ball(getContext());
        setBallParams(size);
        addView(ball);

        // Set if slider content number indicator
        if (showNumberIndicator) {
            if (!isInEditMode()) {
                numberIndicator = new NumberIndicator(getContext());
            }
        }
    }

    private void setBallParams(float size) {
        RelativeLayout.LayoutParams params = new LayoutParams(
                dpToPx(size, getResources()), dpToPx(size, getResources()));
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        ball.setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!placedBall) {
            placeBall();
        }
        if (value == min) {
            // Crop line to transparent effect
            if (mBitmap == null)
                mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            if (mTemp == null)
                mTemp = new Canvas(mBitmap);
            mPaint.setColor(Color.parseColor("#B0B0B0"));
            mPaint.setStrokeWidth(dpToPx(2, getResources()));
            mTemp.drawLine(getHeight() / 2, getHeight() / 2, getWidth() - getHeight() / 2, getHeight() / 2, mPaint);
            mTransPaint.setColor(getResources().getColor(android.R.color.transparent));
            mTransPaint.setXfermode(mPorterDuffXfermode);
            mTemp.drawCircle(ViewHelper.getX(ball) + ball.getWidth() / 2,
                    ViewHelper.getY(ball) + ball.getHeight() / 2,
                    ball.getWidth() / 2, mTransPaint);

            canvas.drawBitmap(mBitmap, 0, 0, mEmptyPaint);
        } else {
            mPaint.setColor(Color.parseColor("#B0B0B0"));
            mPaint.setStrokeWidth(dpToPx(2, getResources()));
            canvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth() - getHeight() / 2, getHeight() / 2, mPaint);
            mPaint.setColor(backgroundColor);
            float division = (ball.xFin - ball.xIni) / (max - min);
            int value = this.value - min;
            canvas.drawLine(getHeight() / 2, getHeight() / 2, value * division + getHeight() / 2, getHeight() / 2, mPaint);
            // init ball's X
            ViewHelper.setX(ball, value * division + getHeight() / 2 - ball.getWidth() / 2);
            ball.changeBackground();
        }
        if (press && !showNumberIndicator) {
            mPaint.setColor(backgroundColor);
            mPaint.setAntiAlias(true);
            canvas.drawCircle(ViewHelper.getX(ball) + ball.getWidth() / 2, getHeight() / 2, getHeight() / 3, mPaint);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isLastTouch = true;
        if (isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (numberIndicator != null && !numberIndicator.isShowing())
                    numberIndicator.show();
                if ((event.getX() <= getWidth() && event.getX() >= 0)) {
                    press = true;
                    // calculate value
                    int newValue = 0;
                    float division = (ball.xFin - ball.xIni) / (max - min);
                    if (event.getX() > ball.xFin) {
                        newValue = max;
                    } else if (event.getX() < ball.xIni) {
                        newValue = min;
                    } else {
                        newValue = min + (int) ((event.getX() - ball.xIni) / division);
                    }
                    if (value != newValue) {
                        value = newValue;
                        if (onValueChangedListener != null)
                            onValueChangedListener.onValueChanged(newValue);
                    }
                    // move ball indicator
                    float x = event.getX();
                    x = (x < ball.xIni) ? ball.xIni : x;
                    x = (x > ball.xFin) ? ball.xFin : x;
                    ViewHelper.setX(ball, x);
                    ball.changeBackground();

                    // If slider has number indicator
                    if (numberIndicator != null) {
                        // move number indicator
                        numberIndicator.indicator.x = x;

                        numberIndicator.indicator.finalY = getRelativeTop(this) - getHeight();
                        numberIndicator.indicator.finalSize = getHeight() / 2;
                        numberIndicator.numberIndicator.setText("");
                    }

                } else {
                    press = false;
                    isLastTouch = false;
                    if (numberIndicator != null)
                        numberIndicator.dismiss();

                }

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (numberIndicator != null)
                    numberIndicator.dismiss();
                isLastTouch = false;
                press = false;
            }
        }
        return true;
    }

    private void placeBall() {
        ViewHelper.setX(ball, getHeight() / 2 - ball.getWidth() / 2);
        ball.xIni = ViewHelper.getX(ball);
        ball.xFin = getWidth() - getHeight() / 2 - ball.getWidth() / 2;
        ball.xCen = getWidth() / 2 - ball.getWidth() / 2;
        placedBall = true;
    }

    public OnValueChangedListener getOnValueChangedListener() {
        return onValueChangedListener;
    }

    public void setOnValueChangedListener(
            OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    public void setThumbSize(float size) {
        setBallParams(size);
    }

    public int getValue() {
        return value;
    }

    public void setProgress(int value) {
        setProgress(value, false);
    }

    public void setProgress(int value,boolean inRunnable) {
        if (value <= min) {
            value = min;
        }
        if (value >= max) {
            value = max;
        }
        setValueInRunnable(value,inRunnable);
    }


    private void setValueInRunnable(final int value,final boolean inRunnable) {
        if(!placedBall && inRunnable)
            post(new Runnable() {
                @Override
                public void run() {
                    setProgress(value, inRunnable);
                }
            });
        else{
            this.value = value;
            float division = (ball.xFin - ball.xIni) / max;
            ViewHelper.setX(ball,value*division + getHeight()/2 - ball.getWidth()/2);
            ball.changeBackground();
        }
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public boolean isShowNumberIndicator() {
        return showNumberIndicator;
    }

    public void showNumberIndicator(boolean showNumberIndicator) {
        this.showNumberIndicator = showNumberIndicator;
        if (!isInEditMode()) {
            numberIndicator = (showNumberIndicator) ? new NumberIndicator(getContext()) : null;
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (isEnabled()) {
            beforeBackground = backgroundColor;
        }
    }

    private boolean placedBall = false;

    private class Ball extends View {

        private float xIni, xFin, xCen;

        public Ball(Context context) {
            super(context);
            if (!isInEditMode()) {
                setBackgroundResource(R.drawable.background_switch_ball_uncheck);
            } else {
                setBackgroundResource(android.R.drawable.radiobutton_off_background);
            }
        }

        public void changeBackground() {
            if (!isInEditMode()) {
                if (value != min) {
                    setBackgroundResource(R.drawable.background_checkbox);
                    LayerDrawable layer = (LayerDrawable) getBackground();
                    GradientDrawable shape = (GradientDrawable) layer
                            .findDrawableByLayerId(R.id.shape_background);
                    shape.setColor(backgroundColor);
                } else {
                    setBackgroundResource(R.drawable.background_switch_ball_uncheck);
                }
            }
        }

    }

    // Slider Number Indicator

    public class NumberIndicator extends Dialog {

        private Indicator indicator;
        private TextView numberIndicator;

        public NumberIndicator(Context context) {
            super(context, R.style.Translucent);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.number_indicator_spinner);
            setCanceledOnTouchOutside(false);

            RelativeLayout content = (RelativeLayout) this.findViewById(R.id.number_indicator_spinner_content);
            indicator = new Indicator(this.getContext());
            content.addView(indicator);

            numberIndicator = new TextView(getContext());
            numberIndicator.setTextColor(Color.WHITE);
            numberIndicator.setGravity(Gravity.CENTER);
            content.addView(numberIndicator);

            indicator.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
        }

        @Override
        public void dismiss() {
            super.dismiss();
            indicator.y = 0;
            indicator.size = 0;
            indicator.animate = true;
        }

        @Override
        public void onBackPressed() {

        }

    }

    private class Indicator extends RelativeLayout {

        // Position of number indicator
        private float x = 0;
        private float y = 0;
        // Size of number indicator
        private float size = 0;

        private Paint mPaint;

        // Final y position after animation
        private float finalY = 0;
        // Final size after animation
        private float finalSize = 0;

        private boolean animate = true;

        private boolean numberIndicatorResize = false;

        public Indicator(Context context) {
            super(context);
            if (mPaint == null)
                mPaint = new Paint();
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (!numberIndicatorResize) {
                RelativeLayout.LayoutParams params = (LayoutParams) numberIndicator.
                        numberIndicator.getLayoutParams();
                params.height = (int) finalSize * 2;
                params.width = (int) finalSize * 2;
                numberIndicator.numberIndicator.setLayoutParams(params);
            }

            mPaint.setAntiAlias(true);
            mPaint.setColor(backgroundColor);
            if (animate) {
                if (y == 0)
                    y = finalY + finalSize * 2;
                y -= dpToPx(-13, getResources());
                size += dpToPx(2, getResources());
            }
            canvas.drawCircle(
                    ViewHelper.getX(ball) + getRelativeLeft((View) ball.getParent())
                            + ball.getWidth() / 2, y, size, mPaint);
            if (animate && size >= finalSize)
                animate = false;
            if (!animate) {
                ViewHelper.setX(numberIndicator.numberIndicator,
                        (ViewHelper.getX(ball) + getRelativeLeft((View) ball.getParent()) + ball.getWidth() / 2) - size);
                ViewHelper.setY(numberIndicator.numberIndicator, y - size);
                numberIndicator.numberIndicator.setText(value + "");
            }
            invalidate();
        }

    }

}