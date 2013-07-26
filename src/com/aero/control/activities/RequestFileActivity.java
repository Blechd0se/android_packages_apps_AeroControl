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

package com.aero.control.activities;

import java.io.Serializable;
import java.util.List;

import com.aero.control.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;

public class RequestFileActivity extends Activity {

    private static final int REQUEST_PICK_FILE = 203;

    public interface RequestFileCallback extends Serializable {

        public void fileRequested(String filePath);
    }

    private static RequestFileCallback sCallback;

    public static void setRequestFileCallback(RequestFileCallback callback) {
        sCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PackageManager packageManager = getPackageManager();
        Intent test = new Intent(Intent.ACTION_GET_CONTENT);
        test.setType("file/*");
        List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                PackageManager.GET_ACTIVITIES);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setType("file/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_FILE);
        } else {
            // No app installed to handle the intent - file explorer
            // required
            Toast.makeText(this, R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_FILE) {
            if (data == null) {
                // Nothing returned by user, probably pressed back button in
                // file manager
                finish();
                return;
            }

            String filePath = data.getData().getPath();

            if (sCallback != null) {
                sCallback.fileRequested(filePath);
            }

        }
        finish();
    }
}
