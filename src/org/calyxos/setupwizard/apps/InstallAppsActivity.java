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
import android.view.View;
import android.widget.CheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.calyxos.setupwizard.BaseSetupWizardActivity;
import org.calyxos.setupwizard.R;
import org.calyxos.setupwizard.apps.AppAdapter.AppItemListener;

import java.io.File;

import static java.util.Objects.requireNonNull;
import static org.calyxos.setupwizard.SetupWizardApp.FDROID_CATEGORY_DEFAULT;
import static org.calyxos.setupwizard.apps.AppInstallerService.APKS;
import static org.calyxos.setupwizard.apps.AppInstallerService.PATH;

public class InstallAppsActivity extends BaseSetupWizardActivity implements AppItemListener {

    public static final String TAG = InstallAppsActivity.class.getSimpleName();

    private static String path;

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

        path = getString(R.string.calyx_fdroid_repo_location);

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
    protected int getTitleResId() {
        return R.string.install_apps_title;
    }


    @Override
    protected int getIconResId() {
        return R.drawable.fdroid_logo;
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
        // scroll to the end, if the user didn't
        LinearLayoutManager layoutManager = (LinearLayoutManager) requireNonNull(list.getLayoutManager());
        int lastPosition = adapter.getItemCount() - 1;
        if (layoutManager.findLastCompletelyVisibleItemPosition() != lastPosition) {
            list.smoothScrollToPosition(lastPosition);
            return;
        }

        Intent i = new Intent(this, AppInstallerService.class);
        i.putExtra(PATH, path);
        i.putStringArrayListExtra(APKS, adapter.getSelectedPackageNameAPKs());
        startForegroundService(i);
        super.onNextPressed();
    }

    private void getApps() {
        File repoPath = new File(path);
        if (!repoPath.isDirectory()) {
            Log.e(TAG, "Local repo does not exist: " + repoPath);
            finish();
        }
        new Thread(() -> {
            FDroidRepo.loadFdroidJson(FDROID_CATEGORY_DEFAULT, path, list, adapter);
            list.post(() -> list.scrollToPosition(0));
        }).start();
    }

    

}
