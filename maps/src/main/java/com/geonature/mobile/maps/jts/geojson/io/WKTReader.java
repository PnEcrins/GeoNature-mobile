package com.geonature.mobile.maps.jts.geojson.io;

import android.support.annotation.NonNull;
import android.util.Log;

import com.geonature.mobile.maps.jts.geojson.Feature;
import com.geonature.mobile.maps.jts.geojson.FeatureCollection;
import com.geonature.mobile.maps.jts.geojson.AbstractGeoJson;
import com.vividsolutions.jts.io.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a GeoJSON in Well-Known Text format to a {@link AbstractGeoJson} implementation.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class WKTReader {

    private static final String TAG = WKTReader.class.getName();

    private final Pattern wktLinePattern = Pattern.compile("^([0-9]+),([A-Z]+\\(.+\\))$");
    private final com.vividsolutions.jts.io.WKTReader wktReader;

    public WKTReader() {
        wktReader = new com.vividsolutions.jts.io.WKTReader();
    }

    /**
     * parse a Well-Known Text format reader to convert as {@link Feature}.
     *
     * @param in       the {@code Reader} to use
     * @param listener the callback to monitor the progression
     */
    public void readFeatures(@NonNull final Reader in,
                             @NonNull final OnWKTReaderListener listener) {
        final FeatureCollection featureCollection = new FeatureCollection();
        final BufferedReader bufferedReader = new BufferedReader(in);
        int currentLine = 0;
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                final Matcher matcher = wktLinePattern.matcher(line);

                if (matcher.matches()) {
                    try {
                        final Feature feature = new Feature(matcher.group(1),
                                                            wktReader.read(matcher.group(2)));
                        featureCollection.addFeature(feature);

                        listener.onProgress(currentLine + 1,
                                            feature);
                    }
                    catch (ParseException pe) {
                        Log.w(TAG,
                              pe.getMessage());
                    }
                }

                currentLine++;
            }

            listener.onFinish(featureCollection);
            bufferedReader.close();
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());

            listener.onError(ioe);
        }
    }

    /**
     * parse a Well-Known Text format reader to convert as {@link Feature}.
     *
     * @param in the {@code Reader} to use
     *
     * @return a {@link Feature} instance from the {@code Reader}
     */
    @NonNull
    public List<Feature> readFeatures(@NonNull final Reader in) {
        final List<Feature> features = new ArrayList<>();
        readFeatures(in,
                     new OnWKTReaderListener() {
                         @Override
                         public void onProgress(int progress,
                                                @NonNull Feature feature) {
                             features.add(feature);
                         }

                         @Override
                         public void onFinish(@NonNull FeatureCollection featureCollection) {
                         }

                         @Override
                         public void onError(Throwable t) {
                         }
                     });

        return features;
    }

    /**
     * parse a Well-Known Text format reader to convert as {@link FeatureCollection}.
     *
     * @param in the {@code Reader} to use
     *
     * @return a {@link FeatureCollection} instance from the {@code Reader}
     */
    @NonNull
    public FeatureCollection readFeatureCollection(@NonNull final Reader in) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.addAllFeatures(readFeatures(in));

        return featureCollection;
    }

    /**
     * Callback used by {@link WKTReader}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnWKTReaderListener {

        void onProgress(int progress,
                        @NonNull final Feature feature);

        void onFinish(@NonNull final FeatureCollection featureCollection);

        void onError(Throwable t);
    }
}
