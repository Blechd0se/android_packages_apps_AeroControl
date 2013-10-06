package com.aero.control.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 16.09.13.
 * Default Updater Fragment
 */
public class UpdaterFragment extends Fragment {
    /*
    TODO:
     */

    public static Fragment newInstance(Context context) {
        UpdaterFragment f = new UpdaterFragment();

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.gpu_fragment, null);
        return root;
    }
}
