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

package com.paranoid.paranoidota.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.paranoid.paranoidota.DirectoryChooserDialog;
import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.RecoveryHelper;
import com.paranoid.paranoidota.helpers.RecoveryHelper.RecoveryInfo;
import com.paranoid.paranoidota.helpers.SettingsHelper;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private SettingsHelper mSettingsHelper;
    private RecoveryHelper mRecoveryHelper;
    private CheckBoxPreference mExpertMode;
    private ListPreference mCheckTime;
    private Preference mDownloadPath;
    private CheckBoxPreference mDownloadFinished;
    private PreferenceCategory mRecoveryCategory;
    private Preference mRecovery;
    private Preference mInternalSdcard;
    private Preference mExternalSdcard;
    private MultiSelectListPreference mOptions;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        mSettingsHelper = new SettingsHelper(this);
        mRecoveryHelper = new RecoveryHelper(this);

        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.layout.activity_settings);

        mExpertMode = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_EXPERT);
        mCheckTime = (ListPreference) findPreference(SettingsHelper.PROPERTY_CHECK_TIME);
        mDownloadPath = findPreference(SettingsHelper.PROPERTY_DOWNLOAD_PATH);
        mDownloadFinished = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_DOWNLOAD_FINISHED);
        mRecovery = findPreference(SettingsHelper.PROPERTY_RECOVERY);
        mInternalSdcard = findPreference(SettingsHelper.PROPERTY_INTERNAL_STORAGE);
        mExternalSdcard = findPreference(SettingsHelper.PROPERTY_EXTERNAL_STORAGE);
        mRecoveryCategory = (PreferenceCategory) findPreference(SettingsHelper.PROPERTY_SETTINGS_RECOVERY);
        mOptions = (MultiSelectListPreference) findPreference(SettingsHelper.PROPERTY_SHOW_OPTIONS);

        if (!IOUtils.hasSecondarySdCard()) {
            mRecoveryCategory.removePreference(mExternalSdcard);
        }

        mExpertMode.setDefaultValue(mSettingsHelper.getExpertMode());
        mCheckTime.setValue(String.valueOf(mSettingsHelper.getCheckTime()));
        mDownloadFinished.setChecked(mSettingsHelper.getDownloadFinished());
        mOptions.setDefaultValue(mSettingsHelper.getShowOptions());

        updateSummaries();
        addOrRemovePreferences();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        if (SettingsHelper.PROPERTY_DOWNLOAD_PATH.equals(key)) {
            selectDownloadPath();
        } else if (SettingsHelper.PROPERTY_RECOVERY.equals(key)) {
            mRecoveryHelper.selectRecovery();
        } else if (SettingsHelper.PROPERTY_INTERNAL_STORAGE.equals(key)) {
            mRecoveryHelper.selectSdcard(true);
        } else if (SettingsHelper.PROPERTY_EXTERNAL_STORAGE.equals(key)) {
            mRecoveryHelper.selectSdcard(false);
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsHelper.PROPERTY_EXPERT.equals(key)) {
            addOrRemovePreferences();
        } else if (SettingsHelper.PROPERTY_CHECK_TIME.equals(key)) {
            Utils.setAlarm(this, mSettingsHelper.getCheckTime(), false);
        }

        updateSummaries();
    }

    private void updateSummaries() {
        mDownloadPath.setSummary(mSettingsHelper.getDownloadPath());
        RecoveryInfo info = mRecoveryHelper.getRecovery();
        mRecovery.setSummary(getResources().getText(R.string.settings_selectrecovery_summary)
                + " (" + info.getName() + ")");
        mInternalSdcard.setSummary(getResources().getText(R.string.settings_internalsdcard_summary)
                + " (" + mSettingsHelper.getInternalStorage() + ")");
        mExternalSdcard.setSummary(getResources().getText(R.string.settings_externalsdcard_summary)
                + " (" + mSettingsHelper.getExternalStorage() + ")");
    }

    @SuppressWarnings("deprecation")
    private void addOrRemovePreferences() {
        boolean expert = mSettingsHelper.getExpertMode();
        if (expert) {
            getPreferenceScreen().addPreference(mRecoveryCategory);
        } else {
            getPreferenceScreen().removePreference(mRecoveryCategory);
        }
    }

    private void selectDownloadPath() {
        new DirectoryChooserDialog(this, new DirectoryChooserDialog.DirectoryChooserListener() {

            @Override
            public void onDirectoryChosen(String chosenDir) {
                mSettingsHelper.setDownloadPath(chosenDir);
                updateSummaries();
            }
        }).chooseDirectory();
    }
}