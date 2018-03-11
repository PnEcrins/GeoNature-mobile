package com.geonature.mobile.flora.settings;

import android.support.annotation.NonNull;
import android.util.JsonReader;

import com.geonature.mobile.commons.settings.AbstractAppSettings;
import com.geonature.mobile.commons.settings.AbstractAppSettingsIntentService;
import com.geonature.mobile.maps.settings.io.MapSettingsReader;

import java.io.IOException;

/**
 * Custom {@code IntentService} to read as JSON file a given {@link AbstractAppSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class AppSettingsIntentService
        extends AbstractAppSettingsIntentService {

    @NonNull
    @Override
    public AbstractAppSettings createAppSettings() {
        return new AppSettings();
    }

    @Override
    public void readAdditionalSettings(@NonNull JsonReader reader,
                                       @NonNull String keyName,
                                       @NonNull AbstractAppSettings appSettings) throws
                                                                                 IOException {
        switch (keyName) {
            case "map":
                ((AppSettings) appSettings).mMapSettings = new MapSettingsReader().readMapSettings(reader);
                break;
        }
    }
}
