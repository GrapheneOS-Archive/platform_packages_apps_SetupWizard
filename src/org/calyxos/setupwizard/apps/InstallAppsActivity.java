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
import android.annotation.WorkerThread;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import androidx.recyclerview.widget.RecyclerView;

import org.calyxos.setupwizard.BaseSetupWizardActivity;
import org.calyxos.setupwizard.R;
import org.calyxos.setupwizard.apps.AppAdapter.AppItemListener;

import java.io.File;
import java.io.FilenameFilter;

import static org.calyxos.setupwizard.apps.AppInstallerService.PACKAGE_PATHS;

public class InstallAppsActivity extends BaseSetupWizardActivity implements AppItemListener {

    public static final String TAG = InstallAppsActivity.class.getSimpleName();

    private RecyclerView list;
    private AppAdapter adapter;
    private CheckBox checkBoxAll;
    private PackageManager pm;
    private boolean appUnchecked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNextText(R.string.next);

        list = findViewById(R.id.list);
        adapter = new AppAdapter(this);
        list.setAdapter(adapter);

        checkBoxAll = findViewById(R.id.checkBoxAll);
        checkBoxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) adapter.setAllChecked(true);
            else if (!appUnchecked) adapter.setAllChecked(false);
        });
        View allLayout = findViewById(R.id.allLayout);
        allLayout.setOnClickListener(v -> checkBoxAll.toggle());

        pm = getPackageManager();

        getApps();
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.install_apps_activity;
    }

    @Override
    public void onItemUnchecked() {
        if (checkBoxAll.isChecked()) {
            appUnchecked = true;
            checkBoxAll.setChecked(false);
            appUnchecked = false;
        }
    }

    @Override
    public void onNextPressed() {
        Intent i = new Intent(this, AppInstallerService.class);
        i.putStringArrayListExtra(PACKAGE_PATHS, adapter.getSelectedPackageIdPaths());
        startForegroundService(i);
        super.onNextPressed();
    }

    private void getApps() {
        String path = getString(R.string.calyx_fdroid_repo_location);
        File repoPath = new File(path);
        if (!repoPath.isDirectory()) {
            Log.e(TAG, "Local repo does not exist: " + repoPath);
            finish();
        }
        FilenameFilter filter = (dir, name) -> name.endsWith(".apk");
        new Thread(() -> {
            for (File apk : repoPath.listFiles(filter)) addApp(apk.getAbsolutePath());
            list.post(() -> list.scrollToPosition(0));
        }).start();
    }

    @WorkerThread
    private void addApp(String pathToApk) {
        PackageInfo packageInfo = pm.getPackageArchiveInfo(pathToApk, 0);
        packageInfo.applicationInfo.sourceDir = pathToApk;
        packageInfo.applicationInfo.publicSourceDir = pathToApk;

        CharSequence label = pm.getApplicationLabel(packageInfo.applicationInfo);
        Drawable logo = pm.getApplicationIcon(packageInfo.applicationInfo);
        String packageId = packageInfo.packageName;

        AppItem item = new AppItem(logo, label, packageId, pathToApk);
        list.post(() -> adapter.addItem(item));
    }

}
