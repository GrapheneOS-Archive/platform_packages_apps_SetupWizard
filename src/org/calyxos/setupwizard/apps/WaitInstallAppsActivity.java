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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.File;

import org.json.JSONException;

import org.calyxos.setupwizard.apps.FDroidRepo;
import org.calyxos.setupwizard.BaseSetupWizardActivity;
import org.calyxos.setupwizard.R;

import static android.view.View.INVISIBLE;
import static android.view.animation.AnimationUtils.loadAnimation;
import static org.calyxos.setupwizard.SetupWizardApp.ACTION_APPS_INSTALLED;

public class WaitInstallAppsActivity extends BaseSetupWizardActivity {

    public static final String TAG = WaitInstallAppsActivity.class.getSimpleName();

    private static final String DEFAULT_BROWSER = "com.duckduckgo.mobile.android";

    private ProgressBar mProgressBar;
    private TextView mWaitingForAppsText;

    private String path;

    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_APPS_INSTALLED.equals(intent.getAction())) {
                afterAppsInstalled();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(packageReceiver, new IntentFilter(ACTION_APPS_INSTALLED));

        mProgressBar = findViewById(R.id.progress);
        mWaitingForAppsText = findViewById(R.id.waiting_for_apps);
        path = getString(R.string.calyx_fdroid_repo_location);

        if (!shouldWeWaitForApps()) {
            afterAppsInstalled();
        } else {
            setBackAllowed(false);
            if (!mProgressBar.isShown()) {
                mProgressBar.setVisibility(View.VISIBLE);
                mWaitingForAppsText.setVisibility(View.VISIBLE);
                mProgressBar.startAnimation(loadAnimation(this, R.anim.translucent_enter));
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(packageReceiver);
        super.onDestroy();
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wait_install_apps_activity;
    }

    private void afterAppsInstalled() {
        if (mProgressBar.isShown()) {
            mProgressBar.startAnimation(loadAnimation(this, R.anim.translucent_exit));
            mProgressBar.setVisibility(INVISIBLE);
            mWaitingForAppsText.setVisibility(INVISIBLE);
        }
        getPackageManager().setDefaultBrowserPackageNameAsUser(DEFAULT_BROWSER, getUserId());
        onNextPressed();
    }

    private boolean shouldWeWaitForApps() {
        if (AppInstallerService.areAllAppsInstalled())
            return false;
        File repoPath = new File(path);
        if (!repoPath.isDirectory()) {
            return false;
        } else {
            try {
                FDroidRepo.checkFdroidRepo(path);
            } catch (IOException | JSONException e) {
                return false;
            }
        }
        return true;
    }

}
