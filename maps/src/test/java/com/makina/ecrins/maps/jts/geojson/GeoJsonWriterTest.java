package com.makina.ecrins.maps.jts.geojson;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.makina.ecrins.maps.TestHelper;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link GeoJsonWriter}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class GeoJsonWriterTest {

    private GeometryFactory gf;
    private GeoJsonWriter geoJsonWriter;

    @Before
    public void setUp() throws
                        Exception {
        gf = new GeometryFactory();
        geoJsonWriter = new GeoJsonWriter();
    }

    @Test
    public void testWriteFeatureAsPoint() throws
                                          Exception {
        // given a Feature as Point
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createPoint(47.2256258d,
                             -1.5545135d)).when(feature)
                                          .getGeometry();
        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_point.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsMultiPoint() throws
                                               Exception {
        // given a Feature as MultiPoint
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createMultiPoint(createPoint(47.2256258d,
                                              -1.5545135d),
                                  createPoint(47.225136d,
                                              -1.553913d))).when(feature)
                                                           .getGeometry();
        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_multipoint.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsLineString() throws
                                               Exception {
        // given a Feature as LineString
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createLineString(createCoordinate(47.2256258d,
                                                   -1.5545135d),
                                  createCoordinate(47.225136d,
                                                   -1.553913d))).when(feature)
                                                                .getGeometry();
        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_linestring.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsMultiLineString() throws
                                                    Exception {
        // given a Feature as MultiLineString
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createMultiLineString(createLineString(createCoordinate(47.2256258d,
                                                                         -1.5545135d),
                                                        createCoordinate(47.225136d,
                                                                         -1.553913d)))).when(feature)
                                                                                       .getGeometry();
        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_multilinestring.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsSimplePolygon() throws
                                                  Exception {
        // given a Feature as MultiLineString
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createPolygon(createCoordinate(47.226219d,
                                                -1.554430d),
                               createCoordinate(47.226237d,
                                                -1.554261d),
                               createCoordinate(47.226122d,
                                                -1.554245d),
                               createCoordinate(47.226106d,
                                                -1.554411d),
                               createCoordinate(47.226219d,
                                                -1.554430d))).when(feature)
                                                             .getGeometry();
        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_polygon_simple.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsPolygonWithHoles() throws
                                                     Exception {
        // given a Feature as MultiLineString
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createPolygon(createLinearRing(createCoordinate(47.226257d,
                                                                 -1.554564d),
                                                createCoordinate(47.226295d,
                                                                 -1.554202d),
                                                createCoordinate(47.226075d,
                                                                 -1.554169d),
                                                createCoordinate(47.226049d,
                                                                 -1.554496d),
                                                createCoordinate(47.226257d,
                                                                 -1.554564d)),
                               createLinearRing(createCoordinate(47.226219d,
                                                                 -1.554430d),
                                                createCoordinate(47.226237d,
                                                                 -1.554261d),
                                                createCoordinate(47.226122d,
                                                                 -1.554245d),
                                                createCoordinate(47.226106d,
                                                                 -1.554411d),
                                                createCoordinate(47.226219d,
                                                                 -1.554430d)))).when(feature)
                                                                               .getGeometry();
        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_polygon_holes.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsMultiPolygon() throws
                                                 Exception {
        // given a Feature as MultiLineString
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createMultiPolygon(createPolygon(createCoordinate(47.226116d,
                                                                   -1.554169d),
                                                  createCoordinate(47.226126d,
                                                                   -1.554097d),
                                                  createCoordinate(47.225527d,
                                                                   -1.553986d),
                                                  createCoordinate(47.225519d,
                                                                   -1.554061d),
                                                  createCoordinate(47.226116d,
                                                                   -1.554169d)),
                                    createPolygon(createLinearRing(createCoordinate(47.226257d,
                                                                                    -1.554564d),
                                                                   createCoordinate(47.226295d,
                                                                                    -1.554202d),
                                                                   createCoordinate(47.226075d,
                                                                                    -1.554169d),
                                                                   createCoordinate(47.226049d,
                                                                                    -1.554496d),
                                                                   createCoordinate(47.226257d,
                                                                                    -1.554564d)),
                                                  createLinearRing(createCoordinate(47.226219d,
                                                                                    -1.554430d),
                                                                   createCoordinate(47.226237d,
                                                                                    -1.554261d),
                                                                   createCoordinate(47.226122d,
                                                                                    -1.554245d),
                                                                   createCoordinate(47.226106d,
                                                                                    -1.554411d),
                                                                   createCoordinate(47.226219d,
                                                                                    -1.554430d))))).when(feature)
                                                                                                   .getGeometry();

        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_multipolygon.json"),
                     writer.toString());
    }

    @Test
    public void testWriteFeatureAsGeometryCollection() throws
                                                       Exception {
        // given a Feature as MultiLineString
        final Feature feature = mock(Feature.class);
        doReturn("id1").when(feature)
                       .getId();
        doReturn("Feature").when(feature)
                           .getType();
        doReturn(createGeometryCollection(createPoint(47.2256258d,
                                                      -1.5545135d),
                                          createMultiPoint(createPoint(47.2256258d,
                                                                       -1.5545135d),
                                                           createPoint(47.225136d,
                                                                       -1.553913d)),
                                          createLineString(createCoordinate(47.2256258d,
                                                                            -1.5545135d),
                                                           createCoordinate(47.225136d,
                                                                            -1.553913d)),
                                          createMultiLineString(createLineString(createCoordinate(47.2256258d,
                                                                                                  -1.5545135d),
                                                                                 createCoordinate(47.225136d,
                                                                                                  -1.553913d))),
                                          createPolygon(createCoordinate(47.226219d,
                                                                         -1.554430d),
                                                        createCoordinate(47.226237d,
                                                                         -1.554261d),
                                                        createCoordinate(47.226122d,
                                                                         -1.554245d),
                                                        createCoordinate(47.226106d,
                                                                         -1.554411d),
                                                        createCoordinate(47.226219d,
                                                                         -1.554430d)))).when(feature)
                                                                                       .getGeometry();

        doReturn(new Bundle()).when(feature)
                              .getProperties();

        // when write this Feature as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            feature);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("feature_geometrycollection.json"),
                     writer.toString());
    }

    @NonNull
    private Coordinate createCoordinate(double latitude,
                                        double longitude) {
        return new Coordinate(longitude,
                              latitude);
    }

    @NonNull
    private Point createPoint(double latitude,
                              double longitude) {
        return gf.createPoint(createCoordinate(latitude,
                                               longitude));
    }

    @NonNull
    private MultiPoint createMultiPoint(@NonNull final Point... points) {
        return gf.createMultiPoint(points);
    }

    @NonNull
    private LineString createLineString(@NonNull final Coordinate... coordinates) {
        return gf.createLineString(coordinates);
    }

    @NonNull
    private MultiLineString createMultiLineString(@NonNull final LineString... lineStrings) {
        return gf.createMultiLineString(lineStrings);
    }

    @NonNull
    private LinearRing createLinearRing(@NonNull final Coordinate... coordinates) {
        return gf.createLinearRing(coordinates);
    }

    @NonNull
    private Polygon createPolygon(@NonNull final Coordinate... coordinates) {
        return gf.createPolygon(coordinates);
    }

    @NonNull
    private Polygon createPolygon(@NonNull final LinearRing shell,
                                  @NonNull final LinearRing... holes) {
        return gf.createPolygon(shell,
                                holes);
    }

    @NonNull
    private MultiPolygon createMultiPolygon(@NonNull final Polygon... polygons) {
        return gf.createMultiPolygon(polygons);
    }

    @NonNull
    private GeometryCollection createGeometryCollection(@NonNull final Geometry... geometries) {
        return gf.createGeometryCollection(geometries);
    }
}