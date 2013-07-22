/*
 * Copyright (C) 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.paranoidota.fragments;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.MainActivity;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.helpers.DownloadHelper;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.updater.Updater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;
import com.paranoid.paranoidota.updater.Updater.UpdaterListener;

public abstract class PreferenceFragment extends android.preference.PreferenceFragment implements UpdaterListener {

    private Context mContext;
    private PreferenceScreen mRoot;
    private Updater mUpdater;
    private PackageInfo[] mPackages;

    Preference.OnPreferenceClickListener mDownloadListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (DownloadHelper.isDownloading(isRom())) {
                Toast.makeText(mContext, getAlreadyDownloadingResourceId(), Toast.LENGTH_LONG)
                        .show();
                return false;
            }
            int index = Integer.parseInt(preference.getKey());
            PackageInfo info = mPackages[index];
            DownloadHelper.downloadFile(info.getPath(), info.getFilename(), info.getMd5(), isRom());
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(R.string.download_title,
                            new Object[] { info.getFilename() }), Toast.LENGTH_LONG).show();
            return false;
        }
    };

    Preference.OnPreferenceClickListener mDownloadedListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            int index = Integer.parseInt(preference.getKey());
            PackageInfo info = mPackages[index];
            File file = new File(new SettingsHelper(mContext).getDownloadPath(), info.getFilename());
            ((MainActivity) getActivity()).onDownloadFinished(Uri.fromFile(file), null, isRom());
            return false;
        }
    };

    public void setUpdater(Updater updater) {
        mUpdater = updater;
        mUpdater.addUpdaterListener(this);
        updateScreen(mUpdater.getLastUpdates());
    }

    public Updater getUpdater() {
        return mUpdater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mRoot = getPreferenceManager().createPreferenceScreen(mContext);
        setPreferenceScreen(mRoot);

        if (mUpdater != null) {
            updateScreen(mUpdater.getLastUpdates());
        }
    }

    @Override
    public void versionFound(PackageInfo[] roms) {
        updateScreen(roms);
    }

    @Override
    public void startChecking() {
        updateScreen(null);
    }

    private void updateScreen(PackageInfo[] packages) {
        if (mRoot == null) {
            return;
        }

        mRoot.removeAll();

        mPackages = packages;

        Preference info = new Preference(mContext);
        info.setSummary(getSummary());
        info.setIcon(getInfoIconResourceId());
        info.setSelectable(false);
        mRoot.addPreference(info);

        if(packages != null && packages.length > 0) {
            for(int i = 0; i<packages.length; i++) {
                final Preference pref = new Preference(mContext);
                pref.setTitle(getPackageTitle(packages[i]));
                pref.setSummary(packages[i].getSize());
                pref.setKey(String.valueOf(i));
                if(IOUtils.isOnDownloadList(mContext, packages[i].getFilename())) {
                    pref.setIcon(getOfflineIconResourceId());
                    pref.setOnPreferenceClickListener(mDownloadedListener);
                } else {
                    pref.setIcon(getDownloadIconResourceId());
                    pref.setOnPreferenceClickListener(mDownloadListener);
                }
                mRoot.addPreference(pref);
            }
            info.setTitle(getOutdatedIconResourceId());
        } else {
            info.setTitle(getNoUpdatesIconResourceId());
        }
    }

    public abstract int getInfoIconResourceId();

    public abstract int getDownloadIconResourceId();

    public abstract int getOfflineIconResourceId();

    public abstract int getOutdatedIconResourceId();

    public abstract int getNoUpdatesIconResourceId();

    public abstract int getAlreadyDownloadingResourceId();

    public abstract String getSummary();

    public abstract String getPackageTitle(PackageInfo packageInfo);

    public abstract boolean isRom();
}