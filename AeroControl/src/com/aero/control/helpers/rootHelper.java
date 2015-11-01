package com.aero.control.helpers;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Alexander Christ on 26.12.13.
 */

public class rootHelper {

    private static final String LOG_TAG = rootHelper.class.getName();
    private final static String NO_DATA_FOUND = "Unavailable";
    private static final int BUFF_LEN = 1024;
    private static final byte[] buffer = new byte[BUFF_LEN];

        public boolean isDeviceRooted() {
            return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
        }

        private boolean checkRootMethod1() {
            try {
                File file = new File("/system/app/Superuser.apk");
                return file.exists();
            } catch (Exception e) {
                Log.e(LOG_TAG, "An Error occured while checking for Superuser.apk.", e);
                return false;
            }

        }

        private boolean checkRootMethod2() {
            try {
                File file = new File("/system/xbin/su");
                return file.exists();
            } catch (Exception e) {
                Log.e(LOG_TAG, "An Error occured while checking for su.", e);
                return false;
            }
        }

        private boolean checkRootMethod3() {

            String output = suCheckRootMethod();

            if (output.equals(NO_DATA_FOUND)) {
                return false;
            }

            // Return true if we are rooted;
            return output.contains("uid=0");

        }

        private String suCheckRootMethod() {
            Process rooting = null;
            try {
                rooting = Runtime.getRuntime().exec("su");

                DataOutputStream stdin = new DataOutputStream(rooting.getOutputStream());

                stdin.writeBytes("id" + "\n");
                InputStream stdout = rooting.getInputStream();
                int read;
                String output = "";
                while(true){
                    read = stdout.read(buffer);
                    if (read == -1)
                        return NO_DATA_FOUND;

                    output += new String(buffer, 0, read);
                    if(read<BUFF_LEN){
                        //we have read everything
                        break;
                    }
                }
                stdin.writeBytes("exit\n");
                return output;

            } catch (IOException e) {
                Log.e(LOG_TAG, "Do you even root, bro? :/", e);
            } finally {
                if (rooting != null) {
                    try {
                        rooting.waitFor();
                    } catch (InterruptedException e) {}
                    rooting.destroy();
                }
            }

            return NO_DATA_FOUND;
        }

}
