package com.makina.ecrins.search.settings;

import android.content.Context;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractLoadSettingsRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Concrete implementation of {@link com.makina.ecrins.commons.settings.AbstractLoadSettingsRequestHandler}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LoadSettingsRequestHandler extends AbstractLoadSettingsRequestHandler {

    public LoadSettingsRequestHandler(Context pContext) {
        super(pContext);
    }

    @NonNull
    @Override
    protected AbstractAppSettings getSettingsFromJsonObject(@NonNull JSONObject settingsJsonObject) throws JSONException {
        return new AppSettings(settingsJsonObject);
    }
}
