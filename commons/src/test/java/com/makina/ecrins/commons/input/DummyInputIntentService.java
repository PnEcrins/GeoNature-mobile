package com.makina.ecrins.commons.input;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;

/**
 * Dummy implementation of {@link AbstractInputIntentService}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyInputIntentService
        extends AbstractInputIntentService {

    @Override
    public void onStart(Intent intent,
                        int startId) {
        onHandleIntent(intent);
        stopSelf(startId);
    }

    @NonNull
    @Override
    public AbstractInput createInput() {
        return new DummyInput(InputType.FAUNA);
    }

    @NonNull
    @Override
    public AbstractTaxon createTaxon() {
        return new DummyTaxon(0L);
    }

    @Override
    public void readAdditionalInputData(@NonNull JsonReader reader,
                                        @NonNull String keyName,
                                        @NonNull AbstractInput input) throws
                                                                      IOException {
        // nothing to do ...
    }

    @Override
    public void readAdditionalTaxonData(@NonNull JsonReader reader,
                                        @NonNull String keyName,
                                        @NonNull AbstractTaxon taxon) throws
                                                                      IOException {
        // nothing to do ...
    }

    @Override
    public void writeAdditionalInputData(@NonNull JsonWriter writer,
                                         @NonNull AbstractInput input) throws
                                                                       IOException {

    }

    @Override
    public void writeAdditionalTaxonData(@NonNull JsonWriter writer,
                                         @NonNull AbstractTaxon taxon) throws
                                                                       IOException {
        // nothing to do ...
    }
}
