package com.makina.ecrins.flora.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;

import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.ui.AbstractMainFragmentActivity;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.settings.AppSettings;
import com.makina.ecrins.flora.settings.SettingsService;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;
import com.makina.ecrins.flora.ui.settings.MainPreferencesActivity;
import com.makina.ecrins.flora.ui.sync.SynchronizationActivity;

/**
 * This is the main <code>Activity</code> of this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity extends AbstractMainFragmentActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, MainPreferencesActivity.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartSynchronization:
                startActivity(new Intent(this, SynchronizationActivity.class));
                break;
            case R.id.buttonStartInput:
                ((MainApplication) getApplication()).setInput(null);
                startActivity(new Intent(this, PagerFragmentActivity.class));
                break;
        }
    }

    @Override
    protected boolean isCloseApplication() {
        return ((MainApplication) getApplication()).isCloseApplication();
    }

    @Override
    protected void setCloseApplication(boolean closeApplication) {
        ((MainApplication) getApplication()).setCloseApplication(closeApplication);
    }

    @Override
    protected AbstractAppSettings getAppSettings() {
        return ((MainApplication) getApplication()).getAppSettings();
    }

    @Override
    protected void setAppSettings(AbstractAppSettings appSettings) {
        ((MainApplication) getApplication()).setAppSettings((AppSettings) appSettings);
    }

    @Override
    protected String getSettingsServiceAction() {
        return SettingsService.class.getName();
    }

    @Override
    protected boolean checkServiceMessageStatusTask() {
        return true;
    }

    @Override
    protected void performMessageStatusTaskHandler(AbstractMainFragmentActivity mainFragmentActivity, Message msg) {
        // nothing to do ...
    }

    @Override
    protected Uri getObserverLoaderUri(long ObserverId) {
        return Uri.withAppendedPath(
                MainContentProvider.CONTENT_OBSERVERS_URI,
                Long.toString(ObserverId));
    }

    @Override
    protected Observer getDefaultObserver() {
        return ((MainApplication) getApplication()).getDefaultObserver();
    }

    @Override
    protected void setDefaultObserver(Observer observer) {
        ((MainApplication) getApplication()).setDefaultObserver(observer);
    }

    @Override
    protected int whatSettingsLoadingStart() {
        return MainApplication.HANDLER_SETTINGS_LOADING_START;
    }

    @Override
    protected int whatSettingsLoading() {
        return MainApplication.HANDLER_SETTINGS_LOADING;
    }

    @Override
    protected int whatSettingsLoadingFailed() {
        return MainApplication.HANDLER_SETTINGS_LOADED_FAILED;
    }

    @Override
    protected int whatSettingsLoadingLoaded() {
        return MainApplication.HANDLER_SETTINGS_LOADED;
    }
}
