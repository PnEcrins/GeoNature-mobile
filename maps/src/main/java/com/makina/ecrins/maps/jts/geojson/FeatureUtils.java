package com.makina.ecrins.maps.jts.geojson;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.maps.jts.geojson.io.GeoJsonReader;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Helper class about {@link Feature} instances.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class FeatureUtils {

    private static final String TAG = FeatureUtils.class.getSimpleName();

    /**
     * Convert the given {@link Feature} as {@code JSON} string.
     *
     * @param feature the {@link Feature} to convert
     *
     * @return a {@code JSON} string representation of the given {@link Feature} or {@code null} if something goes wrong
     */
    @Nullable
    public static String toJson(@Nullable final Feature feature) {
        if (feature == null) {
            return null;
        }

        final StringWriter writer = new StringWriter();

        try {
            new GeoJsonWriter().write(writer,
                                      feature);
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());

            return null;
        }

        return writer.toString();
    }

    /**
     * Convert the given {@link FeatureCollection} as {@code JSON} string.
     *
     * @param featureCollection the {@link FeatureCollection} to convert
     *
     * @return a {@code JSON} string representation of the given {@link FeatureCollection} or {@code null} if something goes wrong
     */
    @Nullable
    public static String toJson(@Nullable final FeatureCollection featureCollection) {
        if (featureCollection == null) {
            return null;
        }

        final StringWriter writer = new StringWriter();

        try {
            new GeoJsonWriter().write(writer,
                                      featureCollection);
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());

            return null;
        }

        return writer.toString();
    }

    /**
     * parse a {@code JSON} string to convert as {@link Feature}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link Feature} instance from the {@code JSON} string or {@code null} if something goes wrong
     */
    @Nullable
    public static Feature featureFromJson(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return new GeoJsonReader().readFeature(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} string to convert as {@link FeatureCollection}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link FeatureCollection} instance from the {@code JSON} string or {@code null} if something goes wrong
     */
    @Nullable
    public static FeatureCollection featureCollectionFromJson(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return new GeoJsonReader().readFeatureCollection(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }
}
