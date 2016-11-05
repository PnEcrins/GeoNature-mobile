package com.makina.ecrins.maps.jts.geojson.io;

import android.support.annotation.NonNull;

import com.makina.ecrins.maps.jts.geojson.AbstractGeoJson;
import com.makina.ecrins.maps.jts.geojson.Feature;

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
                             @NonNull final WKTFileReader.OnWKTFileReaderListener listener) throws
                                                                                            IOException {
        final LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(wkt));

        // noinspection StatementWithEmptyBody
        while (lineNumberReader.readLine() != null) {

        }

        listener.onStart(lineNumberReader.getLineNumber());
        wktReader.readFeatures(new FileReader(wkt),
                               listener);
    }

    /**
     * Callback used by {@link WKTFileReader}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnWKTFileReaderListener
            extends WKTReader.OnWKTReaderListener {

        void onStart(int size);
    }
}
