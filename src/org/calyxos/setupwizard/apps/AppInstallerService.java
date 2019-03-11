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
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.NotificationManager.IMPORTANCE_MIN;
import static java.util.Objects.requireNonNull;

public class AppInstallerService extends IntentService {

    static final String PACKAGE_PATHS = "packagePaths";

    private static final String TAG = AppInstallerService.class.getSimpleName();
    private static final String CHANNEL_ID = "SetupWizard";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    public AppInstallerService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(ONGOING_NOTIFICATION_ID, getNotification());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent i) {
        PackageInstaller28 packageInstaller = new PackageInstaller28(getApplicationContext());
        ArrayList<String> packagePaths = i.getStringArrayListExtra(PACKAGE_PATHS);
        for (String path : packagePaths) {
            try {
                packageInstaller.install(new File(path));
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
        }
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

}
