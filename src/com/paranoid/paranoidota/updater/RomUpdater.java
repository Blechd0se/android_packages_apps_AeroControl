/*
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

package com.paranoid.paranoidota.updater;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.http.URLStringReader;

public class RomUpdater extends Updater {

    public static final String PROPERTY_VERSION = "ro.modversion";

    private static final String URL = "http://api.paranoidandroid.co/updates/%s?v=%s";

    private boolean mScanning = false;
    private boolean mFromAlarm;

    public RomUpdater(Context context, boolean fromAlarm) {
        super(context);
        mFromAlarm = fromAlarm;
    }

    @Override
    public void check() {
        mScanning = true;
        fireStartChecking();
        new URLStringReader(this).execute(String.format(URL, new Object[] {
                getDevice(),
                getVersion() }));
    }

    @Override
    public long getVersion() {
        String version = Utils.getProp(PROPERTY_VERSION);
        String stripped = version.replaceAll("\\D+", "");
        return Long.parseLong(stripped);
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    private String getDevice() {
        return Utils.getProp(PROPERTY_DEVICE);
    }

    @Override
    public void onReadEnd(String buffer) {
        try {
            mScanning = false;
            PackageInfo[] lastRoms = null;
            setLastUpdates(null);
            String error = null;
            List<PackageInfo> list = new ArrayList<PackageInfo>();
            if (buffer != null && !buffer.isEmpty()) {
                JSONObject updateInfo = new JSONObject(buffer);
                error = updateInfo.optString("error");
                if (error == null || error.isEmpty()) {
                    JSONArray updates = updateInfo.getJSONArray("updates");
                    for (int i = 0; i < updates.length(); i++) {
                        JSONObject update = updates.getJSONObject(i);
                        list.add(new UpdatePackage(getDevice(), update.getString("name"), update
                                .getLong("version"), update.getString("size"), update
                                .getString("url"), update.getString("md5"), false));
                    }
                }
            }
            lastRoms = list.toArray(new PackageInfo[list.size()]);
            if (list.size() > 0) {
                if (mFromAlarm) {
                    Utils.showNotification(getContext(), lastRoms, ROM_NOTIFICATION_ID,
                            R.string.new_rom_found_title);
                }
            } else {
                if (error != null && !error.isEmpty()) {
                    versionError(error);
                } else {
                    if (!mFromAlarm) {
                        Utils.showToastOnUiThread(getContext(), R.string.check_rom_updates_no_new);
                    }
                }
            }
            setLastUpdates(lastRoms);
            fireCheckCompleted(lastRoms);
        } catch (Exception ex) {
            mScanning = false;
            ex.printStackTrace();
            versionError(null);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        mScanning = false;
        versionError(null);
    }

    private void versionError(String error) {
        if (!mFromAlarm) {
            if (error != null) {
                Utils.showToastOnUiThread(getContext(),
                        getContext().getResources().getString(R.string.check_rom_updates_error)
                                + ": " + error);
            } else {
                Utils.showToastOnUiThread(getContext(), R.string.check_rom_updates_error);
            }
        }
        fireCheckCompleted(null);
    }
}
