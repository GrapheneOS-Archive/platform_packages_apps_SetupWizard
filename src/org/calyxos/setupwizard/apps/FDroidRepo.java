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

import android.annotation.WorkerThread;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import org.calyxos.setupwizard.util.IOUtils;
import org.calyxos.setupwizard.util.FDroidUtils;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.calyxos.setupwizard.SetupWizardApp.FDROID_CATEGORY_DEFAULT;
import static org.calyxos.setupwizard.SetupWizardApp.FDROID_CATEGORY_DEFAULT_BACKEND;

public class FDroidRepo {

    public static final String TAG = FDroidRepo.class.getSimpleName();

    @WorkerThread
    public static void loadFdroidJson(String category, String path, RecyclerView list, AppAdapter adapter) {
        try {
            File index = new File(path + "/index-v1.json");
            JSONObject obj = new JSONObject(new String(IOUtils.toByteArray(index)));
            JSONArray apps = obj.getJSONArray("apps");
            JSONObject packages = obj.getJSONObject("packages");
            for (int i = 0; i < apps.length(); i++) {
                addApp(category, path, apps.getJSONObject(i), packages, list, adapter);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to local repo index-v1.json" + e);
        }
    }

    @WorkerThread
    public static void addApp(String category, String path, JSONObject app, JSONObject packages, RecyclerView list, AppAdapter adapter) {
        try {
            JSONObject apk = packages.getJSONArray(app.getString("packageName")).getJSONObject(0);

            boolean checked = false;
            JSONArray categories_j = app.getJSONArray("categories");
            ArrayList<String> categories = new ArrayList<>();
            for (int j = 0; j < categories_j.length(); j++) {
                categories.add(categories_j.getString(j));
            }

            if (category.equals(FDROID_CATEGORY_DEFAULT)) {
                // Install Default apps by default
                if (categories.contains(category)) checked = true;
                // Skip apps which are microG UnifiedNlp backend providers
                else if (categories.contains(FDROID_CATEGORY_DEFAULT_BACKEND)) return;
                // This means that apps not "Default" still get shown
                // but are unchecked and hence not installed by default.
            } else {
                // Skip all other apps for any category other than Default
                if (!categories.contains(category)) return;
                else checked = true;
            }

            String iconPath = app.getString("icon");
            // TODO: Avoid using hardcoded density
            Drawable icon = Drawable.createFromPath(path + "/icons-640/" + iconPath);
            CharSequence name = app.getString("name");
            String packageName = app.getString("packageName");
            String apkName = apk.getString("apkName");
            String description = "";
            if (app.has("description")) {
                description = FDroidUtils.formatDescription(app.getString("description"));
            }
            String summary = "";
            if (app.has("summary")) {
                summary = app.getString("summary");
            }
            if (app.has("localized")) {
                JSONObject localized = app.getJSONObject("localized");
                Set<String> localesToUse = FDroidUtils.getlocalesToUse(localized);
                String value = FDroidUtils.getLocalizedEntry(localized, localesToUse, "name");
                if (!TextUtils.isEmpty(value)) name = value;
                value = FDroidUtils.getLocalizedEntry(localized, localesToUse, "description");
                if (!TextUtils.isEmpty(value)) description = FDroidUtils.formatDescription(value);
                value = FDroidUtils.getLocalizedEntry(localized, localesToUse, "summary");
                if (!TextUtils.isEmpty(value)) summary = value;
            }

            AppItem item = new AppItem(icon, name, packageName, apkName,
                categories, description, summary, checked);
            list.post(() -> adapter.addItem(item));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add app" + e);
        }
    }
}