package com.makina.ecrins.maps.geojson.geometry;

import android.util.Log;

import com.makina.ecrins.maps.geojson.GeoJSONType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating or manipulating {@link IGeometry} instances.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class GeometryUtils implements IMathConstants, IGeoConstants {

    /**
     * {@link GeometryUtils} instances should NOT be constructed in standard programming.
     */
    private GeometryUtils() {

    }


    /**
     * Creates a {@link IGeometry} instance from a JSON object.
     *
     * @param jsonObject JSON representation of the {@link IGeometry} to create
     * @return {@link IGeometry} instance or <code>null</code> if something is wrong
     */
    public static IGeometry createGeometryFromJson(JSONObject jsonObject) {
        try {
            if (jsonObject.get("type")
                    .equals(GeoJSONType.POINT.getValue())) {
                return createGeometryFromJsonCoordinates(GeoJSONType.POINT, jsonObject.getJSONArray("coordinates"));
            }
            else if (jsonObject.get("type")
                    .equals(GeoJSONType.LINE_STRING.getValue())) {
                return createGeometryFromJsonCoordinates(GeoJSONType.LINE_STRING, jsonObject.getJSONArray("coordinates"));
            }
            else if (jsonObject.get("type")
                    .equals(GeoJSONType.POLYGON.getValue())) {
                return createGeometryFromJsonCoordinates(GeoJSONType.POLYGON, jsonObject.getJSONArray("coordinates"));
            }
            else {
                Log.w(GeometryUtils.class.getName(), "'" + jsonObject.get("type") + "' not implemented");

                return null;
            }
        }
        catch (JSONException je) {
            Log.w(GeometryUtils.class.getName(), je.getMessage());

            return null;
        }
    }

    /**
     * Creates a {@link IGeometry} instance from a JSON array object of coordinates.
     *
     * @param coordinates JSON array representation of coordinates of the {@link IGeometry} to create
     * @return {@link IGeometry} instance or <code>null</code> if something is wrong
     */
    public static IGeometry createGeometryFromJsonCoordinates(GeoJSONType type, JSONArray coordinates) {
        try {
            switch (type) {
                case POINT:
                    return new Point(new GeoPoint(coordinates, GeoPoint.LON_LAT));
                case LINE_STRING:
                    final List<Point> points = new ArrayList<>();

                    for (int i = 0; i < coordinates.length(); i++) {
                        points.add((Point) createGeometryFromJsonCoordinates(GeoJSONType.POINT, coordinates.getJSONArray(i)));
                    }

                    return new LineString(points);
                case POLYGON:
                    final List<LineString> lineStrings = new ArrayList<>();

                    for (int i = 0; i < coordinates.length(); i++) {
                        lineStrings.add((LineString) createGeometryFromJsonCoordinates(GeoJSONType.LINE_STRING, coordinates.getJSONArray(i)));
                    }

                    if (lineStrings.isEmpty()) {
                        Log.w(GeometryUtils.class.getName(), "wrong representation of a polygon : " + coordinates.toString());

                        return null;
                    }
                    else {
                        Polygon polygon = new Polygon(lineStrings.get(0)
                                .getPoints());

                        if (lineStrings.size() > 1) {
                            for (int i = 1; i < lineStrings.size(); i++) {
                                polygon.addHole(lineStrings.get(i)
                                        .getPoints());
                            }
                        }

                        return polygon;
                    }
                default:
                    Log.w(GeometryUtils.class.getName(), "'" + type.getValue() + "' not implemented");
                    return null;
            }
        }
        catch (JSONException je) {
            Log.w(GeometryUtils.class.getName(), je.getMessage());

            return null;
        }
    }

    /**
     * Returns the minimum distance between two {@link IGeometry} instances.
     *
     * @param fromGeometry the {@link IGeometry} to check the distance from
     * @param toGeometry   the {@link IGeometry} to check the distance to
     * @return the minimum distance
     */
    public static double distanceTo(IGeometry fromGeometry, IGeometry toGeometry) {
        double distance = Double.MAX_VALUE;

        switch (fromGeometry.getType()) {
            case POINT:
                switch (toGeometry.getType()) {
                    case POINT:
                        distance = ((Point) fromGeometry).getGeoPoint()
                                .distanceTo(((Point) toGeometry).getGeoPoint());
                        break;
                    case LINE_STRING:
                        for (Point point : ((LineString) toGeometry).getPoints()) {
                            double distanceFromPoint = ((Point) fromGeometry).getGeoPoint()
                                    .distanceTo(point.getGeoPoint());

                            if (distance > distanceFromPoint) {
                                distance = distanceFromPoint;
                            }
                        }

                        break;
                    case POLYGON:
                        distance = distanceTo(fromGeometry, ((Polygon) toGeometry).getPolygon());
                        break;
                    default:
                        Log.w(GeometryUtils.class.getName(), "no implementation found for geometry '" + toGeometry.getType()
                                .getValue() + "'");
                        break;
                }

                break;
            case LINE_STRING:
                switch (toGeometry.getType()) {
                    case POINT:
                        distance = distanceTo(toGeometry, fromGeometry);
                        break;
                    case LINE_STRING:
                        for (Point fromPoint : ((LineString) fromGeometry).getPoints()) {
                            for (Point toPoint : ((LineString) toGeometry).getPoints()) {
                                double distanceFromPoint = fromPoint.getGeoPoint()
                                        .distanceTo(toPoint.getGeoPoint());

                                if (distance > distanceFromPoint) {
                                    distance = distanceFromPoint;
                                }
                            }
                        }

                        break;
                    case POLYGON:
                        distance = distanceTo(fromGeometry, ((Polygon) toGeometry).getPolygon());
                        break;
                    default:
                        Log.w(GeometryUtils.class.getName(), "no implementation found for geometry '" + toGeometry.getType()
                                .getValue() + "'");
                        break;
                }

                break;
            case POLYGON:
                switch (toGeometry.getType()) {
                    case POINT:
                        distance = distanceTo(toGeometry, fromGeometry);
                        break;
                    case LINE_STRING:
                        distance = distanceTo(toGeometry, fromGeometry);
                        break;
                    case POLYGON:
                        distance = distanceTo(((Polygon) toGeometry).getPolygon(), fromGeometry);
                        break;
                    default:
                        Log.w(GeometryUtils.class.getName(), "no implementation found for geometry '" + toGeometry.getType()
                                .getValue() + "'");
                        break;
                }

                break;
            default:
                distance = 0.0;

                Log.w(
                        GeometryUtils.class.getName(),
                        "no implementation found for geometry '" +
                                fromGeometry.getType().getValue() +
                                "'"
                );

                break;
        }

        return distance;
    }

    /**
     * Tests the validity of this {@link IGeometry}.
     *
     * @param geometry the geometry to test
     * @return <code>true</code> if this {@link IGeometry} is valid, <code>false</code> otherwise
     */
    public static boolean isValid(IGeometry geometry) {
        Geometry jtsGeometry = createJTSGeometry(geometry);

        return (jtsGeometry != null) && jtsGeometry.isValid();
    }

    /**
     * Tests whether the second geometry contains the first geometry.
     *
     * @param geometry1 the first geometry to test
     * @param geometry2 the second geometry to check if the first one is inside or not
     * @return <code>true</code> if the first geometry is inside the second geometry
     */
    public static boolean contains(IGeometry geometry1, IGeometry geometry2) {
        Geometry jtsGeometry1 = createJTSGeometry(geometry1);
        Geometry jtsGeometry2 = createJTSGeometry(geometry2);

        return (jtsGeometry1 != null) && (jtsGeometry2 != null) && jtsGeometry2.contains(jtsGeometry1);
    }

    private static Geometry createJTSGeometry(IGeometry geometry) {
        final GeometryFactory gFactory = new GeometryFactory();
        final CoordinateList coordinateList = new CoordinateList();

        switch (geometry.getType()) {
            case POINT:
                return gFactory.createPoint(
                        new Coordinate(
                                ((Point) geometry).getGeoPoint()
                                        .getLongitudeE6() / 1E6,
                                ((Point) geometry).getGeoPoint()
                                        .getLatitudeE6() / 1E6)
                );
            case LINE_STRING:
                coordinateList.clear();

                for (Point point : ((LineString) geometry).getPoints()) {
                    coordinateList.add(
                            new Coordinate(
                                    point.getGeoPoint()
                                            .getLongitudeE6() / 1E6,
                                    point.getGeoPoint()
                                            .getLatitudeE6() / 1E6),
                            false
                    );
                }

                if (coordinateList.size() >= 2) {
                    return gFactory.createLineString(coordinateList.toCoordinateArray());
                }
                else {
                    return null;
                }
            case POLYGON:
                coordinateList.clear();

                for (Point point : ((Polygon) geometry).getPolygon()
                        .getPoints()) {
                    coordinateList.add(
                            new Coordinate(
                                    point.getGeoPoint()
                                            .getLongitudeE6() / 1E6,
                                    point.getGeoPoint()
                                            .getLatitudeE6() / 1E6),
                            false
                    );
                }

                if (coordinateList.size() >= 3) {
                    // check the last point if it's the same as the first point
                    if (!((Polygon) geometry).getPolygon()
                            .getPoints()
                            .get(((Polygon) geometry).getPolygon()
                                    .getPoints()
                                    .size() - 1)
                            .getGeoPoint()
                            .equals(((Polygon) geometry).getPolygon()
                                    .getPoints()
                                    .get(0)
                                    .getGeoPoint())) {
                        coordinateList.add(
                                new Coordinate(
                                        ((Polygon) geometry).getPolygon()
                                                .getPoints()
                                                .get(0)
                                                .getGeoPoint()
                                                .getLongitudeE6() / 1E6,
                                        ((Polygon) geometry).getPolygon()
                                                .getPoints()
                                                .get(0)
                                                .getGeoPoint()
                                                .getLatitudeE6() / 1E6),
                                false
                        );
                    }

                    List<LinearRing> holes = new ArrayList<>();

                    for (Polygon polygonHole : ((Polygon) geometry).getHoles()) {
                        CoordinateList coordinateListHole = new CoordinateList();

                        for (Point point : polygonHole.getPolygon()
                                .getPoints()) {
                            coordinateListHole.add(
                                    new Coordinate(
                                            point.getGeoPoint()
                                                    .getLongitudeE6() / 1E6,
                                            point.getGeoPoint()
                                                    .getLatitudeE6() / 1E6),
                                    false
                            );
                        }

                        if (coordinateListHole.size() >= 3) {
                            // check the last point if it's the same as the first point
                            if (!polygonHole.getPolygon()
                                    .getPoints()
                                    .get(polygonHole.getPolygon()
                                            .getPoints()
                                            .size() - 1)
                                    .getGeoPoint()
                                    .equals(polygonHole.getPolygon()
                                            .getPoints()
                                            .get(0)
                                            .getGeoPoint())) {
                                coordinateList.add(
                                        new Coordinate(
                                                polygonHole.getPolygon()
                                                        .getPoints()
                                                        .get(0)
                                                        .getGeoPoint()
                                                        .getLongitudeE6() / 1E6,
                                                polygonHole.getPolygon()
                                                        .getPoints()
                                                        .get(0)
                                                        .getGeoPoint()
                                                        .getLatitudeE6() / 1E6),
                                        false
                                );
                            }

                            holes.add(gFactory.createLinearRing(coordinateListHole.toCoordinateArray()));
                        }
                    }

                    return gFactory.createPolygon(
                            gFactory.createLinearRing(coordinateList.toCoordinateArray()),
                            GeometryFactory.toLinearRingArray(holes));
                }
                else {
                    return null;
                }

            default:
                Log.w(GeometryUtils.class.getName(), "no implementation found for geometry '" + geometry.getType()
                        .getValue() + "'");
                return null;
        }
    }
}
