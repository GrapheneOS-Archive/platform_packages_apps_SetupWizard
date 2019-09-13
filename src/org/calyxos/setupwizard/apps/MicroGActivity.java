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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.calyxos.setupwizard.BaseSetupWizardActivity;
import org.calyxos.setupwizard.R;
import org.calyxos.setupwizard.apps.AppAdapter.AppItemListener;

import java.io.IOException;
import java.io.File;

import org.json.JSONException;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static org.calyxos.setupwizard.SetupWizardApp.FDROID_CATEGORY_DEFAULT_BACKEND;
import static org.calyxos.setupwizard.apps.AppInstallerService.APKS;
import static org.calyxos.setupwizard.apps.AppInstallerService.PATH;

public class MicroGActivity extends BaseSetupWizardActivity implements AppItemListener {

    public static final String TAG = MicroGActivity.class.getSimpleName();
    private static final String[] MICROG_PACKAGES = new String[]{
            "com.google.android.gms",
            "com.android.vending"
    };

    private static String path;

    private RecyclerView list;
    private AppAdapter adapter;

    private PackageManager pm;
    private Switch enableSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNextText(R.string.next);

        enableSwitch = findViewById(R.id.enableSwitch);
        findViewById(R.id.switchLayout).setOnClickListener(v -> enableSwitch.toggle());

        pm = getPackageManager();

        // This list is not shown to the user for now.
        list = findViewById(R.id.list);
        adapter = new AppAdapter(this);
        list.setAdapter(adapter);
        path = getString(R.string.calyx_fdroid_repo_location);

        getApps();
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.microg_activity;
    }

    @Override
    protected int getTitleResId() {
        return R.string.microg_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.microg_icon;
    }

    @Override
    public void onItemUnchecked() {
        // Do nothing.
    }

    @Override
    public void onNextPressed() {
        boolean enabled = enableSwitch.isChecked();
        for (String packageId : MICROG_PACKAGES) {
            setAppEnabled(packageId, enabled);
        }
        if (enabled) {
            Intent i = new Intent(this, AppInstallerService.class);
            i.putExtra(PATH, path);
            i.putStringArrayListExtra(APKS, adapter.getSelectedPackageNameAPKs());
            startForegroundService(i);
        }
        super.onNextPressed();
    }

    private void setAppEnabled(String packageName, boolean enabled) {
        int state = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        pm.setApplicationEnabledSetting(packageName, state, 0);
    }

    private void getApps() {
        File repoPath = new File(path);
        if (!repoPath.isDirectory()) {
            Log.e(TAG, "Local repo does not exist: " + repoPath);
            super.onNextPressed();
        } else {
            try {
                FDroidRepo.checkFdroidRepo(path);
            } catch (IOException | JSONException e) {
                super.onNextPressed();
            }
        }
        new Thread(() -> {
            FDroidRepo.loadFdroidJson(FDROID_CATEGORY_DEFAULT_BACKEND, path, list, adapter);
            list.post(() -> list.scrollToPosition(0));
        }).start();
    }
}
