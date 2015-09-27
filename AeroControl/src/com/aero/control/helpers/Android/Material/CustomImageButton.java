package com.aero.control.helpers.Android.Material;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 03.06.15.
 * Simple CardBox class.
 */
public class CustomImageButton extends LinearLayout {

    private ImageView mImageView;

    public CustomImageButton(Context context) {
        super(context);
        init(context, null);
    }

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.imagebutton_layout, this, true);

        mImageView = (ImageView)this.findViewById(R.id.image_button);
        setBackground(attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", R.drawable.info));
    }

    public void setBackground(int drawable) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mImageView.setBackgroundDrawable(getResources().getDrawable(drawable));
        } else {
            mImageView.setBackground(getResources().getDrawable(drawable));
        }
    }
}
