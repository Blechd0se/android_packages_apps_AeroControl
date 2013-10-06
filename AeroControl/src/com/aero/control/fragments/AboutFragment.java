package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.aero.control.R;
import com.aero.control.adapter.AeroAdapter;
import com.aero.control.adapter.adapterInit;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Overview Fragment
 */
public class AboutFragment extends Fragment {

    public ListView listView1;
    public TextView textView1;

    public static Fragment newInstance(Context context) {
        AboutFragment f = new AboutFragment();

        return f;
    }

    // Override for custom view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.about_screen, null);

        // Get name;
        getActivity().setTitle(R.string.about);

        // Default Overview Menu
        adapterInit overview_data[] = new adapterInit[]
                {
                        // First Value (0) is for loadable images.
                        new adapterInit(R.drawable.quarx, "A", "A"),
                        new adapterInit(R.drawable.blechdose, "A", "A"),
                };

        listView1 = (ListView) root.findViewById(R.id.listViewImage);
        AeroAdapter adapter = new AeroAdapter(getActivity(),
                R.layout.about_screen, overview_data);

        listView1.setAdapter(adapter);

        textView1 = (TextView) root.findViewById(R.id.aboutScreen);
        textView1.setText(" The joy of living is in the giving.");


        return root;
    }


}