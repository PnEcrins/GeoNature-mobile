package com.makina.ecrins.search.settings;

import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractSettingsService;
import com.makina.ecrins.commons.settings.IServiceMessageStatusTask;
import com.makina.ecrins.search.MainApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple {@link com.makina.ecrins.commons.settings.AbstractSettingsService} implementation
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SettingsService
        extends AbstractSettingsService {

    @Override
    protected IServiceMessageStatusTask getServiceMessageStatusTask() {
        return null;
    }

    @Override
    protected void executeServiceMessageStatusTask() {
        // nothing to do ...
    }

    @Override
    protected String getSettingsFilename() {
        return "settings_search.json";
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
}
