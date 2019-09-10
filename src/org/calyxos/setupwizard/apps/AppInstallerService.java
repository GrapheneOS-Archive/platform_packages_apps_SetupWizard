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
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.NotificationManager.IMPORTANCE_MIN;
import static org.calyxos.setupwizard.Manifest.permission.FINISH_SETUP;
import static org.calyxos.setupwizard.SetupWizardApp.PACKAGENAMES;
import static org.calyxos.setupwizard.SetupWizardApp.ACTION_APPS_INSTALLED;
import static java.util.Objects.requireNonNull;

public class AppInstallerService extends IntentService {

    static final String APKS = "apks";
    static final String PATH = "path";

    private static final String TAG = AppInstallerService.class.getSimpleName();
    private static final String CHANNEL_ID = "SetupWizard";
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final long TIMEOUT = 60 * 1000;
    private static final long WAIT_TIME = 2 * 1000;

    private final PackageReceiver mPackageReceiver = new PackageReceiver();
    private static ArrayList<String> mPackagesExpected;
    private static ArrayList<String> mPackagesInstalled = new ArrayList<>();
    private static Boolean mAllAppsInstalled = false;

    public AppInstallerService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(ONGOING_NOTIFICATION_ID, getNotification());
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        registerReceiver(mPackageReceiver, intentFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent i) {
        PackageInstaller28 packageInstaller = new PackageInstaller28(getApplicationContext());
        String path = i.getStringExtra(PATH);
        ArrayList<String> apks = i.getStringArrayListExtra(APKS);
        mPackagesExpected = i.getStringArrayListExtra(PACKAGENAMES);
        for (String apk : apks) {
            try {
                packageInstaller.install(new File(path + "/" + apk));
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
        }
        try {
            if (mPackagesExpected != null && !mPackagesExpected.isEmpty()) {
                long waitTime = TIMEOUT;
                while (waitTime > 0 && !mAllAppsInstalled) {
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    waitTime -= WAIT_TIME;
                }
                mAllAppsInstalled = true;
                Intent i2 = new Intent(ACTION_APPS_INSTALLED);
                i2.setPackage(getPackageName());
                sendBroadcastAsUser(i2, Binder.getCallingUserHandle(), FINISH_SETUP);
            } else if (mPackagesExpected != null && mPackagesExpected.isEmpty()) {
                mAllAppsInstalled = true;
                Intent i2 = new Intent(ACTION_APPS_INSTALLED);
                i2.setPackage(getPackageName());
                sendBroadcastAsUser(i2, Binder.getCallingUserHandle(), FINISH_SETUP);
            }
        } catch (NullPointerException e) {
            // Ignore
            // TODO: Fix this mess properly.
        }
    }

    @Override
    public void onDestroy () {
        unregisterReceiver(mPackageReceiver);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        CharSequence name = "SetupWizardChannel";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_MIN);
        NotificationManager notificationManager = requireNonNull(getSystemService(NotificationManager.class));
        notificationManager.createNotificationChannel(channel);
    }

    private Notification getNotification() {
        return new Builder(getApplicationContext(), CHANNEL_ID).build();
    }

    private class PackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getData() == null) {
                return;
            }
            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName == null) {
                return;
            }

            if (mPackagesExpected.contains(packageName)) mPackagesInstalled.add(packageName);

            if (mPackagesInstalled.size() == mPackagesExpected.size()) {
                mAllAppsInstalled = true;
                Intent i = new Intent(ACTION_APPS_INSTALLED);
                i.setPackage(getPackageName());
                sendBroadcastAsUser(i, Binder.getCallingUserHandle(), FINISH_SETUP);
            }
        }
    }

    public static Boolean areAllAppsInstalled() {
        return mAllAppsInstalled;
    }

}
