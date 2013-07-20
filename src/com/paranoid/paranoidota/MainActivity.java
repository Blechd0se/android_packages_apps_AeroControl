/*
 * Copyright 2013 The Android Open Source Project
 * Copyright 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.paranoidota;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paranoid.paranoidota.ListItems.PreferenceItem;
import com.paranoid.paranoidota.Utils.NotificationInfo;
import com.paranoid.paranoidota.activities.SettingsActivity;
import com.paranoid.paranoidota.fragments.ChangelogFragment;
import com.paranoid.paranoidota.fragments.GappsFragment;
import com.paranoid.paranoidota.fragments.RomFragment;
import com.paranoid.paranoidota.fragments.UpdateFragment;
import com.paranoid.paranoidota.helpers.DownloadHelper;
import com.paranoid.paranoidota.helpers.DownloadHelper.DownloadCallback;
import com.paranoid.paranoidota.helpers.NotificationHelper;
import com.paranoid.paranoidota.helpers.NotificationHelper.NotificationCallback;
import com.paranoid.paranoidota.helpers.RebootHelper;
import com.paranoid.paranoidota.helpers.RecoveryHelper;
import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;
import com.paranoid.paranoidota.updater.Updater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;
import com.paranoid.paranoidota.updater.Updater.UpdaterListener;

public class MainActivity extends Activity implements DownloadCallback, NotificationCallback,
        UpdaterListener {

    private static final String SELECTED_ITEM = "SelectedItem";

    private static final int CHECK_UPDATES = 0;
    private static final int ROM = 1;
    private static final int GOOGLE_APPS = 2;
    private static final int CHANGELOG = 3;

    private UpdateFragment mUpdateFragment;
    private RomFragment mRomFragment;
    private GappsFragment mGappsFragment;
    private ChangelogFragment mChangelogFragment;

    private RomUpdater mRomUpdater;
    private GappsUpdater mGappsUpdater;
    private NotificationInfo mNotificationInfo;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ItemAdapter mAdapter;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mSliderTitles;
    private int[] mNotifications = new int[] {0, 0};
    private int mPosition;

    private NotificationHelper mNotificationHelper;
    private RecoveryHelper mRecoveryHelper;
    private RebootHelper mRebootHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mSliderTitles = getResources().getStringArray(R.array.slider_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ListItems content = new ListItems();
        mAdapter = new ItemAdapter(this, R.layout.adapter_item, content.ITEMS);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        setProgressBarIndeterminate(true);

        // Helper methods
        mNotificationHelper = new NotificationHelper(this);
        mRecoveryHelper = new RecoveryHelper(this);
        mRebootHelper = new RebootHelper(this, mRecoveryHelper);

        // No need to instantiate Download Helper, as it's static
        DownloadHelper.registerCallback(this, this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                mAdapter.notifyDataSetChanged();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                mAdapter.notifyDataSetChanged();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mRomUpdater = new RomUpdater(this, false);
        mRomUpdater.addUpdaterListener(this);
        mGappsUpdater = new GappsUpdater(this, false);
        mGappsUpdater.addUpdaterListener(this);

        Intent intent = getIntent();
        onNewIntent(intent);

        if (savedInstanceState == null) {
            if (mNotificationInfo != null) {
                if (mNotificationInfo.mNotificationId != Updater.ROM_NOTIFICATION_ID) {
                    mRomUpdater.check();
                } else {
                    mRomUpdater.setLastUpdates(mNotificationInfo.mPackageInfos);
                    mNotificationHelper.setNotifications(mNotificationInfo.mPackageInfos.length,
                            NotificationHelper.NO_UPDATE);
                }
                if (mNotificationInfo.mNotificationId != Updater.GAPPS_NOTIFICATION_ID) {
                    mGappsUpdater.check();
                } else {
                    mGappsUpdater.setLastUpdates(mNotificationInfo.mPackageInfos);
                    mNotificationHelper.setNotifications(NotificationHelper.NO_UPDATE,
                            mNotificationInfo.mPackageInfos.length);
                }
            } else {
                mRomUpdater.check();
                mGappsUpdater.check();
            }
            selectItem(0);
        } else {
            selectItem(savedInstanceState.getInt(SELECTED_ITEM));
        }

        if (!Utils.alarmExists(this)) {
            Utils.setAlarm(this, true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mNotificationInfo = null;
        if (intent != null && intent.getExtras() != null) {
            mNotificationInfo = (NotificationInfo) intent.getSerializableExtra(Utils.FILES_INFO);
            if (intent.getBooleanExtra(Utils.CHECK_DOWNLOADS_FINISHED, false)) {
                DownloadHelper.checkDownloadFinished(this);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadHelper.unregisterCallback();
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
                RelativeLayout countLayout = (RelativeLayout) v.findViewById(R.id.count_layout);
                if ((position == ROM || position == GOOGLE_APPS)) {
                    int index = position - 1;
                    if (mDrawerLayout.isDrawerVisible(mDrawerList)) {
                        AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setDuration(500);
                        countLayout.setVisibility(mNotifications[index] > 0 ? View.VISIBLE
                                : View.GONE);
                        countLayout.startAnimation(fadeIn);
                    } else {
                        countLayout.clearAnimation();
                        countLayout.setVisibility(View.GONE);
                    }
                    TextView count = (TextView) countLayout.findViewById(R.id.notification_count);
                    count.setText(mNotifications[index] > 99 ? "∞" : String
                            .valueOf(mNotifications[index]));
                }

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(mPosition == CHECK_UPDATES);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_check:
                mRomUpdater.check();
                mGappsUpdater.check();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // RomUpdaterListener methods
    @Override
    public void versionFound(PackageInfo[] info) {
        boolean checking = mRomUpdater.isScanning() || mGappsUpdater.isScanning();
        if (!checking) {
            setProgressBarVisibility(false);
        }
        if (info != null && info.length > 0) {
            if (!info[info.length - 1].isGapps()) {
                mNotificationHelper.setNotifications(info.length, NotificationHelper.NO_UPDATE);
            } else {
                mNotificationHelper.setNotifications(NotificationHelper.NO_UPDATE, info.length);
            }
        } else {
            mNotificationHelper.setNotifications(NotificationHelper.NO_UPDATE,
                    NotificationHelper.NO_UPDATE);
        }
    }

    @Override
    public void startChecking() {
        setProgressBarIndeterminate(true);
        setProgressBarVisibility(true);
    }

    // NotificationCallback methods
    @Override
    public void updateNotifications(int[] notifications) {
        mNotifications = notifications;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDownloadStarted() {
        onDownloadProgress(0);
    }

    @Override
    public void onDownloadError() {
        setProgressBarVisibility(false);
    }

    @Override
    public void onDownloadProgress(int progress) {
        if (progress < 0) {
            setProgressBarIndeterminate(true);
        } else {
            setProgressBarIndeterminate(false);
            setProgress(progress * 100);
        }
        setProgressBarVisibility(progress < 100);
    }

    @Override
    public void onDownloadFinished(Uri uri, final String md5, boolean isRom) {
        setProgressBarVisibility(false);
        if (uri != null) {
            String filePath = uri.toString().replace("file://", "");
            if (filePath == null || !filePath.endsWith(".zip")) {
                Toast.makeText(this, R.string.invalid_zip_file, Toast.LENGTH_SHORT).show();
                return;
            }
            if (md5 != null && !"".equals(md5)) {

                final ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setIndeterminate(true);
                pDialog.setMessage(getResources().getString(R.string.calculating_md5));
                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.show();

                final File file = new File(filePath);

                (new Thread() {

                    public void run() {

                        final String calculatedMd5 = IOUtils.md5(file);

                        pDialog.dismiss();

                        runOnUiThread(new Runnable() {

                            public void run() {
                                if (md5.equals(calculatedMd5)) {
                                    mRebootHelper.showRebootDialog(MainActivity.this,
                                            new String[] { mRecoveryHelper.getRecoveryFilePath(file
                                                    .getAbsolutePath()) });
                                } else {
                                    showMd5Mismatch(md5, calculatedMd5);
                                }
                            }
                        });
                    }
                }).start();
                
            } else {
                filePath = mRecoveryHelper.getRecoveryFilePath(filePath);
                mRebootHelper.showRebootDialog(this, new String[] { filePath });
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = null;

        switch (position) {
            case CHECK_UPDATES:
                if (mUpdateFragment == null) {
                    mUpdateFragment = new UpdateFragment();
                }
                mUpdateFragment.setUpdaters(mRomUpdater, mGappsUpdater);
                fragment = mUpdateFragment;
                break;
            case ROM:
                if (mRomFragment == null) {
                    mRomFragment = new RomFragment();
                }
                mRomFragment.setUpdater(mRomUpdater);
                fragment = mRomFragment;
                break;
            case GOOGLE_APPS:
                if (mGappsFragment == null) {
                    mGappsFragment = new GappsFragment();
                }
                mGappsFragment.setUpdater(mGappsUpdater);
                fragment = mGappsFragment;
                break;
            case CHANGELOG:
                if (mChangelogFragment == null) {
                    setProgressBarIndeterminate(true);
                    setProgressBarVisibility(true);
                    mChangelogFragment = new ChangelogFragment();
                }
                fragment = mChangelogFragment;
                break;
        }
        if (fragment != null)
            switchContent(fragment);

        mDrawerList.setItemChecked(position, true);
        setTitle(mSliderTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);

        mPosition = position;
        invalidateOptionsMenu();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void switchContent(final Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM, mPosition);
    }

    private void showMd5Mismatch(String md5, String calculated) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.md5_mismatch);
        alert.setMessage(getResources().getString(
                R.string.md5_mismatch_summary, new Object[] {md5, calculated}));
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}