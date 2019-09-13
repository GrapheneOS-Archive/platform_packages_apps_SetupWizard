/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2017-2019 The LineageOS Project
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

package org.calyxos.setupwizard;

import static org.calyxos.setupwizard.SetupWizardApp.ACTION_SETUP_COMPLETE;
import static org.calyxos.setupwizard.SetupWizardApp.LOGV;

import android.animation.Animator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.File;

import org.json.JSONException;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.calyxos.setupwizard.apps.AppInstallerService;
import org.calyxos.setupwizard.apps.FDroidRepo;
import org.calyxos.setupwizard.util.EnableAccessibilityController;

import static android.os.Binder.getCallingUserHandle;
import static org.calyxos.setupwizard.Manifest.permission.FINISH_SETUP;
import static org.calyxos.setupwizard.SetupWizardApp.ACTION_APPS_INSTALLED;

public class FinishActivity extends BaseSetupWizardActivity {

    public static final String TAG = FinishActivity.class.getSimpleName();
    public static final String DEFAULT_BROWSER = "com.duckduckgo.mobile.android";

    private ImageView mReveal;

    private EnableAccessibilityController mEnableAccessibilityController;

    private SetupWizardApp mSetupWizardApp;

    private final Handler mHandler = new Handler();

    private volatile boolean mIsFinishing = false;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=" + savedInstanceState);
        }
        mSetupWizardApp = (SetupWizardApp) getApplication();
        mReveal = (ImageView) findViewById(R.id.reveal);
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        setNextText(R.string.start);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mWaitingForAppsText = (TextView) findViewById(R.id.waiting_for_apps);
        registerReceiver(packageReceiver, new IntentFilter(ACTION_APPS_INSTALLED));
        path = getString(R.string.calyx_fdroid_repo_location);
        if (!shouldWeWaitForApps()) {
            afterAppsInstalled();
        } else {
            // Wait for all apps to be installed before allowing the user to proceed
            setNextAllowed(false);
            setBackAllowed(false);
            if (!mProgressBar.isShown()) {
                mProgressBar.setVisibility(View.VISIBLE);
                mWaitingForAppsText.setVisibility(View.VISIBLE);
                mProgressBar.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.translucent_enter));
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
        return R.layout.finish_activity;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.translucent_enter, R.anim.translucent_exit);
    }

    @Override
    public void onNavigateNext() {
        applyForwardTransition(TRANSITION_ID_NONE);
        startFinishSequence();
    }

    private void finishSetup() {
        if (!mIsFinishing) {
            mIsFinishing = true;
            setupRevealImage();
        }
    }

    private void startFinishSequence() {
        Intent i = new Intent(ACTION_SETUP_COMPLETE);
        i.setPackage(getPackageName());
        sendBroadcastAsUser(i, getCallingUserHandle(), FINISH_SETUP);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        hideBackButton();
        hideNextButton();
        finishSetup();
    }

    private void setupRevealImage() {
        final Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(this);
        wallpaperManager.forgetLoadedWallpaper();
        final Bitmap wallpaper = wallpaperManager.getBitmap();
        Bitmap cropped = null;
        if (wallpaper != null) {
            cropped = Bitmap.createBitmap(wallpaper, 0,
                    0, Math.min(p.x, wallpaper.getWidth()),
                    Math.min(p.y, wallpaper.getHeight()));
        }
        if (cropped != null) {
            mReveal.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mReveal.setImageBitmap(cropped);
        } else {
            mReveal.setBackground(wallpaperManager
                    .getBuiltInDrawable(p.x, p.y, false, 0, 0));
        }
        animateOut();
    }

    private void animateOut() {
        int cx = (mReveal.getLeft() + mReveal.getRight()) / 2;
        int cy = (mReveal.getTop() + mReveal.getBottom()) / 2;
        int finalRadius = Math.max(mReveal.getWidth(), mReveal.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mReveal, cx, cy, 0, finalRadius);
        anim.setDuration(900);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        completeSetup();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
    }

    private void completeSetup() {
        if (mEnableAccessibilityController != null) {
            mEnableAccessibilityController.onDestroy();
        }
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(mSetupWizardApp);
        wallpaperManager.forgetLoadedWallpaper();
        finishAllAppTasks();
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(),
                Activity.RESULT_OK);
        startActivityForResult(intent, NEXT_REQUEST);
    }

    private void afterAppsInstalled() {
        if (mProgressBar.isShown()) {
            mProgressBar.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.translucent_exit));
            mProgressBar.setVisibility(View.INVISIBLE);
            mWaitingForAppsText.setVisibility(View.INVISIBLE);
        }
        getPackageManager().setDefaultBrowserPackageNameAsUser(DEFAULT_BROWSER, getUserId());
        setNextAllowed(true);
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
