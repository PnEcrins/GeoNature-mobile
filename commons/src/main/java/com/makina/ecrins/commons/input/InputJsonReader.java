package com.makina.ecrins.commons.input;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class InputJsonReader {

    private static final String TAG = InputJsonReader.class.getSimpleName();

    private final String dateFormat;
    private final OnInputJsonReaderListener onInputJsonReaderListener;

    public InputJsonReader(@NonNull final String dateFormat,
                           @NonNull final OnInputJsonReaderListener onInputJsonReaderListener) {
        this.dateFormat = dateFormat;
        this.onInputJsonReaderListener = onInputJsonReaderListener;
    }

    @NonNull
    public String getDateFormat() {
        return dateFormat;
    }

    @NonNull
    public AbstractInput read(@NonNull final Reader in) throws
                                                        IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final AbstractInput input = readInput(jsonReader);
        jsonReader.close();

        return input;
    }

    @NonNull
    private AbstractInput readInput(@NonNull final JsonReader reader) throws
                                                                      IOException {
        final AbstractInput input = onInputJsonReaderListener.createInput();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "id":
                    input.mInputId = reader.nextLong();
                    break;
                case "input_type":
                    final InputType inputType = InputType.fromValue(reader.nextString());

                    if (inputType != null) {
                        input.mType = inputType;
                    }

                    break;
                case "initial_input":
                    reader.skipValue();
                    break;
                case "dateobs":
                    try {
                        @SuppressLint("SimpleDateFormat")
                        final Date date = new SimpleDateFormat(getDateFormat()).parse(reader.nextString());
                        input.setDate(date);
                    }
                    catch (ParseException pe) {
                        Log.w(TAG,
                              pe.getMessage(),
                              pe);
                    }
                    break;
                case "observers_id":
                    if (reader.peek() != JsonToken.NULL) {
                        readObservers(reader,
                                      input.getObservers());
                    }

                    break;
                case "taxons":
                    if (reader.peek() != JsonToken.NULL) {
                        readTaxa(reader,
                                 input.getTaxa());
                    }

                    break;
                default:
                    onInputJsonReaderListener.readAdditionalInputData(reader,
                                                                      keyName,
                                                                      input);
                    break;
            }
        }

        reader.endObject();

        return input;
    }

    private void readObservers(@NonNull final JsonReader reader,
                               @NonNull final Map<Long, Observer> observers) throws
                                                                             IOException {
        reader.beginArray();

        while (reader.hasNext()) {
            switch (reader.peek()) {
                case BEGIN_OBJECT:
                    final Observer observer = readObserver(reader);
                    observers.put(observer.getObserverId(),
                                  observer);
                    break;
                case NUMBER:
                    final Observer observerAsId = new Observer(reader.nextLong(),
                                                               "",
                                                               "");
                    observers.put(observerAsId.getObserverId(),
                                  observerAsId);
                    break;
            }
        }

        reader.endArray();
    }

    @NonNull
    private Observer readObserver(@NonNull final JsonReader reader) throws
                                                                    IOException {
        final Observer observer = new Observer();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "id":
                    observer.mObserverId = reader.nextLong();
                    break;
                case "lastname":
                    observer.setLastname(reader.nextString());
                    break;
                case "firstname":
                    observer.setFirstname(reader.nextString());
                    break;
            }
        }

        reader.endObject();

        return observer;
    }

    private void readTaxa(@NonNull final JsonReader reader,
                          @NonNull final Map<Long, AbstractTaxon> taxa) throws
                                                                        IOException {
        reader.beginArray();

        while (reader.hasNext()) {
            final AbstractTaxon taxon = readTaxon(reader);
            taxa.put(taxon.getId(),
                     taxon);
        }

        reader.endArray();
    }

    @NonNull
    private AbstractTaxon readTaxon(@NonNull final JsonReader reader) throws
                                                                      IOException {
        final AbstractTaxon taxon = onInputJsonReaderListener.createTaxon();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "id":
                    taxon.mId = reader.nextLong();
                    break;
                case "id_taxon":
                    taxon.mTaxonId = reader.nextLong();
                    break;
                case "name_entered":
                    taxon.setNameEntered(reader.nextString());
                    break;
                case "observation":
                    reader.beginObject();

                    while (reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "criterion":
                                taxon.setCriterionId(reader.nextLong());
                        }
                    }

                    reader.endObject();
                    break;
                case "comment":
                    taxon.setComment(reader.nextString());
                    break;
                default:
                    onInputJsonReaderListener.readAdditionalTaxonData(reader,
                                                                      keyName,
                                                                      taxon);
                    break;
            }
        }

        reader.endObject();

        return taxon;
    }

    /**
     * Callback used by {@link InputJsonReader}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnInputJsonReaderListener {

        /**
         * Returns a new instance of {@link AbstractInput}.
         *
         * @return new instance of {@link AbstractInput}
         */
        @NonNull
        AbstractInput createInput();

        /**
         * Returns a new instance of {@link AbstractTaxon}.
         *
         * @return new instance of {@link AbstractTaxon} to use
         */
        @NonNull
        AbstractTaxon createTaxon();

        /**
         * Reading some additional data to set to the given {@link AbstractInput}.
         *
         * @param reader  the current @code JsonReader} to use
         * @param keyName the JSON key read
         * @param input   the current {@link AbstractInput} to use
         *
         * @throws IOException
         */
        void readAdditionalInputData(@NonNull final JsonReader reader,
                                     @NonNull final String keyName,
                                     @NonNull final AbstractInput input) throws
                                                                         IOException;

        /**
         * Reading some additional data to set to the given {@link AbstractTaxon}.
         *
         * @param reader  the current @code JsonReader} to use
         * @param keyName the JSON key read
         * @param taxon   the current {@link AbstractTaxon} to use
         *
         * @throws IOException
         */
        void readAdditionalTaxonData(@NonNull final JsonReader reader,
                                     @NonNull final String keyName,
                                     @NonNull final AbstractTaxon taxon) throws
                                                                         IOException;
    }
}
