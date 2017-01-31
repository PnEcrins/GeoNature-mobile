package com.makina.ecrins.maps.jts.geojson.io;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;

import com.makina.ecrins.maps.jts.geojson.AbstractGeoJson;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Default {@code JsonWriter} about writing an {@link AbstractGeoJson} as {@code JSON}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946">https://tools.ietf.org/html/rfc7946</a>
 * @see GeoJsonReader
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeoJsonWriter {

    private static final String TAG = GeoJsonWriter.class.getSimpleName();

    /**
     * Convert the given {@link Feature} as {@code JSON} string.
     *
     * @param feature the {@link Feature} to convert
     *
     * @return a {@code JSON} string representation of the given {@link Feature} or {@code null} if something goes wrong
     *
     * @see #write(Writer, Feature)
     */
    @Nullable
    public String write(@Nullable final Feature feature) {
        if (feature == null) {
            return null;
        }

        final StringWriter writer = new StringWriter();

        try {
            write(writer,
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
     * Convert the given {@link Feature} as {@code JSON} and write it to the given {@code Writer}.
     *
     * @param out     the {@code Writer} to use
     * @param feature the {@link Feature} to convert
     *
     * @throws IOException if something goes wrong
     */
    public void write(@NonNull final Writer out,
                      @NonNull final Feature feature) throws
                                                      IOException {
        final JsonWriter writer = new JsonWriter(out);
        writeFeature(writer,
                     feature);
        writer.flush();
        writer.close();
    }

    /**
     * Convert the given {@link FeatureCollection} as {@code JSON} string.
     *
     * @param featureCollection the {@link FeatureCollection} to convert
     *
     * @return a {@code JSON} string representation of the given {@link FeatureCollection} or {@code null} if something goes wrong
     *
     * @see #write(Writer, FeatureCollection)
     */
    @Nullable
    public String write(@Nullable final FeatureCollection featureCollection) {
        if (featureCollection == null) {
            return null;
        }

        final StringWriter writer = new StringWriter();

        try {
            write(writer,
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
     * Convert the given {@link FeatureCollection} as {@code JSON} and write it to the given {@code Writer}.
     *
     * @param out               the {@code Writer} to use
     * @param featureCollection the {@link FeatureCollection} to convert
     *
     * @throws IOException if something goes wrong
     */
    public void write(@NonNull final Writer out,
                      @NonNull final FeatureCollection featureCollection) throws
                                                                          IOException {
        final JsonWriter writer = new JsonWriter(out);
        writeFeatureCollection(writer,
                               featureCollection);
        writer.flush();
        writer.close();
    }

    public void writeFeature(@NonNull final JsonWriter writer,
                             @NonNull final Feature feature) throws
                                                             IOException {
        writer.beginObject();
        writer.name("id")
              .value(feature.getId());
        writer.name("type")
              .value(feature.getType());
        writer.name("geometry");
        writeGeometry(writer,
                      feature.getGeometry());
        writeProperties(writer,
                        feature.getProperties());
        writer.endObject();
    }

    private void writeFeatureCollection(@NonNull final JsonWriter writer,
                                        @NonNull final FeatureCollection featureCollection) throws
                                                                                            IOException {
        writer.beginObject();
        writer.name("type")
              .value(featureCollection.getType());
        writer.name("features");
        writer.beginArray();

        for (Feature feature : featureCollection.getFeatures()) {
            writeFeature(writer,
                         feature);
        }

        writer.endArray();
        writer.endObject();
    }

    private void writeGeometry(@NonNull final JsonWriter writer,
                               @NonNull final Geometry geometry) throws
                                                                 IOException {
        if (TextUtils.isEmpty(geometry.getGeometryType())) {
            throw new IOException("invalid geometry type");
        }

        switch (geometry.getGeometryType()) {
            case "Point":
                writePoint(writer,
                           (Point) geometry);
                break;
            case "MultiPoint":
                writeMultiPoint(writer,
                                (MultiPoint) geometry);
                break;
            case "LineString":
                writeLineString(writer,
                                (LineString) geometry);
                break;
            case "MultiLineString":
                writeMultiLineString(writer,
                                     (MultiLineString) geometry);
                break;
            case "Polygon":
                writePolygon(writer,
                             (Polygon) geometry);
                break;
            case "MultiPolygon":
                writeMultiPolygon(writer,
                                  (MultiPolygon) geometry);
                break;
            case "GeometryCollection":
                writer.beginObject();
                writer.name("type")
                      .value(geometry.getGeometryType());
                writer.name("geometries");
                writer.beginArray();

                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    writeGeometry(writer,
                                  geometry.getGeometryN(i));
                }

                writer.endArray();
                writer.endObject();
                break;
        }
    }

    private void writePoint(@NonNull final JsonWriter writer,
                            @NonNull final Point point) throws
                                                        IOException {
        writer.beginObject();
        writer.name("type")
              .value(point.getGeometryType());
        writer.name("coordinates");
        writeCoordinateSequence(writer,
                                point.getCoordinateSequence());
        writer.endObject();
    }

    private void writeMultiPoint(@NonNull final JsonWriter writer,
                                 @NonNull final MultiPoint multiPoint) throws
                                                                       IOException {
        writer.beginObject();
        writer.name("type")
              .value(multiPoint.getGeometryType());
        writer.name("coordinates");
        writeGeometryCollection(writer,
                                multiPoint);
        writer.endObject();
    }

    private void writeLineString(@NonNull final JsonWriter writer,
                                 @NonNull final LineString lineString) throws
                                                                       IOException {
        writer.beginObject();
        writer.name("type")
              .value(lineString.getGeometryType());
        writer.name("coordinates");
        writeCoordinateSequence(writer,
                                lineString.getCoordinateSequence());
        writer.endObject();
    }

    private void writeMultiLineString(@NonNull final JsonWriter writer,
                                      @NonNull final MultiLineString multiLineString) throws
                                                                                      IOException {
        writer.beginObject();
        writer.name("type")
              .value(multiLineString.getGeometryType());
        writer.name("coordinates");
        writeGeometryCollection(writer,
                                multiLineString);
        writer.endObject();
    }

    private void writePolygon(@NonNull final JsonWriter writer,
                              @NonNull final Polygon polygon) throws
                                                              IOException {
        writer.beginObject();
        writer.name("type")
              .value(polygon.getGeometryType());
        writer.name("coordinates");
        writePolygonCoordinates(writer,
                                polygon);

        writer.endObject();
    }

    private void writePolygonCoordinates(@NonNull final JsonWriter writer,
                                         @NonNull final Polygon polygon) throws
                                                                         IOException {
        writer.beginArray();
        writeCoordinateSequence(writer,
                                polygon.getExteriorRing()
                                       .getCoordinateSequence());

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            writeCoordinateSequence(writer,
                                    polygon.getInteriorRingN(i)
                                           .getCoordinateSequence());
        }

        writer.endArray();
    }

    private void writeMultiPolygon(@NonNull final JsonWriter writer,
                                   @NonNull final MultiPolygon multiPolygon) throws
                                                                             IOException {
        writer.beginObject();
        writer.name("type")
              .value(multiPolygon.getGeometryType());
        writer.name("coordinates");
        writeGeometryCollection(writer,
                                multiPolygon);
        writer.endObject();
    }

    private void writeGeometryCollection(@NonNull final JsonWriter writer,
                                         @NonNull final GeometryCollection geometryCollection) throws
                                                                                               IOException {
        writer.beginArray();

        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            final Geometry geometry = geometryCollection.getGeometryN(i);

            if (TextUtils.isEmpty(geometry.getGeometryType())) {
                Log.w(TAG,
                      "invalid geometry type");
                continue;
            }

            switch (geometry.getGeometryType()) {
                case "Point":
                    writeCoordinateSequence(writer,
                                            ((Point) geometry).getCoordinateSequence());
                    break;
                case "LineString":
                    writeCoordinateSequence(writer,
                                            ((LineString) geometry).getCoordinateSequence());
                    break;
                case "Polygon":
                    writePolygonCoordinates(writer,
                                            (Polygon) geometry);

                    break;
            }
        }

        writer.endArray();
    }

    private void writeCoordinateSequence(@NonNull final JsonWriter writer,
                                         @NonNull final CoordinateSequence coordinateSequence) throws
                                                                                               IOException {
        if (coordinateSequence.size() > 1) {
            writer.beginArray();
        }

        for (int i = 0; i < coordinateSequence.size(); i++) {
            writer.beginArray();
            writer.value(coordinateSequence.getOrdinate(i,
                                                        CoordinateSequence.X));
            writer.value(coordinateSequence.getOrdinate(i,
                                                        CoordinateSequence.Y));

            if (coordinateSequence.getDimension() > 2) {
                double z = coordinateSequence.getOrdinate(i,
                                                          CoordinateSequence.Z);

                if (!Double.isNaN(z)) {
                    writer.value(z);
                }
            }

            writer.endArray();
        }

        if (coordinateSequence.size() > 1) {
            writer.endArray();
        }
    }

    private void writeProperties(@NonNull final JsonWriter writer,
                                 @NonNull final Bundle properties) throws
                                                                   IOException {
        writer.name("properties");
        writeBundle(writer,
                    properties);
    }

    private void writeBundle(@NonNull final JsonWriter writer,
                             @NonNull final Bundle bundle) throws
                                                           IOException {
        writer.beginObject();

        for (String key : bundle.keySet()) {
            final Object value = bundle.get(key);

            if (value instanceof String) {
                writer.name(key)
                      .value((String) value);
            }

            if (value instanceof Number) {
                writer.name(key)
                      .value((Number) value);
            }

            if (value instanceof Bundle) {
                writer.name(key);
                writeBundle(writer,
                            (Bundle) value);
            }

            // TODO: manage other types
        }

        writer.endObject();
    }
}
