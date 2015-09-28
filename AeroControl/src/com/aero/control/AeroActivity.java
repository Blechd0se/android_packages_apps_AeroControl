package com.aero.control;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.aero.control.fragments.AppMonitorFragment;
import com.aero.control.fragments.StatisticsFragment;
import com.aero.control.helpers.PerApp.AppMonitor.JobManager;
import com.aero.control.navItems.NavBarItems;
import com.aero.control.fragments.AeroFragment;
import com.aero.control.fragments.CPUFragment;
import com.aero.control.fragments.DefyPartsFragment;
import com.aero.control.fragments.GPUFragment;
import com.aero.control.fragments.MemoryFragment;
import com.aero.control.fragments.MiscSettingsFragment;
import com.aero.control.fragments.ProfileFragment;
import com.aero.control.helpers.GenericHelper;
import com.aero.control.testsuite.TestSuiteFragment;
import com.aero.control.fragments.UpdaterFragment;
import com.aero.control.helpers.rootHelper;
import com.aero.control.helpers.shellHelper;
import com.aero.control.navItems.NavBarItems.PreferenceItem;
import com.aero.control.settings.PrefsActivity;
import com.aero.control.service.PerAppService;
import com.aero.control.service.PerAppServiceHelper;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import java.util.ArrayList;
import java.util.Stack;

public final class AeroActivity extends Activity {

    private static final String SELECTED_ITEM = "SelectedItem";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ItemAdapter mAdapter;
    public static Stack<Fragment> mFragmentStack;

    private DrawerArrowDrawable mDrawerArrow;
    private CharSequence mTitle;
    private String[] mAeroTitle;
    private int mPreviousTitle;
    private static int mBackCounter = 0;

    // Fragment Keys;
    private static final int OVERVIEW = 0;
    private static final int CPU = 1;
    private static final int STATISTICS = 2;
    private static final int GPU = 3;
    private static final int MEMORY = 4;
    private static final int MISC = 5;
    private static final int DEFY = 6;
    private static final int UPDATER = 7;
    private static final int PROFILE = 8;
    private static final int APPSTATISTICS = 9;
    private static final int TESTSUITE = 10;

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
    private AppMonitorFragment mAppStatisticsFragment;
    private TestSuiteFragment mTestSuiteFragment;

    public static final Handler mHandler = new Handler(Looper.getMainLooper());
    public static final Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
    public int mActionBarTitleID;
    public TextView mActionBarTitle;
    private ActionBar mActionBar;

    private static final rootHelper rootCheck = new rootHelper();
    public static final shellHelper shell = new shellHelper();
    public static PerAppServiceHelper perAppService;

    public static GenericHelper genHelper = new GenericHelper();
    public static JobManager mJobManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mJobManager = JobManager.instance(AeroActivity.this);

        int actionBarHeight = 0;

        if (getActionBar() != null) {
            getActionBar().setIcon(android.R.color.transparent);
        }

        mFragmentStack = new Stack<Fragment>();

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

        // Start the service if needed;
        if (!isServiceUp()) {
            // Service is not running, check if it should;
            perAppService = new PerAppServiceHelper(this);
            if (perAppService.shouldBeStarted())
                perAppService.startService();
        }

        // Assign action bar title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar = getActionBar();
        } else {
            mActionBarTitleID = getResources().getIdentifier("action_bar_title", "id", "android");
            mActionBarTitle = (TextView) findViewById(mActionBarTitleID);
            mActionBarTitle.setTypeface(font);
        }

        // Check if system has root;
        if (!rootCheck.isDeviceRooted())
            showRootDialog();

        mTitle = getTitle();
        mAeroTitle = getResources().getStringArray(R.array.aero_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        if (actionBarHeight != 0) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mDrawerLayout.getLayoutParams();
            params.setMargins(0, actionBarHeight + (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()), 0, 0);
            mDrawerLayout.setLayoutParams(params);
        }

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        // Set up lists;
        NavBarItems content = new NavBarItems(this);

        mAdapter = new ItemAdapter(this, R.layout.activity_main, content.ITEMS);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon

        mDrawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };

        // Navigation Drawer with toggle and animation;
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mDrawerArrow,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            selectItem(OVERVIEW);
        } else {
            selectItem(savedInstanceState.getInt(SELECTED_ITEM));
        }

        // Handle notification click here;
        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                if (extras.getString("NOTIFY_STRING").equals("APPMONITOR")) {
                    selectItem(!(Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526")) ? PROFILE : APPSTATISTICS);
                }
            }
        } else {
            if (savedInstanceState.getSerializable("NOTIFY_STRING") != null) {
                if (savedInstanceState.getSerializable("NOTIFY_STRING").equals("APPMONITOR")) {
                    selectItem(!(Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526")) ? PROFILE : APPSTATISTICS);
                }
            }
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
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //If we don't do this, the application will crash when resume via a notification.
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();

        // Display the appmonitor upon resume;
        if (extras != null) {
            if (extras.getString("NOTIFY_STRING").equals("APPMONITOR")) {
                selectItem(!(Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526")) ? PROFILE : APPSTATISTICS);
            }
        }

        // Reset the string;
        getIntent().putExtra("NOTIFY_STRING", new String());

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
                trIntent.setClass(this, PrefsActivity.class);
                trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(trIntent);
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

    private boolean isServiceUp() {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PerAppService.class.getName().equals(service.service.getClassName())) {
                // its already up and running
                return true;
            }
        }
        return false;
    }


    private void selectItem(int position) {

        int j = position;

        if(mDrawerLayout != null)
            mDrawerLayout.closeDrawers();

        // update the main content by replacing fragments
        Fragment fragment = null;

        if (!(Build.MODEL.equals("MB525") || Build.MODEL.equals("MB526"))
                && position >= DEFY)
            j++;

        // Switch to show different fragments;
        switch (j) {
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
                if (mMiscSettingsFragment == null) {
                    mMiscSettingsFragment = new MiscSettingsFragment();
                }
                fragment = mMiscSettingsFragment;
                break;
            case DEFY:
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
            case APPSTATISTICS:
                if (mAppStatisticsFragment == null) {
                    mAppStatisticsFragment = new AppMonitorFragment();
                }
                fragment = mAppStatisticsFragment;
                break;
            case TESTSUITE:
                if (mTestSuiteFragment == null) {
                    mTestSuiteFragment = new TestSuiteFragment();
                }
                fragment = mTestSuiteFragment;
                break;
        }

        if (fragment != null)
            switchContent(fragment);

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mPreviousTitle = j;
        setTitle(mAeroTitle[j]);
        mBackCounter = 0;

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void setActionBarTitle(String title) {
        setTitle(title);
    }

    public final void setTitle(CharSequence title) {
        mTitle = title;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mActionBar != null)
                mActionBar.setTitle(mTitle);
        } else {
            if (mActionBarTitle != null)
                mActionBarTitle.setText(mTitle);
        }
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

        if (mFragmentStack.size() > 1) {
            switchContent(mFragmentStack.lastElement());
            setTitle(mAeroTitle[mPreviousTitle]);
        }

        // Back-Button logic;
        mBackCounter++;
        if (mBackCounter == 1) {
            Toast.makeText(this, R.string.back_for_close, Toast.LENGTH_LONG).show();
        }
        if (mBackCounter == 2)
            finish();
    }

    /**
     * Resets the back button counter logic.
     */
    public static void resetBackCounter() {
        mBackCounter = 0;
    }

    public final void showRootDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.about_screen, null);
        TextView aboutText = (TextView) layout.findViewById(R.id.aboutScreen);

        builder.setTitle(R.string.not_rooted);
        builder.setIcon(R.drawable.warning);

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

                /*
                 * Somehow the activity is destroyed sometimes when we switched activities which
                 * forced an orientation. To hopefully avoid this, we are just restarting the app
                 * safely.
                 */
                try {
                    getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in,
                            android.R.animator.fade_out).replace(R.id.content_frame, fragment).commitAllowingStateLoss();
                } catch (IllegalStateException e) {
                    recreate();
                }
            }
        },genHelper.getDefaultDelay());
        mFragmentStack.push(fragment);
    }
}