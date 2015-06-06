package com.aero.control.helpers.Android.Material;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 03.06.15.
 * Simple CardBox class.
 */
public class CardBox extends LinearLayout {

    private TextView mTextTitle;
    private TextView mTextContent;

    public CardBox(Context context) {
        super(context);
        init(context, null);
    }

    public CardBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CardBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        String title = "";
        String content = "";
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardBox, 0, 0);
            title = ta.getString(R.styleable.CardBox_cardTitle);
            content = ta.getString(R.styleable.CardBox_cardContent);
            ta.recycle();
        }

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cardbox_layout, this, true);

        mTextTitle = (TextView) this.findViewById(R.id.card_title);
        mTextContent = (TextView) this.findViewById(R.id.card_content);

        mTextTitle.setText(title);
        mTextContent.setText(content);
    }

    public void setTitle(String title) {
        mTextTitle.setText(title);
        invalidate();
    }

    public String getTitle() {
        return mTextTitle.getText().toString();
    }

    public void setContent(String content) {
        mTextContent.setText(content);
        invalidate();
    }

    public String getContent() {
        return mTextContent.getText().toString();
    }

    public void setBackground(int drawable) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            this.setBackgroundDrawable(getResources().getDrawable(drawable));
        } else {
            this.setBackground(getResources().getDrawable(drawable));
        }
    }

}
