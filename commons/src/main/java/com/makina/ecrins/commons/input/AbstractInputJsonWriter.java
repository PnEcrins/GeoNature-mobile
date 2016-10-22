package com.makina.ecrins.commons.input;

import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * Default {@code JsonWriter} about writing an {@link AbstractInput} as {@code JSON}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractInputJsonWriter {

    String dateFormat = "yyyy/MM/dd";

    public void setDateFormat(@NonNull final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void write(@NonNull final Writer out,
                      @NonNull final AbstractInput input) throws
                                                          IOException {
        final JsonWriter writer = new JsonWriter(out);
        writeInput(writer,
                   input);
        writer.close();
    }

    /**
     * Adding some additional data to write from the current {@link AbstractInput}.
     *
     * @param writer the current @code JsonWriter} to use
     * @param input  the current {@link AbstractInput} to read
     *
     * @throws IOException
     */
    public abstract void writeAdditionalInputData(@NonNull final JsonWriter writer,
                                                  @NonNull final AbstractInput input) throws
                                                                                      IOException;

    /**
     * Adding some additional data to write from the current {@link AbstractTaxon}.
     *
     * @param writer the current @code JsonWriter} to use
     * @param taxon  the current {@link AbstractTaxon} to read
     *
     * @throws IOException
     */
    public abstract void writeAdditionalTaxonData(@NonNull final JsonWriter writer,
                                                  @NonNull final AbstractTaxon taxon) throws
                                                                                      IOException;

    private void writeInput(@NonNull final JsonWriter writer,
                            @NonNull final AbstractInput input) throws
                                                                IOException {
        writer.beginObject();

        writer.name("id")
              .value(input.getInputId());
        writer.name("input_type")
              .value(input.getType()
                          .getValue());
        writer.name("initial_input")
              .value("nomade");
        writer.name("dateobs")
              .value(DateFormat.format(dateFormat,
                                       input.getDate())
                               .toString());

        writeAdditionalInputData(writer,
                                 input);

        writeObservers(writer,
                       input.getObservers()
                            .values());
        writeTaxa(writer,
                  input.getTaxa()
                       .values());

        writer.endObject();
    }

    private void writeObservers(@NonNull final JsonWriter writer,
                                @NonNull final Collection<Observer> observers) throws
                                                                               IOException {
        writer.name("observers_id");
        writer.beginArray();

        for (Observer observer : observers) {
            writer.value(observer.getObserverId());
        }

        writer.endArray();
    }

    private void writeTaxa(@NonNull final JsonWriter writer,
                           @NonNull final Collection<AbstractTaxon> taxa) throws
                                                                          IOException {
        writer.name("taxons");
        writer.beginArray();

        for (AbstractTaxon taxon : taxa) {
            writeTaxon(writer,
                       taxon);
        }

        writer.endArray();
    }

    private void writeTaxon(@NonNull final JsonWriter writer,
                            @NonNull final AbstractTaxon taxon) throws
                                                                IOException {
        writer.beginObject();

        writer.name("id")
              .value(taxon.getId());
        writer.name("id_taxon")
              .value(taxon.getTaxonId());
        writer.name("name_entered")
              .value(taxon.getNameEntered());

        writer.name("observation");
        writer.beginObject();
        writer.name("criterion")
              .value(taxon.getCriterionId());
        writer.endObject();

        writeAdditionalTaxonData(writer,
                                 taxon);

        writer.name("comment")
              .value(taxon.getComment());

        writer.endObject();
    }
}
