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

package com.paranoid.paranoidota;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

import com.paranoid.paranoidota.helpers.SettingsHelper;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class IOUtils {

    private static final String PREFIX = "pa_";
    private static final String SUFFIX = ".zip";

    private static SettingsHelper sSettingsHelper;
    private static String sPrimarySdcard;
    private static String sSecondarySdcard;
    private static boolean sSdcardsChecked;

    public static String[] getDownloadList(Context context) {
        File downloads = initSettingsHelper(context);
        ArrayList<String> list = new ArrayList<String>();
        for(File f : downloads.listFiles()) {
            if(isRom(f.getName())) {
                list.add(f.getName());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] getDownloadSizes(Context context) {
        File downloads = initSettingsHelper(context);
        ArrayList<String> list = new ArrayList<String>();
        for(File f : downloads.listFiles()) {
            if(isRom(f.getName())) {
                list.add(humanReadableByteCount(f.length(), false));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static boolean isOnDownloadList(Context context, String fileName) {
        for(String file : getDownloadList(context)) {
            if(fileName.equals(file)) return true;
        }
        return false;
    }

    public static boolean isRom(String name) {
        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isInSecondaryStorage(String path) {
        return !path.startsWith(sPrimarySdcard) && !path.startsWith("/sdcard")
                && !path.startsWith("/mnt/sdcard");
    }

    public static boolean hasSecondarySdCard() {
        readMounts();
        return sSecondarySdcard != null;
    }

    public static String getPrimarySdCard() {
        readMounts();
        return sPrimarySdcard;
    }

    public static String getSecondarySdCard() {
        readMounts();
        return sSecondarySdcard;
    }

    private static void readMounts() {
        if(sSdcardsChecked) {
            return;
        }

        ArrayList<String> mounts = new ArrayList<String>();
        ArrayList<String> vold = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    mounts.add(element);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mounts.size() == 0 && isExternalStorageAvailable()) {
            mounts.add("/mnt/sdcard");
        }
        try {
            Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("dev_mount")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    if (element.toLowerCase().indexOf("usb") < 0) {
                        vold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (vold.size() == 0 && isExternalStorageAvailable()) {
            vold.add("/mnt/sdcard");
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            File root = new File(mount);
            if (!vold.contains(mount)
                    || (!root.exists() || !root.isDirectory() || !root.canWrite())) {
                mounts.remove(i--);
            }
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (mount.indexOf("sdcard0") < 0 && !mount.equalsIgnoreCase("/mnt/sdcard")
                    && !mount.equalsIgnoreCase("/sdcard")) {
                sSecondarySdcard = mount;
            } else {
                sPrimarySdcard = mount;
            }
        }

        if (sPrimarySdcard == null) {
            sPrimarySdcard = "/sdcard";
        }

        sSdcardsChecked = true;
    }

    public static double getSpaceLeft() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        return sdAvailSize / 1073741824;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMG" : "KMG").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre).replace(",", ".");
    }

    public static String md5(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    private static File initSettingsHelper(Context context) {
        if (sSettingsHelper == null) {
            sSettingsHelper = new SettingsHelper(context);
        }
        File downloads = new File(sSettingsHelper.getDownloadPath());
        downloads.mkdirs();
        return downloads;
    }
}
