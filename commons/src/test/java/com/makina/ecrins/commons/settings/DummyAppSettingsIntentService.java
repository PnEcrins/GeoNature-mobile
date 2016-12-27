package com.makina.ecrins.commons.settings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.JsonReader;

import java.io.IOException;

/**
 * Dummy implementation of {@link AbstractAppSettingsIntentService}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyAppSettingsIntentService
        extends AbstractAppSettingsIntentService {

    @Override
    public void onStart(Intent intent,
                        int startId) {
        onHandleIntent(intent);
        stopSelf(startId);
    }

    @NonNull
    @Override
    public AbstractAppSettings createAppSettings() {
        return new DummyAppSettings();
    }

    @Override
    public void readAdditionalSettings(@NonNull JsonReader reader,
                                       @NonNull String keyName,
                                       @NonNull AbstractAppSettings appSettings) throws
                                                                                 IOException {
        // nothing to do ...
    }
}
