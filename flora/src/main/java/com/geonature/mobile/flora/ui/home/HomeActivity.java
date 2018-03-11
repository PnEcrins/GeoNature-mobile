package com.geonature.mobile.flora.ui.home;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.input.AbstractInputIntentService;
import com.geonature.mobile.commons.settings.AbstractAppSettings;
import com.geonature.mobile.commons.settings.AbstractAppSettingsIntentService;
import com.geonature.mobile.commons.ui.home.AbstractHomeActivity;
import com.geonature.mobile.flora.MainApplication;
import com.geonature.mobile.flora.input.Input;
import com.geonature.mobile.flora.input.InputIntentService;
import com.geonature.mobile.flora.settings.AppSettings;
import com.geonature.mobile.flora.settings.AppSettingsIntentService;
import com.geonature.mobile.flora.ui.input.PagerFragmentActivity;
import com.geonature.mobile.flora.ui.settings.MainPreferencesActivity;
import com.geonature.mobile.flora.ui.sync.SynchronizationActivity;

/**
 * Home screen {@code Activity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class HomeActivity
        extends AbstractHomeActivity {

    @Override
    protected boolean isCloseApplication() {
        return ((MainApplication) getApplication()).isCloseApplication();
    }

    @Override
    protected void setCloseApplication(boolean closeApplication) {
        ((MainApplication) getApplication()).setCloseApplication(closeApplication);
    }

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
