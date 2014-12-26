package com.aero.control.sliderFragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.SplashScreen;

import java.io.FileOutputStream;
import java.io.IOException;

public class TutorialFragment extends Fragment {

    private static final String ARG_PAGE = "Tutorial";
    private static final Typeface kitkatFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public static TutorialFragment create(int pageNumber) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TutorialFragment() {
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
                .inflate(R.layout.tutorial_fragment, container, false);

        // Set the title view to show the page number.
        TextView heading = ((TextView) rootView.findViewById(R.id.text_heading));
        heading.setText(R.string.introduction_tutorial_heading);
        heading.setTypeface(kitkatFont);

        TextView content = ((TextView) rootView.findViewById(R.id.text_content));
        content.setText(R.string.introduction_tutorial_content);
        content.setTypeface(kitkatFont);

        final CheckBox checkbox = (CheckBox) rootView.findViewById(R.id.show_checkbox);
        checkbox.setTypeface(kitkatFont);

        final Button button = (Button) rootView.findViewById(R.id.show_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FileOutputStream fos;
                // Set tutorials?
                boolean check_state = checkbox.isChecked();

                try {
                    fos = getActivity().openFileOutput(SplashScreen.FIRSTRUN_AERO, Context.MODE_PRIVATE);
                    fos.write("1".getBytes());

                    if (!check_state) {
                        fos = getActivity().openFileOutput("firstrun", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos = getActivity().openFileOutput("firstrun_cpu", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos = getActivity().openFileOutput("firstrun_perapp", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos = getActivity().openFileOutput("firstrun_profiles", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos = getActivity().openFileOutput("firstrun_statistics", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos = getActivity().openFileOutput("firstrun_trim", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                        fos = getActivity().openFileOutput("firstrun_misc", Context.MODE_PRIVATE);
                        fos.write("1".getBytes());
                    }
                    fos.close();
                }
                catch (IOException e) {
                    Log.e("Aero", "Could not save file(s). ", e);
                }

                Intent i = new Intent(getActivity(), AeroActivity.class);
                startActivity(i);

                // close this activity
                getActivity().finish();
            }
        });

        return rootView;
    }
}
