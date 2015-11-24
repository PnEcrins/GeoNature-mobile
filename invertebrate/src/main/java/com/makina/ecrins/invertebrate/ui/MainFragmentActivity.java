package com.makina.ecrins.invertebrate.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractSettingsService;
import com.makina.ecrins.commons.ui.AbstractMainFragmentActivity;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.R;
import com.makina.ecrins.invertebrate.content.MainContentProvider;
import com.makina.ecrins.invertebrate.settings.AppSettings;
import com.makina.ecrins.invertebrate.settings.SettingsService;
import com.makina.ecrins.invertebrate.ui.input.PagerFragmentActivity;
import com.makina.ecrins.invertebrate.ui.settings.MainPreferencesActivity;
import com.makina.ecrins.invertebrate.ui.sync.SynchronizationActivity;
import com.makina.ecrins.maps.geojson.Feature;

import java.util.List;

/**
 * This is the main <code>Activity</code> of this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity
        extends AbstractMainFragmentActivity {

    private static final String PROGRESS_DIALOG_UNITIES_FRAGMENT = "progress_dialog_unities";

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

        return !((MainApplication) getApplication()).getUnities()
                                                    .isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void performMessageStatusTaskHandler(
            AbstractMainFragmentActivity mainFragmentActivity,
            Message msg) {

        switch (msg.what) {
            case MainApplication.HANDLER_UNITIES_LOADING_START:
                mainFragmentActivity.showProgressDialog(PROGRESS_DIALOG_UNITIES_FRAGMENT,
                                                        R.string.progress_title,
                                                        R.string.progress_message_loading_unities,
                                                        ProgressDialog.STYLE_HORIZONTAL,
                                                        0,
                                                        (Integer) msg.obj);
                break;
            case MainApplication.HANDLER_UNITIES_LOADING:
                mainFragmentActivity.showProgressDialog(PROGRESS_DIALOG_UNITIES_FRAGMENT,
                                                        R.string.progress_title,
                                                        R.string.progress_message_loading_unities,
                                                        ProgressDialog.STYLE_HORIZONTAL,
                                                        msg.arg1,
                                                        msg.arg2);
                break;
            case MainApplication.HANDLER_UNITIES_LOADED:
                mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_UNITIES_FRAGMENT);
                ((MainApplication) mainFragmentActivity.getApplication()).setUnities((List<Feature>) msg.obj);
                break;
            case MainApplication.HANDLER_UNITIES_LOADED_FAILED:
                mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_UNITIES_FRAGMENT);
                Toast.makeText(mainFragmentActivity,
                               String.format(mainFragmentActivity.getString(R.string.message_unities_not_found),
                                             msg.obj),
                               Toast.LENGTH_LONG)
                     .show();
                break;
        }
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
