package com.aero.control.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aero.control.R;

/**
 * Created by Alexander Christ on 10.10.13.
 */
public class ProfileFragment extends PreferenceFragment {

    /*
     * TODO: - Make survival over fragment switch
     *       - save as commit()
     */

    private ViewGroup mContainerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View v = inflater.inflate(R.layout.profile_fragment, null);

        mContainerView = (ViewGroup)v.findViewById(R.id.container);

        return mContainerView;
    }

    // Create our options menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profiles_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                // Hide the "empty" view since there is now at least one item in the list.
                mContainerView.findViewById(android.R.id.empty).setVisibility(View.GONE);
                showDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {

        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_a_name)
                .setMessage(R.string.define_a_name)
                .setView(input)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Get content;
                        addItem(input.getText());

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    // Adds the object to our "list"
    private void addItem(Editable s) {

        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(
                R.layout.profiles_list, mContainerView, false);

        ((TextView) newView.findViewById(android.R.id.text1)).setText(s);

        newView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContainerView.removeView(newView);

                // If there are no rows remaining, show the empty view.
                if (mContainerView.getChildCount() == 0) {
                    mContainerView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
            }
        });

        mContainerView.addView(newView, 0);
    }

}
