/*
 * Copyright (C) 2019 The Calyx Institute
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.calyxos.setupwizard.apps;

import android.annotation.Nullable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

class AppItem {

    @Nullable
    final Drawable icon;
    @Nullable
    final CharSequence name;
    final String packageName;
    final String apkName;
    @Nullable
    final ArrayList<String> categories;
    @Nullable
    final String description;
    @Nullable
    final String summary;
    boolean checked;

    AppItem(@Nullable Drawable icon, @Nullable CharSequence name, String packageName, String apkName,
        ArrayList<String> categories, String description, String summary, boolean checked) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
        this.apkName = apkName;
        this.categories = categories;
        this.description = description;
        this.summary = summary;
        this.checked = checked;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof AppItem && packageName.equals(((AppItem) o).packageName);
    }

    boolean allEquals(AppItem o) {
        return equals(o) && checked == o.checked &&
                (name != null && name.equals(o.name) || name == null && o.name == null);
    }

}
