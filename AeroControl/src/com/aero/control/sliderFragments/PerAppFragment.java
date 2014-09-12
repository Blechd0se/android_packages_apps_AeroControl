package com.aero.control.sliderFragments;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aero.control.R;

public class PerAppFragment extends Fragment {

    public static final String ARG_PAGE = "PerApp";
    public static final Typeface kitkatFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public static PerAppFragment create(int pageNumber) {
        PerAppFragment fragment = new PerAppFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PerAppFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        final ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.perappshow_fragment, container, false);

        // Set the title view to show the page number.
        TextView heading = ((TextView) rootView.findViewById(R.id.text_heading));
        heading.setText(R.string.introduction_perapp_heading);
        heading.setTypeface(kitkatFont);

        TextView content = ((TextView) rootView.findViewById(R.id.text_content));
        content.setText(R.string.introduction_perapp_content);
        content.setTypeface(kitkatFont);

        CheckBox checkbox = (CheckBox) rootView.findViewById(R.id.show_checkbox);
        checkbox.setTypeface(kitkatFont);

        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.show_progress);
        progressBar.setVisibility(View.INVISIBLE);

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = mSharedPreference.edit();
                progressBar.setVisibility(View.VISIBLE);

                // Service should be enabled;
                if (checked) {
                    editor.putBoolean("per_app_service", true);
                } else {
                    editor.putBoolean("per_app_service", false);
                }
                editor.commit();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }, 1500);
            }
        });

        return rootView;
    }
}
