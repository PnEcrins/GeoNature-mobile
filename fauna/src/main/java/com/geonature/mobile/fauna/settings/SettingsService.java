package com.geonature.mobile.fauna.settings;

import android.util.Log;

import com.geonature.mobile.commons.model.MountPoint;
import com.geonature.mobile.commons.settings.AbstractAppSettings;
import com.geonature.mobile.commons.settings.AbstractSettingsService;
import com.geonature.mobile.commons.settings.IServiceMessageStatusTask;
import com.geonature.mobile.commons.settings.ServiceStatus;
import com.geonature.mobile.commons.util.FileUtils;
import com.geonature.mobile.fauna.MainApplication;
import com.geonature.mobile.maps.AbstractLoadFeaturesFromFileAsyncTask;
import com.geonature.mobile.maps.jts.geojson.Feature;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Simple {@link AbstractSettingsService} implementation
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SettingsService
        extends AbstractSettingsService {

    protected LoadUnitiesFromFileAsyncTask mLoadUnitiesFromFileAsyncTask = null;

    @Override
    protected IServiceMessageStatusTask getServiceMessageStatusTask() {

        if (mLoadUnitiesFromFileAsyncTask == null) {
            Log.d(getClass().getName(),
                  "getServiceMessageStatusTask : create new instance");
            mLoadUnitiesFromFileAsyncTask = new LoadUnitiesFromFileAsyncTask();
        }
        else {
            Log.d(getClass().getName(),
                  "getServiceMessageStatusTask : initialized");
        }

        return mLoadUnitiesFromFileAsyncTask;
    }

    @Override
    protected void executeServiceMessageStatusTask() {

        if ((mLoadUnitiesFromFileAsyncTask != null) && mLoadUnitiesFromFileAsyncTask.getServiceStatus()
                                                                                    .getStatus()
                                                                                    .equals(ServiceStatus.Status.PENDING)) {
            try {
                File unities = FileUtils.getFile(FileUtils.getRootFolder(this,
                                                                         MountPoint.StorageType.EXTERNAL),
                                                 "unities.wkt");
                mLoadUnitiesFromFileAsyncTask.execute(unities);
            }
            catch (IOException ioe) {
                mLoadUnitiesFromFileAsyncTask.cancel(true);
                Log.w(getClass().getName(),
                      ioe.getMessage(),
                      ioe);
                sendMessage(mLoadUnitiesFromFileAsyncTask.whatLoadingFailed(),
                            "unities.wkt");
            }
        }
    }

    @Override
    protected String getSettingsFilename() {

        return "settings_fauna.json";
    }

    @Override
    protected AbstractAppSettings getSettingsFromJsonObject(JSONObject settingsJsonObject) throws JSONException {

        return new AppSettings(settingsJsonObject);
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

    public class LoadUnitiesFromFileAsyncTask
            extends AbstractLoadFeaturesFromFileAsyncTask
            implements IServiceMessageStatusTask {

        private volatile ServiceStatus mServiceStatus = new ServiceStatus(ServiceStatus.Status.PENDING,
                                                                          "");

        @Override
        public ServiceStatus getServiceStatus() {

            return mServiceStatus;
        }

        @Override
        protected void sendMessage(
                int what,
                Object obj) {

            SettingsService.this.sendMessage(what,
                                             obj);
        }

        @Override
        protected void sendProgress(
                int what,
                int progress,
                int max) {

            SettingsService.this.sendProgress(what,
                                              progress,
                                              max);
        }

        @Override
        protected int whatLoadingStart() {

            return MainApplication.HANDLER_UNITIES_LOADING_START;
        }

        @Override
        protected int whatLoading() {

            return MainApplication.HANDLER_UNITIES_LOADING;
        }

        @Override
        protected int whatLoadingFailed() {

            return MainApplication.HANDLER_UNITIES_LOADED_FAILED;
        }

        @Override
        protected int whatLoadingLoaded() {

            return MainApplication.HANDLER_UNITIES_LOADED;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            mServiceStatus = new ServiceStatus(ServiceStatus.Status.RUNNING,
                                               "");
        }

        @Override
        protected void onPostExecute(List<Feature> result) {

            super.onPostExecute(result);

            if (result.isEmpty()) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED_WITH_ERRORS,
                                                   "");
            }
            else {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED,
                                                   "");
            }

            sendMessage(AbstractSettingsService.HANDLER_STATUS,
                        SettingsService.this.getServiceStatus());
            checkStatusAndStop();
        }

        @Override
        protected void onCancelled() {

            super.onCancelled();

            mServiceStatus = new ServiceStatus(ServiceStatus.Status.ABORTED,
                                               "");

            sendMessage(AbstractSettingsService.HANDLER_STATUS,
                        SettingsService.this.getServiceStatus());
            checkStatusAndStop();
        }
    }
}
