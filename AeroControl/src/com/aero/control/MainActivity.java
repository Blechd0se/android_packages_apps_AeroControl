package com.aero.control;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.fragments.AeroFragment;
import com.aero.control.fragments.CPUFragment;
import com.aero.control.fragments.DefyPartsFragment;
import com.aero.control.fragments.GPUFragment;
import com.aero.control.fragments.MemoryFragment;
import com.aero.control.fragments.ProfileFragment;
import com.aero.control.fragments.UpdaterFragment;
import com.aero.control.lists.generatingLists;
import com.aero.control.lists.generatingLists.PreferenceItem;
import com.aero.control.prefs.PrefsActivity;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String SELECTED_ITEM = "SelectedItem";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ItemAdapter mAdapter;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mAeroTitle;

    // Fragment Keys;
    private static final int OVERVIEW = 0;
    private static final int CPU = 1;
    private static final int GPU = 2;
    private static final int MEMORY = 3;
    private static final int DEFYPARTS = 4;
    private static final int UPDATER = 5;
    private static final int PROFILE = 6;

    // Fragments;
    private AeroFragment mAeroFragment;
    private CPUFragment mCPUFragement;
    private GPUFragment mGPUFragement;
    private DefyPartsFragment mDefyPartsFragment;
    private MemoryFragment mMemoryFragment;
    private UpdaterFragment mUpdaterFragement;
    private ProfileFragment mProfileFragment;

    private int mBackCounter = 0;

    final Handler mHandler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mAeroTitle = getResources().getStringArray(R.array.aero_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        // Set up lists;
        generatingLists content = new generatingLists();

        mAdapter = new ItemAdapter(this, R.layout.activity_main, content.ITEMS);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(6);
        } else {
            selectItem(savedInstanceState.getInt(SELECTED_ITEM));
        }

    }

    private class ItemAdapter extends ArrayAdapter<PreferenceItem> {

        private ArrayList<PreferenceItem> items;

        public ItemAdapter(Context context, int textViewResourceId,
                           ArrayList<PreferenceItem> objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = getLayoutInflater();
                v = vi.inflate(R.layout.adapter_item, null);
            }

            PreferenceItem item = items.get(position);

            if (item != null) {
                ImageView icon = (ImageView) v.findViewById(R.id.icon);
                TextView text = (TextView) v.findViewById(R.id.text);

                if (icon != null) {
                    icon.setImageResource(item.drawable);
                }

                if (text != null) {
                    text.setText(getString(item.content));
                }
            }

            return v;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.aero_settings:
                Toast.makeText(this, "Update location and App Theme are not implemented yet.", Toast.LENGTH_SHORT).show();


                Intent trIntent = new Intent("android.intent.action.PREFS");
                trIntent.setClass(getBaseContext(), PrefsActivity.class);
                trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getBaseContext().startActivity(trIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }


    private void selectItem(int position) {

        drawer: mDrawerLayout.closeDrawers();

        // update the main content by replacing fragments
        Fragment fragment = null;


        // Switch to show different fragments;
        switch (position) {
            case OVERVIEW:
                if (mAeroFragment == null) {
                    mAeroFragment = new AeroFragment();
                }
                fragment = mAeroFragment;
                break;
            case CPU:
                if (mCPUFragement == null) {
                    mCPUFragement = new CPUFragment();
                }
                fragment = mCPUFragement;
                break;
            case GPU:
                if (mGPUFragement == null) {
                    mGPUFragement = new GPUFragment();
                }
                fragment = mGPUFragement;
                break;
            case MEMORY:
                if (mMemoryFragment == null) {
                    mMemoryFragment = new MemoryFragment();
                }
                fragment = mMemoryFragment;
                break;
            case DEFYPARTS:
                if (mDefyPartsFragment == null) {
                    mDefyPartsFragment = new DefyPartsFragment();
                }
                fragment = mDefyPartsFragment;
                break;
            case UPDATER:

                if (mUpdaterFragement == null) {
                    mUpdaterFragement = new UpdaterFragment();
                }
                fragment = mUpdaterFragement;
                break;
            case PROFILE:

                if (mProfileFragment == null) {
                    mProfileFragment = new ProfileFragment();
                }
                fragment = mProfileFragment;
                break;
        }

        if (fragment != null)
            switchContent(fragment);


        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mAeroTitle[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        mBackCounter++;

        Toast.makeText(this, R.string.back_for_close, Toast.LENGTH_SHORT).show();

        if (getFragmentManager().getBackStackEntryCount() == 1)
            return;

        if (mBackCounter == 2)
            finish();
    }

    public void switchContent(final Fragment fragment) {

        // Reduce the navigation drawer delay for a smoother UI;
        mHandler.postDelayed(new Runnable()  {
            @Override
            public void run() {

                getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in,
                        android.R.animator.fade_out).replace(R.id.content_frame, fragment).commit();

                // Reset our BackCounter
                mBackCounter = 0;
            }
        },250);
    }
}