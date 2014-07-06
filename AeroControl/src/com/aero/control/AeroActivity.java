package com.aero.control;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.control.fragments.AeroFragment;
import com.aero.control.fragments.CPUFragment;
import com.aero.control.fragments.DefyPartsFragment;
import com.aero.control.fragments.GPUFragment;
import com.aero.control.fragments.MemoryFragment;
import com.aero.control.fragments.MiscSettingsFragment;
import com.aero.control.fragments.ProfileFragment;
import com.aero.control.fragments.StatisticsFragment;
import com.aero.control.fragments.UpdaterFragment;
import com.aero.control.helpers.rootHelper;
import com.aero.control.helpers.shellHelper;
import com.aero.control.lists.generatingLists;
import com.aero.control.lists.generatingLists.PreferenceItem;
import com.aero.control.prefs.PrefsActivity;
import com.aero.control.service.PerAppService;
import com.aero.control.service.PerAppServiceHelper;

import java.util.ArrayList;

public final class AeroActivity extends Activity {

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
    private static final int STATISTICS = 2;
    private static final int GPU = 3;
    private static final int MEMORY = 4;
    private static final int MISC = 5;
    private static final int UPDATER = 6;
    private static final int PROFILE = 7;

    // Fragments;
    private AeroFragment mAeroFragment;
    private CPUFragment mCPUFragement;
    private GPUFragment mGPUFragement;
    private DefyPartsFragment mDefyPartsFragment;
    private MemoryFragment mMemoryFragment;
    private UpdaterFragment mUpdaterFragement;
    private ProfileFragment mProfileFragment;
    private StatisticsFragment mStatisticsFragment;
    private MiscSettingsFragment mMiscSettingsFragment;

    private SharedPreferences prefs;

    private int mBackCounter = 0;

    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    public static final Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    public int mActionBarTitleID;
    public TextView mActionBarTitle;

    private static final rootHelper rootCheck = new rootHelper();
    public static final shellHelper shell = new shellHelper();
    public static PerAppServiceHelper perAppService;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String a = prefs.getString("app_theme", null);
        int actionBarHeight = 0;
        getActionBar().setIcon(R.drawable.app_icon_actionbar);

        if (a == null)
            a = "";

        if (a.equals("red"))
            setTheme(R.style.RedHolo);
        else if (a.equals("light"))
            setTheme(android.R.style.Theme_Holo_Light);
        else if (a.equals("dark"))
            setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
        else
            setTheme(R.style.RedHolo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                !(ViewConfiguration.get(getBaseContext()).hasPermanentMenuKey())) {

            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            winParams.flags |= bits;
            win.setAttributes(winParams);

            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the service if needed;
        if (!isServiceUp()) {
            // Service is not running, check if it should;
            perAppService = new PerAppServiceHelper(getBaseContext());
            if (perAppService.shouldBeStarted())
                perAppService.startService();
        }

        // Assign action bar title;
        mActionBarTitleID = getResources().getIdentifier("action_bar_title", "id", "android");
        mActionBarTitle = (TextView) findViewById(mActionBarTitleID);
        mActionBarTitle.setTypeface(font);

        // Check if system has root;
        if (!rootCheck.isDeviceRooted())
            showRootDialog();

        mTitle = mDrawerTitle = getTitle();
        mAeroTitle = getResources().getStringArray(R.array.aero_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        if (actionBarHeight != 0) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mDrawerLayout.getLayoutParams();
            params.setMargins(0, actionBarHeight + 50, 0, 0);
            mDrawerLayout.setLayoutParams(params);
        }

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
                setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        } else {
            selectItem(savedInstanceState.getInt(SELECTED_ITEM));
        }

    }

    private final class ItemAdapter extends ArrayAdapter<PreferenceItem> {

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

                text.setTypeface(font);

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String a = prefs.getString("app_theme", null);

        if (a == null)
            a = "";

        if (a.equals("red"))
            inflater.inflate(R.menu.main, menu);
        else if (a.equals("light"))
            inflater.inflate(R.menu.main, menu);
        else if (a.equals("dark"))
            inflater.inflate(R.menu.main_light, menu);
        else
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

        switch (item.getItemId()) {
            case R.id.aero_settings:

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

    private final boolean isServiceUp() {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PerAppService.class.getName().equals(service.service.getClassName())) {
                // its already up and running
                return true;
            }
        }
        return false;
    }


    private final void selectItem(int position) {

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
            case STATISTICS:

                if (mStatisticsFragment == null) {
                    mStatisticsFragment = new StatisticsFragment();
                }
                fragment = mStatisticsFragment;
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
            case MISC:
                /* Defy parts for defy, misc settings for the rest.
                 * Notice; we override position later for different headings
                 */
                if (Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526")) {
                    if (mDefyPartsFragment == null) {
                        mDefyPartsFragment = new DefyPartsFragment();
                    }
                    fragment = mDefyPartsFragment;
                } else {
                    if (mMiscSettingsFragment == null) {
                        mMiscSettingsFragment = new MiscSettingsFragment();
                    }
                    fragment = mMiscSettingsFragment;
                }
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
        if (!(Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526")) && position == 5)
            position = 8;
        setTitle(mAeroTitle[position]);

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public final void setTitle(CharSequence title) {
        mTitle = title;
        mActionBarTitle.setText(mTitle);
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

        FragmentManager fm = getFragmentManager();

        if (fm.getBackStackEntryCount() == 1) {
            try {
                fm.popBackStackImmediate();
                return;
            } catch (IllegalStateException e) {
                /*
                 * When we are on a sub-fragment and change to a parent fragment
                 * and then press the back button we would end up in an unexpected
                 * state, because the fragment in question was already added previously.
                 *
                 * We don't have to handle this case in any special means, thanks to
                 * the back counter logic.
                 */
            }
        }

        mBackCounter++;
        Toast.makeText(this, R.string.back_for_close, Toast.LENGTH_SHORT).show();

        if (mBackCounter == 2)
            finish();

    }

    public final void showRootDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.about_screen, null);
        TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

        builder.setTitle(R.string.not_rooted);
        builder.setIcon(R.drawable.ic_action_warning);

        aboutText.setText(getText(R.string.root_required));
        builder.setCancelable(false);

        builder.setView(layout)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.show();
    }

    public final void switchContent(final Fragment fragment) {

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