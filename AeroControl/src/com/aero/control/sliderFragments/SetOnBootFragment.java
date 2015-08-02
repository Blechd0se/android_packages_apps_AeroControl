package com.aero.control.sliderFragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aero.control.R;
import com.aero.control.SplashScreen;

public class SetOnBootFragment extends Fragment {

    public static final String ARG_PAGE = "Set-On-Boot";
    public static final Typeface kitkatFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public static SetOnBootFragment create(int pageNumber) {
        SetOnBootFragment fragment = new SetOnBootFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SetOnBootFragment() {
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
                .inflate(R.layout.set_on_boot_fragment, container, false);

        // Set the title view to show the page number.
        TextView heading = ((TextView) rootView.findViewById(R.id.text_heading));
        heading.setText(R.string.introduction_setonboot_heading);
        heading.setTypeface(kitkatFont);

        TextView content = ((TextView) rootView.findViewById(R.id.text_content));
        content.setText(R.string.introduction_setonboot_content);
        content.setTypeface(kitkatFont);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SplashScreen)getActivity()).initDefaultSkip();
    }
}
