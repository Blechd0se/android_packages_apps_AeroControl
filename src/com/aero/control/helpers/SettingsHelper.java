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

package com.aero.control.helpers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class SettingsHelper {

    // install options
    public static final String INSTALL_BACKUP = "BACKUP";
    public static final String INSTALL_WIPESYSTEM = "WIPESYSTEM";
    public static final String INSTALL_WIPEDATA = "WIPEDATA";
    public static final String INSTALL_WIPECACHES = "WIPECACHES";
    public static final String[] INSTALL_OPTIONS = { INSTALL_BACKUP, INSTALL_WIPESYSTEM,
            INSTALL_WIPEDATA, INSTALL_WIPECACHES };

    public static final String PROPERTY_EXPERT = "expertmode";
    public static final String PROPERTY_CHECK_TIME = "checktime";
    public static final String PROPERTY_DOWNLOAD_PATH = "downloadpath";
    public static final String PROPERTY_DOWNLOAD_FINISHED = "downloadfinished";
    public static final String PROPERTY_RECOVERY = "recovery";
    public static final String PROPERTY_INTERNAL_STORAGE = "internal-storage";
    public static final String PROPERTY_EXTERNAL_STORAGE = "external-storage";
    public static final String PROPERTY_SETTINGS_RECOVERY = "settings_recovery";
    public static final String PROPERTY_SHOW_OPTIONS = "showoptions";

    public static final String DOWNLOAD_ROM_ID = "download_rom_id";
    public static final String DOWNLOAD_GAPPS_ID = "download_gapps_id";
    public static final String DOWNLOAD_ROM_MD5 = "download_rom_md5";
    public static final String DOWNLOAD_GAPPS_MD5 = "download_gapps_md5";

    private static final boolean DEFAULT_EXPERT = false;
    private static final String DEFAULT_CHECK_TIME = "18000000"; // five hours
    private static final String DEFAULT_DOWNLOAD_PATH = new File(Environment
            .getExternalStorageDirectory(), "paranoidota/").getAbsolutePath();
    private static final boolean DEFAULT_DOWNLOAD_FINISHED = true;
    private static final String DEFAULT_RECOVERY = "cwmbased";
    private static final String DEFAULT_INTERNAL_STORAGE = "emmc";
    private static final String DEFAULT_EXTERNAL_STORAGE = "sdcard";
    private static final Set<String> DEFAULT_SHOW_OPTIONS = new HashSet<String>();

    private SharedPreferences settings;
    private Context mContext;

    public SettingsHelper(Context context) {
        mContext = context;

        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getExpertMode() {
        return settings.getBoolean(PROPERTY_EXPERT, DEFAULT_EXPERT);
    }

    public String getInternalStorage() {
        return settings.getString(PROPERTY_INTERNAL_STORAGE, DEFAULT_INTERNAL_STORAGE);
    }

    public void setInternalStorage(String value) {
        savePreference(PROPERTY_INTERNAL_STORAGE, value);
    }

    public String getExternalStorage() {
        return settings.getString(PROPERTY_EXTERNAL_STORAGE, DEFAULT_EXTERNAL_STORAGE);
    }

    public void setExternalStorage(String value) {
        savePreference(PROPERTY_EXTERNAL_STORAGE, value);
    }

    public boolean existsRecovery() {
        return settings.contains(PROPERTY_RECOVERY);
    }

    public String getRecovery() {
        return settings.getString(PROPERTY_RECOVERY, DEFAULT_RECOVERY);
    }

    public void setRecovery(String value) {
        savePreference(PROPERTY_RECOVERY, value);
    }

    public boolean isShowOption(String option) {
        Set<String> opts = settings.getStringSet(PROPERTY_SHOW_OPTIONS, DEFAULT_SHOW_OPTIONS);
        return opts.contains(option);
    }

    public Set<String> getShowOptions() {
        return settings.getStringSet(PROPERTY_SHOW_OPTIONS, DEFAULT_SHOW_OPTIONS);
    }

    public void setShowOptions(String options) {
        savePreference(PROPERTY_SHOW_OPTIONS, options);
    }

    public String getDownloadPath() {
        return settings.getString(PROPERTY_DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
    }

    public void setDownloadPath(String path) {
        savePreference(PROPERTY_DOWNLOAD_PATH, path);
    }

    public boolean getDownloadFinished() {
        return settings.getBoolean(PROPERTY_DOWNLOAD_FINISHED, DEFAULT_DOWNLOAD_FINISHED);
    }

    public long getCheckTime() {
        return Long.parseLong(settings.getString(PROPERTY_CHECK_TIME, DEFAULT_CHECK_TIME));
    }

    public void setDownloadRomId(Long id, String md5) {
        if (id == null) {
            removePreference(DOWNLOAD_ROM_ID);
            removePreference(DOWNLOAD_ROM_MD5);
        } else {
            savePreference(DOWNLOAD_ROM_ID, String.valueOf(id));
            savePreference(DOWNLOAD_ROM_MD5, md5);
        }
    }

    public long getDownloadRomId() {
        return Long.parseLong(settings.getString(DOWNLOAD_ROM_ID, "-1"));
    }

    public String getDownloadRomMd5() {
        return settings.getString(DOWNLOAD_ROM_MD5, null);
    }

    public void setDownloadGappsId(Long id, String md5) {
        if (id == null) {
            removePreference(DOWNLOAD_GAPPS_ID);
            removePreference(DOWNLOAD_GAPPS_MD5);
        } else {
            savePreference(DOWNLOAD_GAPPS_ID, String.valueOf(id));
            savePreference(DOWNLOAD_GAPPS_MD5, md5);
        }
    }

    public long getDownloadGappsId() {
        return Long.parseLong(settings.getString(DOWNLOAD_GAPPS_ID, "-1"));
    }

    public String getDownloadGappsMd5() {
        return settings.getString(DOWNLOAD_GAPPS_MD5, null);
    }

    private void savePreference(String preference, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(preference, value);
        editor.commit();
    }

    private void removePreference(String preference) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(preference);
        editor.commit();
    }
}
