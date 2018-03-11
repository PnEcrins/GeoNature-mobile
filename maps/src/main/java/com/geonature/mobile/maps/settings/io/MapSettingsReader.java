package com.geonature.mobile.maps.settings.io;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.geonature.mobile.maps.jts.geojson.GeoPoint;
import com.geonature.mobile.maps.settings.CRSSettings;
import com.geonature.mobile.maps.settings.LayerSettings;
import com.geonature.mobile.maps.settings.MapSettings;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding
 * {@link MapSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MapSettingsReader {

    private static final String TAG = MapSettingsReader.class.getName();

    /**
     * parse a {@code JSON} string to convert as {@link MapSettings}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link MapSettings} instance from the {@code JSON} string or {@code null} if something goes wrong
     *
     * @see #readMapSettings(Reader)
     */
    @Nullable
    public MapSettings readMapSettings(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return readMapSettings(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link MapSettings}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link MapSettings} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     * @see #readMapSettings(JsonReader)
     */
    @NonNull
    public MapSettings readMapSettings(@NonNull final Reader in) throws
                                                                 IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final MapSettings mapSettings = readMapSettings(jsonReader);
        jsonReader.close();

        return mapSettings;
    }

    /**
     * Use a {@code JsonReader} instance to convert as {@link MapSettings}.
     *
     * @param reader the {@code JsonReader} to use
     *
     * @return a {@link MapSettings} instance from the {@code JsonReader}
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public MapSettings readMapSettings(@NonNull final JsonReader reader) throws
                                                                         IOException {
        final MapSettings.Builder builder = MapSettings.Builder.newInstance();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "display_scale":
                    builder.showScale(reader.nextBoolean());
                    break;
                case "show_unities_layer":
                    builder.showUnitiesLayer(reader.nextBoolean());
                    break;
                case "crs":
                    if (reader.peek() != JsonToken.NULL) {
                        builder.setCRSSettings(readCRSSettings(reader));
                    }
                    else {
                        reader.nextNull();
                    }

                    break;
                case "max_bounds":
                    final List<GeoPoint> maxBounds = new ArrayList<>();

                    reader.beginArray();

                    while (reader.hasNext()) {
                        final List<Double> tokens = new ArrayList<>();
                        reader.beginArray();

                        while (reader.hasNext()) {
                            tokens.add(reader.nextDouble());
                        }

                        reader.endArray();

                        if (tokens.size() == 2) {
                            maxBounds.add(new GeoPoint(tokens.get(0),
                                                       tokens.get(1)));
                        }
                    }

                    reader.endArray();

                    builder.setMaxBounds(maxBounds);
                    break;
                case "center":
                    final List<Double> tokens = new ArrayList<>();
                    reader.beginArray();

                    while (reader.hasNext()) {
                        tokens.add(reader.nextDouble());
                    }

                    reader.endArray();

                    if (tokens.size() == 2) {
                        builder.setCenter(new GeoPoint(tokens.get(0),
                                                       tokens.get(1)));
                    }

                    break;
                case "start_zoom":
                case "zoom":
                    builder.setZoom(reader.nextInt());
                    break;
                case "min_zoom":
                    builder.setMinZoom(reader.nextInt());
                    break;
                case "max_zoom":
                    builder.setMaxZoom(reader.nextInt());
                    break;
                case "min_zoom_pointing":
                    builder.setMinZoomPointing(reader.nextInt());
                    break;
                case "layers":
                    readLayerSettingsAsList(reader,
                                            builder);
                    break;
                case "unity_layer":
                    final LayerSettings layerSettings = readLayerSettings(reader);

                    if (layerSettings != null) {
                        builder.setUnitiesLayer(layerSettings);
                    }

                    break;
            }
        }

        reader.endObject();

        return builder.build();
    }

    @Nullable
    private CRSSettings readCRSSettings(@NonNull final JsonReader reader) throws
                                                                          IOException {
        reader.beginObject();

        String code = null;
        String def = null;
        final List<Integer> bbox = new ArrayList<>();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "code":
                    code = reader.nextString();
                    break;
                case "def":
                    def = reader.nextString();
                    break;
                case "bbox":
                    reader.beginArray();

                    while (reader.hasNext()) {
                        bbox.add(reader.nextInt());
                    }

                    reader.endArray();
                    break;
            }
        }

        reader.endObject();

        if (!TextUtils.isEmpty(code) && !TextUtils.isEmpty(def)) {
            return new CRSSettings(code,
                                   def,
                                   bbox);
        }
        return null;
    }

    private void readLayerSettingsAsList(@NonNull final JsonReader reader,
                                         @NonNull final MapSettings.Builder builder) throws
                                                                                     IOException {
        reader.beginArray();

        while (reader.hasNext()) {
            final LayerSettings layerSettings = readLayerSettings(reader);

            if (layerSettings != null) {
                builder.addLayerSettings(layerSettings);
            }
        }

        reader.endArray();
    }

    @Nullable
    private LayerSettings readLayerSettings(@NonNull final JsonReader reader) throws
                                                                              IOException {
        reader.beginObject();

        String name = null;
        String label = null;
        String source = null;

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "name":
                    name = reader.nextString();
                    break;
                case "label":
                    label = reader.nextString();
                    break;
                case "source":
                    source = reader.nextString();
                    break;
            }
        }

        reader.endObject();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(label) && !TextUtils.isEmpty(source)) {
            return new LayerSettings(name,
                                     label,
                                     source);
        }

        return null;
    }
}

