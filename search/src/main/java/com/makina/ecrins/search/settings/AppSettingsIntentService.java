package com.makina.ecrins.search.settings;

import android.support.annotation.NonNull;
import android.util.JsonReader;

import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractAppSettingsIntentService;
import com.makina.ecrins.maps.settings.io.MapSettingsReader;

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
            case "search":
                ((AppSettings) appSettings).mSearchSettings = new SearchSettingsReader().readSearchSettings(reader);
                break;
        }
    }
}
