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

import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationAlarm extends BroadcastReceiver {

    private RomUpdater mRomUpdater;
    private GappsUpdater mGappsUpdater;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mRomUpdater == null) {
            mRomUpdater = new RomUpdater(context, true);
        }
        if (mGappsUpdater == null) {
            mGappsUpdater = new GappsUpdater(context, true);
        }
        if (Utils.isNetworkAvailable(context)) {
            mRomUpdater.check();
            mGappsUpdater.check();
        }
    }
}