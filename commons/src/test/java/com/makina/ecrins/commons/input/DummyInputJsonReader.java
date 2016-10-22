package com.makina.ecrins.commons.input;

import android.support.annotation.NonNull;

import com.google.gson.stream.JsonReader;

/**
 * Dummy implementation of {@link AbstractInputJsonReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyInputJsonReader
        extends AbstractInputJsonReader {

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
                                        @NonNull AbstractInput input) {

    }

    @Override
    public void readAdditionalTaxonData(@NonNull JsonReader reader,
                                        @NonNull String keyName,
                                        @NonNull AbstractTaxon taxon) {

    }
}
