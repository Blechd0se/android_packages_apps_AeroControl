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

public class NotificationHelper {

    public static final int NO_UPDATE = -1;

    private NotificationCallback mCallback;
    private int mRomNotifications;
    private int mGappsNotifications;

    public interface NotificationCallback {
        public abstract void updateNotifications(int[] notifications);
    }

    public NotificationHelper(NotificationCallback callback) {
        mCallback = callback;
    }

    public void setNotifications(int rom, int gapps) {
        if(rom == NO_UPDATE) {
            rom = mRomNotifications;
        }
        if(gapps == NO_UPDATE) {
           gapps = mGappsNotifications;
        }
        mRomNotifications = rom;
        mGappsNotifications = gapps;
        mCallback.updateNotifications(new int[]{rom, gapps});
    }
}
