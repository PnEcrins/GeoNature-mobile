package com.makina.ecrins.flora.ui.home;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractInputIntentService;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractAppSettingsIntentService;
import com.makina.ecrins.commons.ui.home.AbstractHomeActivity;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.InputIntentService;
import com.makina.ecrins.flora.settings.AppSettings;
import com.makina.ecrins.flora.settings.AppSettingsIntentService;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;
import com.makina.ecrins.flora.ui.settings.MainPreferencesActivity;
import com.makina.ecrins.flora.ui.sync.SynchronizationActivity;

/**
 * Home screen {@code Activity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class HomeActivity
        extends AbstractHomeActivity {

    @NonNull
    @Override
    public AbstractInput createInput() {
        return new Input();
    }

    @NonNull
    @Override
    public Class<? extends AbstractInputIntentService> getInputIntentServiceClass() {
        return InputIntentService.class;
    }

    @NonNull
    @Override
    public Class<? extends AbstractAppSettingsIntentService> getAppSettingsIntentServiceClass() {
        return AppSettingsIntentService.class;
    }

    @Override
    public void onAppSettingsLoaded(@NonNull AbstractAppSettings appSettings) {
        ((MainApplication) getApplication()).setAppSettings((AppSettings) appSettings);
    }

    @Override
    public void onShowSettings() {
        startActivity(new Intent(this,
                                 MainPreferencesActivity.class));
    }

    @Override
    public void onStartSync() {
        startActivity(new Intent(this,
                                 SynchronizationActivity.class));
    }

    @Override
    public void onStartInput() {
        startActivity(new Intent(this,
                                 PagerFragmentActivity.class));
    }

    @Override
    public void onContinueInput(long inputId) {
        final Intent intent = new Intent(this,
                                         PagerFragmentActivity.class);

        intent.putExtra(PagerFragmentActivity.EXTRA_NEW_INPUT,
                        false);
        intent.putExtra(PagerFragmentActivity.EXTRA_PAGER_ID,
                        inputId);

        startActivity(intent);
    }
}
