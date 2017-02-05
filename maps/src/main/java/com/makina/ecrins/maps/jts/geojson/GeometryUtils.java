package com.makina.ecrins.maps.jts.geojson;

import android.support.annotation.NonNull;
import android.util.Log;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import static com.makina.ecrins.maps.jts.geojson.IMathConstants.DEG2RAD;

/**
 * Helper class about {@code Geometry} instances.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class GeometryUtils {

    private static final String TAG = GeometryUtils.class.getName();

    /**
     * Returns the minimum distance between two {@code Geometry} instances.
     *
     * @param fromGeometry the {@code Geometry} to check the distance from
     * @param toGeometry   the {@code Geometry} to check the distance to
     *
     * @return the minimum distance in meters
     *
     * @see GeoPoint#distanceTo(GeoPoint)
     */
    public static double distanceTo(Geometry fromGeometry,
                                    Geometry toGeometry) {
        double distance = Double.MAX_VALUE;

        switch (fromGeometry.getGeometryType()) {
            case "Point":
                final GeoPoint fromGeoPoint = new GeoPoint((Point) fromGeometry);

                switch (toGeometry.getGeometryType()) {
                    case "Point":
                        distance = fromGeoPoint.distanceTo(new GeoPoint((Point) toGeometry));
                        break;
                    case "LineString":
                    case "LinearRing":
                        for (int i = 0; i < toGeometry.getNumPoints(); i++) {
                            double distanceFromPoint = fromGeoPoint.distanceTo(new GeoPoint(((LineString) toGeometry).getPointN(i)));

                            if (distance > distanceFromPoint) {
                                distance = distanceFromPoint;
                            }
                        }

                        break;
                    case "Polygon":
                        distance = distanceTo(fromGeometry,
                                              ((Polygon) toGeometry).getExteriorRing());
                        break;
                    default:
                        Log.w(TAG,
                              "distanceTo: no implementation found for geometry '" + toGeometry.getGeometryType() + "'");
                        break;
                }
                break;
            case "LineString":
            case "LinearRing":
                switch (toGeometry.getGeometryType()) {
                    case "Point":
                        distance = distanceTo(toGeometry,
                                              fromGeometry);
                        break;
                    case "LineString":
                    case "LinearRing":
                        for (int i = 0; i < fromGeometry.getNumPoints(); i++) {
                            for (int j = 0; j < toGeometry.getNumPoints(); j++) {
                                double distanceFromPoint = new GeoPoint(((LineString) fromGeometry).getPointN(i)).distanceTo(new GeoPoint(((LineString) toGeometry).getPointN(i)));

                                if (distance > distanceFromPoint) {
                                    distance = distanceFromPoint;
                                }
                            }
                        }

                        break;
                    case "Polygon":
                        distance = distanceTo(fromGeometry,
                                              ((Polygon) toGeometry).getExteriorRing());
                        break;
                    default:
                        Log.w(TAG,
                              "distanceTo: no implementation found for geometry '" + toGeometry.getGeometryType() + "'");
                        break;
                }
                break;
            case "Polygon":
                switch (toGeometry.getGeometryType()) {
                    case "Point":
                        distance = distanceTo(toGeometry,
                                              fromGeometry);
                        break;
                    case "LineString":
                    case "LinearRing":
                        distance = distanceTo(toGeometry,
                                              fromGeometry);
                        break;
                    case "Polygon":
                        distance = distanceTo(((Polygon) toGeometry).getExteriorRing(),
                                              ((Polygon) fromGeometry).getExteriorRing());
                        break;
                    default:
                        Log.w(TAG,
                              "distanceTo: no implementation found for geometry '" + toGeometry.getGeometryType() + "'");
                        break;
                }
                break;
            default:
                Log.w(TAG,
                      "distanceTo: geometry " + fromGeometry.getGeometryType() + " not implemented");
        }

        return distance;
    }

    /**
     * Calculate the approximate length of a given {@code Geometry} were it projected onto the Earth.
     *
     * @return the approximate geodesic length in meters.
     *
     * @see GeoPoint#distanceTo(GeoPoint)
     */
    public static double getGeodesicLength(@NonNull final Geometry geometry) {
        double length = 0.0;

        switch (geometry.getGeometryType()) {
            case "LineString":
            case "LinearRing":
                if (!geometry.isEmpty()) {
                    for (int i = 1; i < geometry.getNumPoints(); i++) {
                        length += new GeoPoint(((LineString) geometry).getPointN(i)).distanceTo(new GeoPoint(((LineString) geometry).getPointN(i - 1)));
                    }
                }

                break;
            case "Polygon":
                final LineString exteriorRing = ((Polygon) geometry).getExteriorRing();

                length = getGeodesicLength(exteriorRing);

                // adds the last segment of the LineString if the last Point is not the same as the first Point
                if (!exteriorRing.isClosed()) {
                    length += new GeoPoint(exteriorRing.getEndPoint()).distanceTo(new GeoPoint(exteriorRing.getStartPoint()));
                }

                break;
            default:
                Log.w(TAG,
                      "getGeodesicLength: no implementation found for geometry '" + geometry.getGeometryType() + "'");
        }

        return length;
    }

    /**
     * Calculates the approximate area of this {@code LineString} were it projected onto the Earth.
     * <p><strong>Note:</strong> The {@code LineString} may be closed or not and seen as a {@code LinearRing}.</p>
     *
     * @return the approximate geodesic area in square meters.
     *
     * @see <a href="http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409">http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409</a>
     * @see GeoPoint#distanceTo(GeoPoint)
     */
    public static double getGeodesicArea(@NonNull final LineString lineString) {
        double area = 0.0;

        // do not add the last point if it's the same as the first point
        for (int i = 0; i < ((lineString.isClosed()) ? lineString.getNumPoints() - 1 : lineString.getNumPoints()); i++) {
            GeoPoint p1 = new GeoPoint(lineString.getPointN(i));
            GeoPoint p2 = new GeoPoint(lineString.getPointN((i + 1) % lineString.getNumPoints()));

            area += ((p2.getLongitude() - p1.getLongitude()) * DEG2RAD) * (2 + Math.sin(p1.getLatitude() * DEG2RAD) + Math.sin(p2.getLatitude() * DEG2RAD));
        }

        area = area * IGeoConstants.RADIUS_EARTH_METERS * IGeoConstants.RADIUS_EARTH_METERS / 2.0;

        return Math.abs(area);
    }

    /**
     * Calculates the approximate area of this {@code Polygon} were it projected onto the Earth.
     *
     * @param checkHoles also check if this {@code Polygon} contains holes and subtract the areas of
     *                   all these internal holes
     *
     * @return the approximate geodesic area in square meters.
     *
     * @see <a href="http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409">http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409</a>
     * @see #getGeodesicArea(LineString)
     */
    public static double getGeodesicArea(@NonNull final Polygon polygon,
                                         boolean checkHoles) {
        double area = 0.0;

        if (polygon.isValid()) {
            area = getGeodesicArea(polygon.getExteriorRing());

            if (checkHoles) {
                for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                    area -= getGeodesicArea(polygon.getInteriorRingN(i));
                }
            }
        }

        return Math.abs(area);
    }
}
