package com.makina.ecrins.commons.input;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * Default {@code JsonWriter} about writing an {@link AbstractInput} as {@code JSON}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see InputJsonReader
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InputJsonWriter {

    private static final String TAG = InputJsonWriter.class.getName();

    private String dateFormat;
    private final OnInputJsonWriterListener onInputJsonWriterListener;

    public InputJsonWriter(@NonNull OnInputJsonWriterListener onInputJsonWriterListener) {
        this.dateFormat = InputHelper.DEFAULT_DATE_FORMAT;
        this.onInputJsonWriterListener = onInputJsonWriterListener;
    }

    @NonNull
    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(@NonNull final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Convert the given {@link AbstractInput} as {@code JSON} string.
     *
     * @param input the {@link AbstractInput} to convert
     *
     * @return a {@code JSON} string representation of the given {@link AbstractInput} or {@code null} if something goes wrong
     *
     * @see #write(Writer, AbstractInput)
     */
    @Nullable
    public String write(@Nullable final AbstractInput input) {
        if (input == null) {
            return null;
        }

        final StringWriter writer = new StringWriter();

        try {
            write(writer,
                  input);
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());

            return null;
        }

        return writer.toString();
    }

    /**
     * Convert the given {@link AbstractInput} as {@code JSON} and write it to the given {@code Writer}.
     *
     * @param out   the {@code Writer} to use
     * @param input the {@link AbstractInput} to convert
     *
     * @throws IOException if something goes wrong
     */
    public void write(@NonNull final Writer out,
                      @NonNull final AbstractInput input) throws
                                                          IOException {
        final JsonWriter writer = new JsonWriter(out);
        writeInput(writer,
                   input);
        writer.flush();
        writer.close();
    }

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
              .value(DateFormat.format(getDateFormat(),
                                       input.getDate())
                               .toString());

        if (input.getQualification() != null) {
            writer.name("qualification");
            writeQualification(writer,
                               input.getQualification());
        }

        onInputJsonWriterListener.writeAdditionalInputData(writer,
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

        writer.name("observers");
        writer.beginArray();

        for (Observer observer : observers) {
            writer.beginObject();
            writer.name("id")
                  .value(observer.getObserverId());
            writer.name("lastname")
                  .value(observer.getLastname());
            writer.name("firstname")
                  .value(observer.getFirstname());
            writer.endObject();
        }

        writer.endArray();
    }

    private void writeQualification(@NonNull final JsonWriter writer,
                                    @NonNull final Qualification qualification) throws
                                                                                IOException {
        writer.beginObject();

        writer.name("organism")
              .value(qualification.getOrganism());
        writer.name("protocol")
              .value(qualification.getProtocol());
        writer.name("lot")
              .value(qualification.getLot());

        writer.endObject();
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

        onInputJsonWriterListener.writeAdditionalTaxonData(writer,
                                                           taxon);

        writer.name("comment")
              .value(taxon.getComment());

        writer.endObject();
    }

    /**
     * Callback used by {@link InputJsonWriter}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    interface OnInputJsonWriterListener {

        /**
         * Adding some additional data to write from the current {@link AbstractInput}.
         *
         * @param writer the current @code JsonWriter} to use
         * @param input  the current {@link AbstractInput} to read
         *
         * @throws IOException if something goes wrong
         */
        void writeAdditionalInputData(@NonNull final JsonWriter writer,
                                      @NonNull final AbstractInput input) throws
                                                                          IOException;

        /**
         * Adding some additional data to write from the current {@link AbstractTaxon}.
         *
         * @param writer the current @code JsonWriter} to use
         * @param taxon  the current {@link AbstractTaxon} to read
         *
         * @throws IOException if something goes wrong
         */
        void writeAdditionalTaxonData(@NonNull final JsonWriter writer,
                                      @NonNull final AbstractTaxon taxon) throws
                                                                          IOException;
    }
}
