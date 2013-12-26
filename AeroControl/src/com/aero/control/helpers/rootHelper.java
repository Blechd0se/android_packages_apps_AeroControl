package com.aero.control.helpers;

import java.io.File;

/**
 * Created by Alexander Christ on 26.12.13.
 */

public class rootHelper {

        private String LOG_TAG = rootHelper.class.getName();

        public boolean isDeviceRooted() {
            return checkRootMethod1() || checkRootMethod2();
        }

        public boolean checkRootMethod1() {
            try {
                File file = new File("/system/app/Superuser.apk");
                return file.exists();
            } catch (Exception e) {
                return false;
            }

        }

        public boolean checkRootMethod2() {
            try {
                File file = new File("/system/xbin/su");
                return file.exists();
            } catch (Exception e) {
                return false;
            }
        }

}
