package com.makina.ecrins.commons.input;

import android.support.annotation.NonNull;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Dummy implementation of {@link AbstractInputJsonWriter}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyInputJsonWriter
        extends AbstractInputJsonWriter {

    @Override
    public void writeAdditionalInputData(@NonNull JsonWriter writer,
                                         @NonNull AbstractInput input) throws
                                                                       IOException {

    }

    @Override
    public void writeAdditionalTaxonData(@NonNull JsonWriter writer,
                                         @NonNull AbstractTaxon taxon) throws
                                                                       IOException {

    }
}
