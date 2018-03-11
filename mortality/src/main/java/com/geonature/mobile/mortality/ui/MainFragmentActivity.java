package com.geonature.mobile.mortality.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;

import com.geonature.mobile.commons.input.Observer;
import com.geonature.mobile.commons.settings.AbstractAppSettings;
import com.geonature.mobile.commons.settings.AbstractSettingsService;
import com.geonature.mobile.commons.ui.AbstractMainFragmentActivity;
import com.geonature.mobile.mortality.MainApplication;
import com.geonature.mobile.mortality.R;
import com.geonature.mobile.mortality.content.MainContentProvider;
import com.geonature.mobile.mortality.settings.AppSettings;
import com.geonature.mobile.mortality.settings.SettingsService;
import com.geonature.mobile.mortality.ui.input.PagerFragmentActivity;
import com.geonature.mobile.mortality.ui.settings.MainPreferencesActivity;
import com.geonature.mobile.mortality.ui.sync.SynchronizationActivity;

/**
 * This is the main <code>Activity</code> of this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity
        extends AbstractMainFragmentActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this,
                                         MainPreferencesActivity.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonStartSynchronization:
                startActivity(new Intent(this,
                                         SynchronizationActivity.class));
                break;
            case R.id.buttonStartInput:
                ((MainApplication) getApplication()).setInput(null);
                startActivity(new Intent(this,
                                         PagerFragmentActivity.class));
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
    protected Class<? extends AbstractSettingsService> getSettingsServiceClass() {

        return SettingsService.class;
    }

    @Override
    protected boolean checkServiceMessageStatusTask() {

        return true;
    }

    @Override
    protected void performMessageStatusTaskHandler(
            AbstractMainFragmentActivity mainFragmentActivity,
            Message msg) {
        // nothing to do ...
    }

    @Override
    protected Uri getObserverLoaderUri(long ObserverId) {

        return Uri.withAppendedPath(MainContentProvider.CONTENT_OBSERVERS_URI,
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
