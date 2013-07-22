/*
 * Copyright (C) 2012 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.paranoid.paranoidota;

import java.util.ArrayList;

public class ListItems {

    public ArrayList<PreferenceItem> ITEMS = new ArrayList<PreferenceItem>();

    public ListItems() {
        addItem(new PreferenceItem(R.string.slider_updates, R.drawable.slider_check));
        addItem(new PreferenceItem(R.string.slider_rom, R.drawable.slider_rom));
        addItem(new PreferenceItem(R.string.slider_google_apps, R.drawable.slider_gapps));
        addItem(new PreferenceItem(R.string.slider_changelog, R.drawable.slider_changelog));
    }

    public static class PreferenceItem {
        public int content;
        public int drawable;

        public PreferenceItem(int content, int drawable) {
            this.content = content;
            this.drawable = drawable;
        }
    }

    private void addItem(PreferenceItem item) {
        ITEMS.add(item);
    }
}