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

package com.aero.control.fragments;

import com.aero.control.Utils;
import com.aero.control.updater.GappsUpdater;
import com.aero.control.updater.Updater.PackageInfo;
import com.aero.control.R;

public class GappsFragment extends PreferenceFragment {

    @Override
    public int getInfoIconResourceId() {
        return R.drawable.ic_info;
    }

    @Override
    public int getDownloadIconResourceId() {
        return R.drawable.ic_download;
    }

    @Override
    public int getOfflineIconResourceId() {
        return R.drawable.ic_offline;
    }

    @Override
    public int getOutdatedIconResourceId() {
        return R.string.gapps_outdated;
    }

    @Override
    public int getNoUpdatesIconResourceId() {
        return R.string.no_gapps_updates;
    }

    @Override
    public int getAlreadyDownloadingResourceId() {
        return R.string.already_downloading_gapps;
    }

    @Override
    public String getSummary() {
        GappsUpdater updater = (GappsUpdater)getUpdater();
        return Utils.getReadableVersion("pa_gapps-" + updater.getPlatform() + "-" + updater.getVersion());
    }

    @Override
    public String getPackageTitle(PackageInfo packageInfo) {
        return packageInfo.getFilename();
    }

    @Override
    public boolean isRom() {
        return false;
    }

}