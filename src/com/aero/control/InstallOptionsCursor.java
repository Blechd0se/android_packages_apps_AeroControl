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

package com.aero.control;

import com.aero.control.helpers.SettingsHelper;
import com.aero.control.R;

import android.content.Context;
import android.database.AbstractCursor;

public class InstallOptionsCursor extends AbstractCursor {

    private static final String[] COLUMN_NAMES = { "_id", "TEXT", "CHECKED" };

    private String[] mOption;
    private String[] mText;
    private int[] mChecked;
    private int mCount = 0;

    public InstallOptionsCursor(Context context) {
        SettingsHelper mSettingsHelper = new SettingsHelper(context);

        for (int i = 0; i < SettingsHelper.INSTALL_OPTIONS.length; i++) {
            if (mSettingsHelper.isShowOption(SettingsHelper.INSTALL_OPTIONS[i])) {
                mCount++;
            }
        }
        mOption = new String[mCount];
        mText = new String[mCount];
        mChecked = new int[mCount];
        int count = 0;
        for (int i = 0; i < SettingsHelper.INSTALL_OPTIONS.length; i++) {
            if (mSettingsHelper.isShowOption(SettingsHelper.INSTALL_OPTIONS[i])) {
                mOption[count] = SettingsHelper.INSTALL_OPTIONS[i];
                mText[count] = context.getResources().getString(
                        getText(SettingsHelper.INSTALL_OPTIONS[i]));
                mChecked[count] = 0;
                count++;
            }
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @Override
    public String getString(int column) {
        switch (column) {
            case 0:
                return mOption[getPosition()];
            case 1:
                return mText[getPosition()];
        }
        return null;
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        if (column == 2) {
            return mChecked[getPosition()];
        }
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }

    public void setOption(int which, boolean isChecked) {
        mChecked[which] = isChecked ? 1 : 0;
    }

    public boolean isWipeSystem() {
        return isOption("WIPESYSTEM");
    }

    public boolean isWipeData() {
        return isOption("WIPEDATA");
    }

    public boolean isWipeCaches() {
        return isOption("WIPECACHES");
    }

    public boolean isBackup() {
        return isOption("BACKUP");
    }

    public boolean hasWipeSystem() {
        return hasOption("WIPESYSTEM");
    }

    public boolean hasWipeData() {
        return hasOption("WIPEDATA");
    }

    public boolean hasWipeCaches() {
        return hasOption("WIPECACHES");
    }

    public boolean hasBackup() {
        return hasOption("BACKUP");
    }

    public String getIsCheckedColumn() {
        return "CHECKED";
    }

    public String getLabelColumn() {
        return "TEXT";
    }

    private boolean isOption(String option) {
        for (int i = 0; i < getCount(); i++) {
            if (option.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    private boolean hasOption(String option) {
        for (int i = 0; i < getCount(); i++) {
            if (option.equals(mOption[i])) {
                return true;
            }
        }
        return false;
    }

    private int getText(String option) {
        if (SettingsHelper.INSTALL_BACKUP.equals(option)) {
            return R.string.backup;
        } else if (SettingsHelper.INSTALL_WIPESYSTEM.equals(option)) {
            return R.string.wipe_system;
        } else if (SettingsHelper.INSTALL_WIPEDATA.equals(option)) {
            return R.string.wipe_data;
        } else if (SettingsHelper.INSTALL_WIPECACHES.equals(option)) {
            return R.string.wipe_caches;
        }
        return -1;
    }
}