package com.makina.ecrins.maps.jts.geojson.io;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.makina.ecrins.maps.jts.geojson.AbstractGeoJson;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding
 * {@link AbstractGeoJson} implementation.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946">https://tools.ietf.org/html/rfc7946</a>
 * @see GeoJsonWriter
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeoJsonReader {

    private static final String TAG = GeoJsonReader.class.getName();

    private final GeometryFactory gf;

    public GeoJsonReader() {
        gf = new GeometryFactory();
    }

    /**
     * parse a {@code JSON} string to convert as {@link Feature}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link Feature} instance from the {@code JSON} string or {@code null} if something goes wrong
     *
     * @see #readFeature(Reader)
     */
    @Nullable
    public Feature readFeature(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return readFeature(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link Feature}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link Feature} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public Feature readFeature(@NonNull final Reader in) throws
                                                         IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final Feature feature = readFeature(jsonReader);
        jsonReader.close();

        return feature;
    }

    /**
     * parse a {@code JSON} string to convert as {@link FeatureCollection}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link FeatureCollection} instance from the {@code JSON} string or {@code null} if something goes wrong
     *
     * @see #readFeatureCollection(Reader)
     */
    @Nullable
    public FeatureCollection readFeatureCollection(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return readFeatureCollection(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link FeatureCollection}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link FeatureCollection} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public FeatureCollection readFeatureCollection(@NonNull final Reader in) throws
                                                                             IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final FeatureCollection featureCollection = readFeatureCollection(jsonReader);
        jsonReader.close();

        return featureCollection;
    }

    /**
     * parse a {@code JSON} string to convert as {@code Geometry}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@code Geometry} instance from the {@code JSON} string or {@code null} if something goes wrong
     *
     * @see #readGeometry(Reader)
     */
    @Nullable
    public Geometry readGeometry(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return readGeometry(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@code Geometry}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@code Geometry} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public Geometry readGeometry(@NonNull final Reader in) throws
                                                           IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final Geometry geometry = readGeometry(jsonReader);
        jsonReader.close();

        return geometry;
    }

    @NonNull
    public Feature readFeature(@NonNull final JsonReader reader) throws
                                                                  IOException {
        String id = null;
        String type = null;
        Geometry geometry = null;
        Bundle bundle = null;

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "id":
                    id = reader.nextString();
                    break;
                case "type":
                    type = reader.nextString();
                    break;
                case "geometry":
                    geometry = readGeometry(reader);
                    break;
                case "properties":
                    bundle = readProperties(reader);
                    break;
            }
        }

        reader.endObject();

        if (TextUtils.isEmpty(id)) {
            throw new IOException("No id found for feature");
        }

        if (!"Feature".equals(type)) {
            throw new IOException("No such type found for feature " + id);
        }

        if (geometry == null) {
            throw new IOException("No geometry found for feature " + id);
        }

        final Feature feature = new Feature(id,
                                            geometry);

        if ((bundle != null) && !bundle.isEmpty()) {
            feature.getProperties()
                   .putAll(bundle);
        }

        return feature;
    }

    @NonNull
    private FeatureCollection readFeatureCollection(@NonNull final JsonReader reader) throws
                                                                                      IOException {
        final FeatureCollection featureCollection = new FeatureCollection();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "type":
                    if (!"FeatureCollection".equals(reader.nextString())) {
                        throw new IOException("No such type found for FeatureCollection");
                    }

                    break;
                case "features":
                    reader.beginArray();

                    while (reader.hasNext()) {
                        featureCollection.addFeature(readFeature(reader));
                    }

                    reader.endArray();
                    break;
            }
        }

        reader.endObject();

        return featureCollection;
    }

    @NonNull
    private Geometry readGeometry(@NonNull final JsonReader reader) throws
                                                                    IOException {
        reader.beginObject();
        final String nextName = reader.nextName();

        if (!nextName.equals("type")) {
            throw new IOException("Expected 'type' property but was " + nextName);
        }

        final String type = reader.nextString();
        final Geometry geometry;

        switch (type) {
            case "Point":
                geometry = readPoint(reader);
                break;
            case "MultiPoint":
                geometry = readMultiPoint(reader);
                break;
            case "LineString":
                geometry = readLineString(reader,
                                          true);
                break;
            case "MultiLineString":
                geometry = readMultiLineString(reader);
                break;
            case "Polygon":
                geometry = readPolygon(reader,
                                       true);
                break;
            case "MultiPolygon":
                geometry = readMultiPolygon(reader);
                break;
            case "GeometryCollection":
                geometry = readGeometryCollection(reader);
                break;
            default:
                throw new IOException("No such geometry " + type);
        }

        reader.endObject();

        return geometry;
    }

    @NonNull
    private Point readPoint(@NonNull final JsonReader reader) throws
                                                              IOException {
        final String nextName = reader.nextName();

        if (!nextName.equals("coordinates")) {
            throw new IOException("Expected 'coordinates' property but was " + nextName);
        }

        return gf.createPoint(readCoordinate(reader));
    }

    @NonNull
    private MultiPoint readMultiPoint(@NonNull final JsonReader reader) throws
                                                                        IOException {
        final String nextName = reader.nextName();

        if (!nextName.equals("coordinates")) {
            throw new IOException("Expected 'coordinates' property but was " + nextName);
        }

        return gf.createMultiPoint(readCoordinates(reader));
    }

    @NonNull
    private LineString readLineString(@NonNull final JsonReader reader,
                                      boolean readCoordinatesJsonKey) throws
                                                                      IOException {
        if (readCoordinatesJsonKey) {
            final String nextName = reader.nextName();

            if (!nextName.equals("coordinates")) {
                throw new IOException("Expected 'coordinates' property but was " + nextName);
            }
        }

        return gf.createLineString(readCoordinates(reader));
    }

    @NonNull
    private MultiLineString readMultiLineString(@NonNull final JsonReader reader) throws
                                                                                  IOException {
        final String nextName = reader.nextName();

        if (!nextName.equals("coordinates")) {
            throw new IOException("Expected 'coordinates' property but was " + nextName);
        }

        final List<LineString> lineStrings = new ArrayList<>();

        reader.beginArray();

        while (reader.hasNext()) {
            lineStrings.add(readLineString(reader,
                                           false));
        }

        reader.endArray();

        return gf.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
    }

    @NonNull
    private Polygon readPolygon(@NonNull final JsonReader reader,
                                boolean readCoordinatesJsonKey) throws
                                                                IOException {
        if (readCoordinatesJsonKey) {
            final String nextName = reader.nextName();

            if (!nextName.equals("coordinates")) {
                throw new IOException("Expected 'coordinates' property but was " + nextName);
            }
        }

        final List<LinearRing> linearRings = new ArrayList<>();

        reader.beginArray();

        while (reader.hasNext()) {
            linearRings.add(gf.createLinearRing(readCoordinates(reader)));
        }

        reader.endArray();

        if (linearRings.isEmpty()) {
            throw new IOException("No coordinates defined for polygon");
        }

        // this is a polygon with no holes defined
        if (linearRings.size() == 1) {
            return gf.createPolygon(linearRings.get(0));
        }
        else {
            return gf.createPolygon(linearRings.get(0),
                                    linearRings.subList(1,
                                                        linearRings.size())
                                               .toArray(new LinearRing[linearRings.size() - 1]));
        }
    }

    @NonNull
    private MultiPolygon readMultiPolygon(@NonNull final JsonReader reader) throws
                                                                            IOException {
        final String nextName = reader.nextName();

        if (!nextName.equals("coordinates")) {
            throw new IOException("Expected 'coordinates' property but was " + nextName);
        }

        final List<Polygon> polygons = new ArrayList<>();

        reader.beginArray();

        while (reader.hasNext()) {
            polygons.add(readPolygon(reader,
                                     false));
        }

        reader.endArray();

        return gf.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }

    @NonNull
    private GeometryCollection readGeometryCollection(@NonNull final JsonReader reader) throws
                                                                                        IOException {
        final String nextName = reader.nextName();

        if (!nextName.equals("geometries")) {
            throw new IOException("Expected 'geometries' property but was " + nextName);
        }

        final List<Geometry> geometries = new ArrayList<>();

        reader.beginArray();

        while (reader.hasNext()) {
            geometries.add(readGeometry(reader));
        }

        reader.endArray();

        return gf.createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
    }

    @NonNull
    private Coordinate[] readCoordinates(@NonNull final JsonReader reader) throws
                                                                           IOException {
        final List<Coordinate> coordinates = new ArrayList<>();

        reader.beginArray();

        while (reader.hasNext()) {
            coordinates.add(readCoordinate(reader));
        }

        reader.endArray();

        return coordinates.toArray(new Coordinate[coordinates.size()]);
    }

    @NonNull
    private Coordinate readCoordinate(@NonNull final JsonReader reader) throws
                                                                        IOException {
        final Coordinate coordinate = new Coordinate();
        int ordinateIndex = 0;

        reader.beginArray();

        while (reader.hasNext()) {
            final JsonToken jsonToken = reader.peek();

            switch (jsonToken) {
                case NUMBER:
                    if (ordinateIndex < 3) {
                        coordinate.setOrdinate(ordinateIndex,
                                               reader.nextDouble());
                        ordinateIndex++;
                    }

                    break;
            }
        }

        reader.endArray();

        return coordinate;
    }

    @NonNull
    private Bundle readProperties(@NonNull final JsonReader reader) throws
                                                                    IOException {
        final Bundle bundle = new Bundle();

        if (reader.peek()
                  .equals(JsonToken.BEGIN_OBJECT)) {
            reader.beginObject();
            String key = null;

            while (reader.hasNext()) {
                final JsonToken jsonToken = reader.peek();

                switch (jsonToken) {
                    case NAME:
                        key = reader.nextName();
                        break;
                    case STRING:
                        if (!TextUtils.isEmpty(key)) {
                            bundle.putString(key,
                                             reader.nextString());
                        }

                        break;
                    case NUMBER:
                        if (!TextUtils.isEmpty(key)) {
                            bundle.putDouble(key,
                                             reader.nextDouble());
                        }

                        break;
                    case BEGIN_OBJECT:
                        if (!TextUtils.isEmpty(key)) {
                            bundle.putBundle(key,
                                             readProperties(reader));
                        }

                        break;
                }
            }

            reader.endObject();
        }

        return bundle;
    }
}
