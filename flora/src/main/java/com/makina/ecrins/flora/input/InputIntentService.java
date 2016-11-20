package com.makina.ecrins.flora.input;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractInputIntentService;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonReader;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Custom {@code IntentService} to read, save or export as JSON file a given {@link Input}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InputIntentService
        extends AbstractInputIntentService {

    @NonNull
    @Override
    public AbstractInput createInput() {
        return new Input();
    }

    @NonNull
    @Override
    public AbstractTaxon createTaxon() {
        return new Taxon(0L);
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
        switch (keyName) {
            case "areas":
                readAreas(reader,
                          ((Taxon) taxon).getAreas());
                break;
            case "prospecting_area":
                reader.beginObject();

                while (reader.hasNext()) {
                    final JsonToken jsonToken = reader.peek();

                    switch (jsonToken) {
                        case NAME:
                            switch (reader.nextName()) {
                                case "feature":
                                    final JsonToken featureJsonToken = reader.peek();

                                    switch (featureJsonToken) {
                                        case BEGIN_OBJECT:
                                            ((Taxon) taxon).setProspectingArea(new GeoJsonReader().readFeature(reader));
                                            break;
                                    }

                                    break;
                            }
                    }
                }

                reader.endObject();
                break;
        }
    }

    @Override
    public void writeAdditionalInputData(@NonNull JsonWriter writer,
                                         @NonNull AbstractInput input) throws
                                                                       IOException {
        // nothing to do ...
    }

    @Override
    public void writeAdditionalTaxonData(@NonNull JsonWriter writer,
                                         @NonNull AbstractTaxon taxon) throws
                                                                       IOException {
        writeAreas(writer,
                   ((Taxon) taxon).getAreas()
                                  .values());

        writer.name("prospecting_area")
              .beginObject();
        writer.name("feature");

        final Feature prospectingArea = ((Taxon) taxon).getProspectingArea();

        if (prospectingArea == null) {
            writer.nullValue();
        }
        else {
            new GeoJsonWriter().writeFeature(writer,
                                             prospectingArea);
        }

        writer.endObject();
    }

    private void readAreas(@NonNull final JsonReader reader,
                           @NonNull final Map<String, Area> areas) throws
                                                                   IOException {
        reader.beginArray();

        while (reader.hasNext()) {
            final Area area = readArea(reader);

            if (area.getFeature() != null) {
                areas.put(area.getFeature()
                              .getId(),
                          area);
            }
        }

        reader.endArray();
    }

    @NonNull
    private Area readArea(@NonNull final JsonReader reader) throws
                                                            IOException {
        final Area area = new Area();

        reader.beginObject();

        while (reader.hasNext()) {
            final JsonToken jsonToken = reader.peek();

            switch (jsonToken) {
                case NAME:
                    final String keyName = reader.nextName();

                    switch (keyName) {
                        case "id":
                            area.mAreaId = reader.nextLong();
                            break;
                        case "incline":
                            area.setInclineValue(reader.nextDouble());
                            break;
                        case "area":
                            area.setArea(reader.nextDouble());
                            break;
                        case "computed_area":
                            area.setComputedArea(reader.nextDouble());
                            break;
                        case "feature":
                            area.setFeature(new GeoJsonReader().readFeature(reader));
                            break;
                        case "frequency":
                            final JsonToken frequencyJsonToken = reader.peek();

                            switch (frequencyJsonToken) {
                                case BEGIN_OBJECT:
                                    area.setFrequency(readFrequency(reader));
                                    break;
                            }

                            break;
                        case "phenology":
                            area.setPhenologyId(reader.nextLong());
                            break;
                        case "counting":
                            area.setCounting(readCounting(reader));
                            break;
                        case "physiognomy":
                            reader.beginArray();

                            while (reader.hasNext()) {
                                area.getSelectedPhysiognomy()
                                    .add(reader.nextLong());
                            }

                            reader.endArray();
                            break;
                        case "disturbances":
                            reader.beginArray();

                            while (reader.hasNext()) {
                                area.getSelectedDisturbances()
                                    .add(reader.nextLong());
                            }

                            reader.endArray();
                            break;
                        case "comment":
                            area.setComment(reader.nextString());
                            break;
                    }
            }
        }

        reader.endObject();

        return area;
    }

    @NonNull
    private Frequency readFrequency(@NonNull final JsonReader reader) throws
                                                                      IOException {
        reader.beginObject();
        final String nextName = reader.nextName();

        if (!nextName.equals("type")) {
            throw new IOException("Expected 'type' property but was " + nextName);
        }

        final String type = reader.nextString();
        final Frequency.FrequencyType frequencyType = Frequency.FrequencyType.fromValue(type);

        if (frequencyType == null) {
            throw new IOException("No such FrequencyType: " + type);
        }

        final Frequency frequency = new Frequency(frequencyType);

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "computed_recommended_step":
                    frequency.setRecommendedStep(reader.nextDouble());
                    break;
                case "transects":
                    frequency.setTransects(reader.nextInt());
                    break;
                case "transect_yes":
                    frequency.setTransectYes(reader.nextInt());
                    break;
                case "transect_no":
                    frequency.setTransectNo(reader.nextInt());
                    break;
                case "value":
                    frequency.setValue(reader.nextDouble());
                    break;
            }
        }

        reader.endObject();

        return frequency;
    }

    @NonNull
    private Counting readCounting(@NonNull final JsonReader reader) throws
                                                                    IOException {
        reader.beginObject();
        final String nextName = reader.nextName();

        if (!nextName.equals("type")) {
            throw new IOException("Expected 'type' property but was " + nextName);
        }

        final String type = reader.nextString();
        final Counting.CountingType countingType = Counting.CountingType.fromValue(type);

        if (countingType == null) {
            throw new IOException("No such CountingType: " + type);
        }

        final Counting counting = new Counting(countingType);

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "plot_surface":
                    counting.setPlotSurface(reader.nextDouble());
                    break;
                case "plots":
                    counting.setPlots(reader.nextInt());
                    break;
                case "fertile":
                    counting.setCountFertile(reader.nextInt());
                    break;
                case "total_fertile":
                    counting.setTotalFertile(reader.nextInt());
                    break;
                case "sterile":
                    counting.setCountSterile(reader.nextInt());
                    break;
                case "total_sterile":
                    counting.setTotalSterile(reader.nextInt());
                    break;
            }
        }

        reader.endObject();

        return counting;
    }

    private void writeAreas(@NonNull final JsonWriter writer,
                            @NonNull final Collection<Area> areas) throws
                                                                   IOException {
        writer.name("areas");
        writer.beginArray();

        for (Area area : areas) {
            writeArea(writer,
                      area);
        }

        writer.endArray();
    }

    private void writeArea(@NonNull final JsonWriter writer,
                           @NonNull final Area area) throws
                                                     IOException {
        if (area.getFeature() == null) {
            return;
        }

        writer.beginObject();
        writer.name("id")
              .value(area.getAreaId());
        writer.name("incline")
              .value(area.getInclineValue());
        writer.name("area")
              .value(area.getArea());
        writer.name("computed_area")
              .value(area.getComputedArea());
        writer.name("feature");
        new GeoJsonWriter().writeFeature(writer,
                                         area.getFeature());

        writer.name("frequency");
        final Frequency frequency = area.getFrequency();

        if (frequency == null) {
            writer.nullValue();
        }
        else {
            writeFrequency(writer,
                           frequency);
        }

        writer.name("phenology")
              .value(area.getPhenologyId());
        writer.name("counting");
        writeCounting(writer,
                      area.getCounting());

        writer.name("physiognomy");
        writer.beginArray();

        for (Long id : area.getSelectedPhysiognomy()) {
            writer.value(id);
        }

        writer.endArray();

        writer.name("disturbances");
        writer.beginArray();

        for (Long id : area.getSelectedDisturbances()) {
            writer.value(id);
        }

        writer.endArray();

        writer.name("comment")
              .value(area.getComment());
        writer.endObject();
    }

    private void writeFrequency(@NonNull final JsonWriter writer,
                                @NonNull final Frequency frequency) throws
                                                                    IOException {
        writer.beginObject();
        writer.name("type")
              .value(frequency.getType()
                              .getValue());
        writer.name("computed_recommended_step")
              .value(frequency.getRecommendedStep());
        writer.name("transects")
              .value(frequency.getTransects());
        writer.name("transect_yes")
              .value(frequency.getTransectYes());
        writer.name("transect_no")
              .value(frequency.getTransectNo());
        writer.name("value")
              .value(frequency.getValue());
        writer.endObject();
    }

    private void writeCounting(@NonNull final JsonWriter writer,
                               @NonNull final Counting counting) throws
                                                                 IOException {
        writer.beginObject();
        writer.name("type")
              .value(counting.getType()
                             .getValue());
        writer.name("plot_surface")
              .value(counting.getPlotSurface());
        writer.name("plots")
              .value(counting.getPlots());
        writer.name("fertile")
              .value(counting.getCountFertile());
        writer.name("sterile")
              .value(counting.getCountSterile());
        writer.name("total_sterile")
              .value(counting.getTotalSterile());
        writer.endObject();
    }
}
