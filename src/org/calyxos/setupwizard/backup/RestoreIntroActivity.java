package org.calyxos.setupwizard.backup;

import android.content.Intent;
import org.calyxos.setupwizard.R;
import org.calyxos.setupwizard.SubBaseActivity;

import static org.calyxos.setupwizard.SetupWizardApp.*;

public class RestoreIntroActivity extends SubBaseActivity {

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);

        findViewById(R.id.intro_restore_button).setOnClickListener(v -> launchRestore());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.intro_restore_activity;
    }

    @Override
    protected int getTitleResId() {
        return R.string.intro_restore_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_restore;
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_SLIDE;
    }

    private void launchRestore() {
        Intent intent = new Intent(ACTION_RESTORE_FROM_BACKUP);
        startSubactivity(intent, REQUEST_CODE_RESTORE);
    }

}
