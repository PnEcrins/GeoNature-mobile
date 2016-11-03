package com.makina.ecrins.maps.jts.geojson;

import android.support.annotation.NonNull;

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

/**
 * Helper class about JTS.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class JTSTestHelper {

    @NonNull
    public static Coordinate createCoordinate(@NonNull final GeometryFactory gf,
                                              double latitude,
                                              double longitude) {
        return new Coordinate(longitude,
                              latitude);
    }

    @NonNull
    public static Point createPoint(@NonNull final GeometryFactory gf,
                                    double latitude,
                                    double longitude) {
        return gf.createPoint(createCoordinate(gf,
                                               latitude,
                                               longitude));
    }

    @NonNull
    public static MultiPoint createMultiPoint(@NonNull final GeometryFactory gf,
                                              @NonNull final Point... points) {
        return gf.createMultiPoint(points);
    }

    @NonNull
    public static LineString createLineString(@NonNull final GeometryFactory gf,
                                              @NonNull final Coordinate... coordinates) {
        return gf.createLineString(coordinates);
    }

    @NonNull
    public static MultiLineString createMultiLineString(@NonNull final GeometryFactory gf,
                                                        @NonNull final LineString... lineStrings) {
        return gf.createMultiLineString(lineStrings);
    }

    @NonNull
    public static LinearRing createLinearRing(@NonNull final GeometryFactory gf,
                                              @NonNull final Coordinate... coordinates) {
        return gf.createLinearRing(coordinates);
    }

    @NonNull
    public static Polygon createPolygon(@NonNull final GeometryFactory gf,
                                        @NonNull final Coordinate... coordinates) {
        return gf.createPolygon(coordinates);
    }

    @NonNull
    public static Polygon createPolygon(@NonNull final GeometryFactory gf,
                                        @NonNull final LinearRing shell,
                                        @NonNull final LinearRing... holes) {
        return gf.createPolygon(shell,
                                holes);
    }

    @NonNull
    public static MultiPolygon createMultiPolygon(@NonNull final GeometryFactory gf,
                                                  @NonNull final Polygon... polygons) {
        return gf.createMultiPolygon(polygons);
    }

    @NonNull
    public static GeometryCollection createGeometryCollection(@NonNull final GeometryFactory gf,
                                                              @NonNull final Geometry... geometries) {
        return gf.createGeometryCollection(geometries);
    }
}
