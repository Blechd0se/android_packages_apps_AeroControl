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

package com.paranoid.paranoidota.updater;

import java.io.Serializable;

import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class UpdatePackage implements PackageInfo, Serializable {

    private String md5 = null;
    private String incremental_md5 = null;
    private String filename = null;
    private String incremental_filename = null;
    private String path = null;
    private String size = null;
    private String incremental_path = null;
    private long version = -1;
    private boolean isDelta = false;
    private boolean isGapps = false;

    public UpdatePackage(String device, String name, long version, String size, String url,
            String md5, boolean gapps) {
        this.filename = name;
        this.version = version;
        this.size = size;
        this.path = url;
        this.md5 = md5;
        this.isGapps = gapps;
    }

    @Override
    public boolean isDelta() {
        return isDelta;
    }

    @Override
    public String getDeltaFilename() {
        return incremental_filename;
    }

    @Override
    public String getDeltaPath() {
        return incremental_path;
    }

    @Override
    public String getDeltaMd5() {
        return incremental_md5;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public boolean isGapps() {
        return isGapps;
    }
}