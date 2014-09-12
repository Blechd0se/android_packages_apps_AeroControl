package com.aero.control.sliderFragments;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.aero.control.R;

public class IntroductionFragment extends Fragment {

    public static final String ARG_PAGE = "Introduction";
    public static final Typeface kitkatFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public static IntroductionFragment create(int pageNumber) {
        IntroductionFragment fragment = new IntroductionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public IntroductionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.introduction_fragment, container, false);

        // Set the title view to show the page number.
        TextView heading = ((TextView) rootView.findViewById(R.id.text_heading));
        heading.setText(R.string.introduction_welcome_heading);
        heading.setTypeface(kitkatFont);

        TextView content = ((TextView) rootView.findViewById(R.id.text_content));
        content.setText(R.string.introduction_welcome_content);
        content.setTypeface(kitkatFont);

        ImageView image = (ImageView) rootView.findViewById(R.id.image_content);
        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
        anim.setStartOffset(500);
        image.setAnimation(anim);

        return rootView;
    }
}
