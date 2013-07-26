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

package com.aero.control.updater;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.aero.control.Utils;
import com.aero.control.http.URLStringReader;
import com.aero.control.R;

public class GappsUpdater extends Updater {

    private static final String URL_GAPPS = "http://api.paranoidandroid.co/updates/gapps?v=%s";

    private String mPlatform;
    private long mVersion = -1L;
    private boolean mCanUpdate;
    private boolean mFromAlarm;
    private boolean mScanning;

    public GappsUpdater(Context context, boolean fromAlarm) {
        super(context);
        mFromAlarm = fromAlarm;

        File file = new File("/system/etc/g.prop");
        mCanUpdate = file.exists();
        if (mCanUpdate) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(file));
                String versionProperty = "ro.addon.pa_version";
                String versionString = properties.getProperty(versionProperty);
                if (versionString == null || "".equals(versionString) || versionProperty == null
                        || "".equals(versionProperty)) {
                    versionProperty = "ro.addon.version";
                    versionString = properties.getProperty(versionProperty);
                }
                mPlatform = Utils.getProp("ro.build.version.release");
                mPlatform = mPlatform.replace(".", "");
                if (mPlatform.length() > 2) {
                    mPlatform = mPlatform.substring(0, 2);
                }
                if (versionString == null || "".equals(versionString)) {
                    mCanUpdate = false;
                } else {
                    String[] version = versionString.split("-");
                    for (int i = 0; i < version.length; i++) {
                        try {
                            mVersion = Long.parseLong(version[i]);
                            break;
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                mCanUpdate = false;
            }
        }
    }

    @Override
    public void onReadEnd(String buffer) {
        mScanning = false;
        try {
            PackageInfo[] lastGapps = null;
            setLastUpdates(null);
            String error = null;
            List<PackageInfo> packagesList = new ArrayList<PackageInfo>();
            if (buffer != null && !buffer.isEmpty()) {
                JSONObject updateInfo = new JSONObject(buffer);
                error = updateInfo.optString("error");
                if (error == null || error.isEmpty()) {
                    JSONArray updates = updateInfo.getJSONArray("updates");
                    for (int i = 0; i < updates.length(); i++) {
                        JSONObject update = updates.getJSONObject(i);
                        packagesList.add(new UpdatePackage("gapps", update.getString("name"),
                                update.getLong("version"), update.getString("size"), update
                                        .getString("url"), update.getString("md5"), true));
                    }
                }
            }
            lastGapps = packagesList.toArray(new PackageInfo[packagesList.size()]);
            if (lastGapps.length > 0) {
                if (mFromAlarm) {
                    Utils.showNotification(getContext(), lastGapps, GAPPS_NOTIFICATION_ID,
                            R.string.new_gapps_found_title);
                }
            } else {
                if (error != null && !error.isEmpty()) {
                    versionError(error);
                } else {
                    if (!mFromAlarm) {
                        Utils.showToastOnUiThread(getContext(), R.string.check_gapps_updates_no_new);
                    }
                }
            }
            setLastUpdates(lastGapps);
            fireCheckCompleted(lastGapps);
        } catch (Exception ex) {
            System.out.println(buffer);
            onReadError(ex);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        ex.printStackTrace();
        versionError(null);
    }

    private void versionError(String error) {
        mScanning = false;
        if (!mFromAlarm) {
            if (error != null) {
                Utils.showToastOnUiThread(getContext(), R.string.check_gapps_updates_error + ": "
                        + error);
            } else {
                Utils.showToastOnUiThread(getContext(), R.string.check_gapps_updates_error);
            }
        }
        fireCheckCompleted(null);
    }

    public String getPlatform() {
        return mPlatform;
    }

    @Override
    public long getVersion() {
        return mVersion;
    }

    @Override
    public void check() {
        mScanning = true;
        fireStartChecking();
        new URLStringReader(this).execute(String.format(URL_GAPPS, new Object[] {
                getPlatform() + getVersion() }));
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

}
