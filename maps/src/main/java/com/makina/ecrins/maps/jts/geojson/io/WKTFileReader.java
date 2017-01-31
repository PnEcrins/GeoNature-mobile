package com.makina.ecrins.maps.jts.geojson.io;

import android.support.annotation.NonNull;

import com.makina.ecrins.maps.jts.geojson.AbstractGeoJson;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Converts a GeoJSON in Well-Known Text format from {@code File} to a {@link AbstractGeoJson} implementation.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see WKTReader
 */
public class WKTFileReader {

    private final WKTReader wktReader;
    private int lineNumber;

    public WKTFileReader() {
        this.wktReader = new WKTReader();
    }

    /**
     * parse a Well-Known Text format reader to convert as {@link Feature}.
     *
     * @param wkt      the WKT {@code File} to read
     * @param listener the callback to monitor the progression
     */
    public void readFeatures(@NonNull final File wkt,
                             @NonNull final WKTFileReader.OnWKTFileReaderListener listener) {
        try {
            final LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(wkt));

            // noinspection StatementWithEmptyBody
            while (lineNumberReader.readLine() != null) {

            }

            lineNumber = lineNumberReader.getLineNumber();
            listener.onStart(lineNumber);

            final WKTReader.OnWKTReaderListener onWKTReaderListener = new WKTReader.OnWKTReaderListener() {
                @Override
                public void onProgress(int progress,
                                       @NonNull Feature feature) {
                    listener.onProgress(progress,
                                        lineNumber,
                                        feature);
                }

                @Override
                public void onFinish(@NonNull FeatureCollection featureCollection) {
                    listener.onFinish(featureCollection);
                }

                @Override
                public void onError(Throwable t) {
                    listener.onError(t);
                }
            };

            wktReader.readFeatures(new FileReader(wkt),
                                   onWKTReaderListener);
        }
        catch (IOException ioe) {
            listener.onError(ioe);
        }
    }

    /**
     * Callback used by {@link WKTFileReader}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnWKTFileReaderListener
            extends WKTReader.OnWKTReaderListener {

        void onStart(int size);

        void onProgress(int progress,
                        int size,
                        @NonNull final Feature feature);
    }
}
