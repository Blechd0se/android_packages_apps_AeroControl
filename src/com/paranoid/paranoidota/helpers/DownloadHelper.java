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

package com.paranoid.paranoidota.helpers;

import java.io.File;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.paranoid.paranoidota.R;

public class DownloadHelper {

    private static Context sContext;
    private static SettingsHelper sSettingsHelper;
    private static Handler sUpdateHandler = new Handler();

    private static DownloadManager sDownloadManager;
    private static DownloadCallback sCallback;

    private static boolean sDownloadingRom = false;
    private static boolean sDownloadingGapps = false;

    public static interface DownloadCallback {

        public abstract void onDownloadStarted();

        public abstract void onDownloadProgress(int progress);

        public abstract void onDownloadFinished(Uri uri, String md5, boolean isRom);

        public abstract void onDownloadError();
    }

    private static Runnable sUpdateProgress = new Runnable() {
        public void run() {
            if (!sDownloadingRom && !sDownloadingGapps) {
                return;
            }

            long idRom = sSettingsHelper.getDownloadRomId();
            long idGapps = sSettingsHelper.getDownloadGappsId();

            long[] statusRom = sDownloadingRom ? getDownloadProgress(idRom, true) : new long[] {
                    DownloadManager.STATUS_SUCCESSFUL,
                    0,
                    0 };
            long[] statusGapps = sDownloadingGapps ? getDownloadProgress(idGapps, false) : new long[] {
                    DownloadManager.STATUS_SUCCESSFUL,
                    0,
                    0 };

            int status = DownloadManager.STATUS_SUCCESSFUL;
            if (statusRom[0] == DownloadManager.STATUS_FAILED && statusGapps[0] == DownloadManager.STATUS_FAILED) {
                status = DownloadManager.STATUS_FAILED;
            } else if (statusRom[0] == DownloadManager.STATUS_PENDING && statusGapps[0] == DownloadManager.STATUS_PENDING) {
                status = DownloadManager.STATUS_PENDING;
            }

            switch (status) {
                case DownloadManager.STATUS_PENDING:
                    sCallback.onDownloadProgress(-1);
                    break;
                case DownloadManager.STATUS_FAILED:
                    sCallback.onDownloadError();
                    break;
                default:
                    long totalBytes = statusRom[1] + statusGapps[1];
                    long downloadedBytes = statusRom[2] + statusGapps[2];
                    long percent = totalBytes == -1 && downloadedBytes == -1 ? -1 : downloadedBytes
                            * 100 / totalBytes;
                    if (totalBytes != -1 && downloadedBytes != -1 && percent != -1) {
                        sCallback.onDownloadProgress((int)percent);
                    }
                    break;
            }

            if (status != DownloadManager.STATUS_FAILED) {
                sUpdateHandler.postDelayed(this, 1000);
            }
        }
    };

    public static void registerCallback(Context context, DownloadCallback callback) {
        sContext = context;
        sCallback = callback;
        if (sDownloadManager == null) {
            sDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        sSettingsHelper = new SettingsHelper(sContext);
        sDownloadingRom = sSettingsHelper.getDownloadRomId() >= 0;
        sDownloadingGapps = sSettingsHelper.getDownloadGappsId() >= 0;
        sUpdateHandler.post(sUpdateProgress);
    }

    public static void unregisterCallback() {
        sUpdateHandler.removeCallbacks(sUpdateProgress);
    }

    public static void checkDownloadFinished(Context context) {
        sContext = context;
        if (sDownloadManager == null) {
            sDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        sSettingsHelper = new SettingsHelper(sContext);
        checkDownloadFinished(true);
        checkDownloadFinished(false);
    }

    private static void checkDownloadFinished(boolean isRom) {
        long id = isRom ? sSettingsHelper.getDownloadRomId() : sSettingsHelper.getDownloadGappsId();
        if (id == -1L) {
            return;
        }
        String md5 = isRom ? sSettingsHelper.getDownloadRomMd5() : sSettingsHelper.getDownloadGappsMd5();
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = sDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);

            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    sCallback.onDownloadError();
                    removeDownload(id, isRom, true);
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    String uriString = cursor.getString(cursor
                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    sCallback.onDownloadFinished(Uri.parse(uriString), md5, isRom);
                    removeDownload(id, isRom, false);
                    break;
                default:
                    cancelDownload(id, isRom);
                    break;
            }
        } else {
            removeDownload(id, isRom, true);
        }
    }

    public static boolean isDownloading(boolean rom) {
        if (rom) {
            return sDownloadingRom;
        } else {
            return sDownloadingGapps;
        }
    }

    public static void downloadFile(String url, String fileName, String md5, boolean isRom) {

        sUpdateHandler.post(sUpdateProgress);
        sCallback.onDownloadStarted();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(sContext.getResources().getString(R.string.download_title,
                new Object[] { fileName }));
        File file = new File(sSettingsHelper.getDownloadPath());
        if (!file.exists()) {
            file.mkdirs();
        }
        request.setDestinationUri(Uri.fromFile(new File(sSettingsHelper.getDownloadPath(), fileName)));

        long id = sDownloadManager.enqueue(request);
        if (isRom) {
            sDownloadingRom = true;
            sSettingsHelper.setDownloadRomId(id, md5);
        } else {
            sDownloadingGapps = true;
            sSettingsHelper.setDownloadGappsId(id, md5);
        }
    }

    private static void removeDownload(long id, boolean isRom, boolean removeDownload) {
        if (isRom) {
            sDownloadingRom = false;
            sSettingsHelper.setDownloadRomId(null, null);
        } else {
            sDownloadingGapps = false;
            sSettingsHelper.setDownloadGappsId(null, null);
        }
        if (removeDownload) {
            sDownloadManager.remove(id);
        }
        sUpdateHandler.removeCallbacks(sUpdateProgress);
        sCallback.onDownloadFinished(null, null, isRom);
    }

    private static void cancelDownload(final long id, final boolean isRom) {
        new AlertDialog.Builder(sContext)
                .setTitle(R.string.cancel_download_alert_title)
                .setMessage(R.string.cancel_download_alert_summary)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeDownload(id, isRom, true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private static long[] getDownloadProgress(long id, boolean isRom) {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(id);

        Cursor cursor = sDownloadManager.query(q);
        int status;

        if (cursor == null || !cursor.moveToFirst()) {
            status = DownloadManager.STATUS_FAILED;
        } else {
            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }

        long totalBytes = -1;
        long downloadedBytes = -1;

        switch (status) {
            case DownloadManager.STATUS_PAUSED:
            case DownloadManager.STATUS_RUNNING:
                downloadedBytes = cursor.getLong(
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                totalBytes = cursor.getLong(
                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                break;
            case DownloadManager.STATUS_FAILED:
                if (isRom) {
                    sDownloadingRom = false;
                } else {
                    sDownloadingGapps = false;
                }
                break;
        }

        if (cursor != null) {
            cursor.close();
        }

        return new long[] {status, totalBytes, downloadedBytes};
    }
}
