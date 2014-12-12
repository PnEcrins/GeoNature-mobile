package com.makina.ecrins.maps.geojson;

/**
 * Describes all constants used by the GeoJSON format.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public enum GeoJSONType {

    FEATURE_COLLECTION("FeatureCollection"),
    FEATURE("Feature"),
    POINT("Point"),
    MULTI_POINT("MultiPoint"),
    LINE_STRING("LineString"),
    MULTI_LINE_STRING("MultiLineString"),
    POLYGON("Polygon"),
    MULTI_POLYGON("MultiPolygon"),
    GEOMETRY_COLLECTION("GeometryCollection");

    private final String value;

    GeoJSONType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
