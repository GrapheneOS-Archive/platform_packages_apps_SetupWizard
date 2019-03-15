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

package org.calyxos.setupwizard;

import android.annotation.Nullable;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Switch;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

public class MicroGActivity extends BaseSetupWizardActivity {

    public static final String TAG = MicroGActivity.class.getSimpleName();
    private static final String[] MICROG_PACKAGES = new String[]{
            "com.google.android.gms",
            "com.android.vending"
    };

    private PackageManager pm;
    private Switch enableSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNextText(R.string.next);

        enableSwitch = findViewById(R.id.enableSwitch);
        findViewById(R.id.switchLayout).setOnClickListener(v -> enableSwitch.toggle());

        pm = getPackageManager();
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
    public void onNextPressed() {
        boolean enabled = enableSwitch.isChecked();
        for (String packageId : MICROG_PACKAGES) {
            setAppEnabled(packageId, enabled);
        }
        super.onNextPressed();
    }

    private void setAppEnabled(String packageName, boolean enabled) {
        int state = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        pm.setApplicationEnabledSetting(packageName, state, 0);
    }

}
