package com.makina.ecrins.maps.jts.geojson.io;

import android.os.Bundle;

import com.makina.ecrins.maps.TestHelper;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.StringWriter;

import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createCoordinate;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createGeometryCollection;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createLineString;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createLinearRing;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createMultiLineString;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createMultiPoint;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createMultiPolygon;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createPoint;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createPolygon;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureToJsonString() throws
                                               Exception {
        // given a Feature as Point
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createPoint(gf,
                             47.2256258d,
                             -1.5545135d))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
                .getProperties();

        // when write this Feature as JSON string
        final String json = geoJsonWriter.write(feature);

        // then
        assertNotNull(json);
        assertEquals(TestHelper.getFixture("feature_point.json"),
                     json);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteInvalidFeatureToJsonString() throws
                                                      Exception {
        // given a Feature as Point
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn(mock(Geometry.class))
                .when(feature)
                .getGeometry();

        // when write this Feature as JSON string
        final String json = geoJsonWriter.write(feature);

        // then
        assertNull(json);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsPoint() throws
                                          Exception {
        // given a Feature as Point
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createPoint(gf,
                             47.2256258d,
                             -1.5545135d))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsMultiPoint() throws
                                               Exception {
        // given a Feature as MultiPoint
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createMultiPoint(gf,
                                  createPoint(gf,
                                              47.2256258d,
                                              -1.5545135d),
                                  createPoint(gf,
                                              47.225136d,
                                              -1.553913d)))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsLineString() throws
                                               Exception {
        // given a Feature as LineString
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createLineString(gf,
                                  createCoordinate(gf,
                                                   47.2256258d,
                                                   -1.5545135d),
                                  createCoordinate(gf,
                                                   47.225136d,
                                                   -1.553913d)))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsMultiLineString() throws
                                                    Exception {
        // given a Feature as MultiLineString
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createMultiLineString(gf,
                                       createLineString(gf,
                                                        createCoordinate(gf,
                                                                         47.2256258d,
                                                                         -1.5545135d),
                                                        createCoordinate(gf,
                                                                         47.225136d,
                                                                         -1.553913d))))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsSimplePolygon() throws
                                                  Exception {
        // given a Feature as simple Polygon
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createPolygon(gf,
                               createCoordinate(gf,
                                                47.226219d,
                                                -1.554430d),
                               createCoordinate(gf,
                                                47.226237d,
                                                -1.554261d),
                               createCoordinate(gf,
                                                47.226122d,
                                                -1.554245d),
                               createCoordinate(gf,
                                                47.226106d,
                                                -1.554411d),
                               createCoordinate(gf,
                                                47.226219d,
                                                -1.554430d)))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsPolygonWithHoles() throws
                                                     Exception {
        // given a Feature as Polygon with holes
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createPolygon(gf,
                               createLinearRing(gf,
                                                createCoordinate(gf,
                                                                 47.226257d,
                                                                 -1.554564d),
                                                createCoordinate(gf,
                                                                 47.226295d,
                                                                 -1.554202d),
                                                createCoordinate(gf,
                                                                 47.226075d,
                                                                 -1.554169d),
                                                createCoordinate(gf,
                                                                 47.226049d,
                                                                 -1.554496d),
                                                createCoordinate(gf,
                                                                 47.226257d,
                                                                 -1.554564d)),
                               createLinearRing(gf,
                                                createCoordinate(gf,
                                                                 47.226219d,
                                                                 -1.554430d),
                                                createCoordinate(gf,
                                                                 47.226237d,
                                                                 -1.554261d),
                                                createCoordinate(gf,
                                                                 47.226122d,
                                                                 -1.554245d),
                                                createCoordinate(gf,
                                                                 47.226106d,
                                                                 -1.554411d),
                                                createCoordinate(gf,
                                                                 47.226219d,
                                                                 -1.554430d))))
                .when(feature)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsMultiPolygon() throws
                                                 Exception {
        // given a Feature as MultiPolygon
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createMultiPolygon(gf,
                                    createPolygon(gf,
                                                  createCoordinate(gf,
                                                                   47.226116d,
                                                                   -1.554169d),
                                                  createCoordinate(gf,
                                                                   47.226126d,
                                                                   -1.554097d),
                                                  createCoordinate(gf,
                                                                   47.225527d,
                                                                   -1.553986d),
                                                  createCoordinate(gf,
                                                                   47.225519d,
                                                                   -1.554061d),
                                                  createCoordinate(gf,
                                                                   47.226116d,
                                                                   -1.554169d)),
                                    createPolygon(gf,
                                                  createLinearRing(gf,
                                                                   createCoordinate(gf,
                                                                                    47.226257d,
                                                                                    -1.554564d),
                                                                   createCoordinate(gf,
                                                                                    47.226295d,
                                                                                    -1.554202d),
                                                                   createCoordinate(gf,
                                                                                    47.226075d,
                                                                                    -1.554169d),
                                                                   createCoordinate(gf,
                                                                                    47.226049d,
                                                                                    -1.554496d),
                                                                   createCoordinate(gf,
                                                                                    47.226257d,
                                                                                    -1.554564d)),
                                                  createLinearRing(gf,
                                                                   createCoordinate(gf,
                                                                                    47.226219d,
                                                                                    -1.554430d),
                                                                   createCoordinate(gf,
                                                                                    47.226237d,
                                                                                    -1.554261d),
                                                                   createCoordinate(gf,
                                                                                    47.226122d,
                                                                                    -1.554245d),
                                                                   createCoordinate(gf,
                                                                                    47.226106d,
                                                                                    -1.554411d),
                                                                   createCoordinate(gf,
                                                                                    47.226219d,
                                                                                    -1.554430d)))))
                .when(feature)
                .getGeometry();

        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureAsGeometryCollection() throws
                                                       Exception {
        // given a Feature as GeometryCollection
        final Feature feature = mock(Feature.class);
        doReturn("id1")
                .when(feature)
                .getId();
        doReturn("Feature")
                .when(feature)
                .getType();
        doReturn(createGeometryCollection(gf,
                                          createPoint(gf,
                                                      47.2256258d,
                                                      -1.5545135d),
                                          createMultiPoint(gf,
                                                           createPoint(gf,
                                                                       47.2256258d,
                                                                       -1.5545135d),
                                                           createPoint(gf,
                                                                       47.225136d,
                                                                       -1.553913d)),
                                          createLineString(gf,
                                                           createCoordinate(gf,
                                                                            47.2256258d,
                                                                            -1.5545135d),
                                                           createCoordinate(gf,
                                                                            47.225136d,
                                                                            -1.553913d)),
                                          createMultiLineString(gf,
                                                                createLineString(gf,
                                                                                 createCoordinate(gf,
                                                                                                  47.2256258d,
                                                                                                  -1.5545135d),
                                                                                 createCoordinate(gf,
                                                                                                  47.225136d,
                                                                                                  -1.553913d))),
                                          createPolygon(gf,
                                                        createCoordinate(gf,
                                                                         47.226219d,
                                                                         -1.554430d),
                                                        createCoordinate(gf,
                                                                         47.226237d,
                                                                         -1.554261d),
                                                        createCoordinate(gf,
                                                                         47.226122d,
                                                                         -1.554245d),
                                                        createCoordinate(gf,
                                                                         47.226106d,
                                                                         -1.554411d),
                                                        createCoordinate(gf,
                                                                         47.226219d,
                                                                         -1.554430d))))
                .when(feature)
                .getGeometry();

        doReturn(new Bundle())
                .when(feature)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureCollectionToJsonString() throws
                                                         Exception {
        // given Feature1 as Point
        final Feature feature1 = mock(Feature.class);
        doReturn("id1")
                .when(feature1)
                .getId();
        doReturn("Feature")
                .when(feature1)
                .getType();
        doReturn(createPoint(gf,
                             47.2256258d,
                             -1.5545135d))
                .when(feature1)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature1)
                .getProperties();

        // given Feature2 as MultiPoint
        final Feature feature2 = mock(Feature.class);
        doReturn("id2")
                .when(feature2)
                .getId();
        doReturn("Feature")
                .when(feature2)
                .getType();
        doReturn(createMultiPoint(gf,
                                  createPoint(gf,
                                              47.2256258d,
                                              -1.5545135d),
                                  createPoint(gf,
                                              47.225136d,
                                              -1.553913d)))
                .when(feature2)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature2)
                .getProperties();

        // given Feature3 as LineString
        final Feature feature3 = mock(Feature.class);
        doReturn("id3")
                .when(feature3)
                .getId();
        doReturn("Feature")
                .when(feature3)
                .getType();
        doReturn(createLineString(gf,
                                  createCoordinate(gf,
                                                   47.2256258d,
                                                   -1.5545135d),
                                  createCoordinate(gf,
                                                   47.225136d,
                                                   -1.553913d)))
                .when(feature3)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature3)
                .getProperties();

        // given Feature4 as MultiLineString
        final Feature feature4 = mock(Feature.class);
        doReturn("id4")
                .when(feature4)
                .getId();
        doReturn("Feature")
                .when(feature4)
                .getType();
        doReturn(createMultiLineString(gf,
                                       createLineString(gf,
                                                        createCoordinate(gf,
                                                                         47.2256258d,
                                                                         -1.5545135d),
                                                        createCoordinate(gf,
                                                                         47.225136d,
                                                                         -1.553913d))))
                .when(feature4)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature4)
                .getProperties();

        // given Feature5 as simple Polygon
        final Feature feature5 = mock(Feature.class);
        doReturn("id5")
                .when(feature5)
                .getId();
        doReturn("Feature")
                .when(feature5)
                .getType();
        doReturn(createPolygon(gf,
                               createCoordinate(gf,
                                                47.226219d,
                                                -1.554430d),
                               createCoordinate(gf,
                                                47.226237d,
                                                -1.554261d),
                               createCoordinate(gf,
                                                47.226122d,
                                                -1.554245d),
                               createCoordinate(gf,
                                                47.226106d,
                                                -1.554411d),
                               createCoordinate(gf,
                                                47.226219d,
                                                -1.554430d)))
                .when(feature5)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature5)
                .getProperties();

        // given a FeatureCollection
        final FeatureCollection featureCollection = mock(FeatureCollection.class);
        doReturn("FeatureCollection")
                .when(featureCollection)
                .getType();
        doReturn(asList(feature1,
                        feature2,
                        feature3,
                        feature4,
                        feature5))
                .when(featureCollection)
                .getFeatures();

        // when write this FeatureCollection as JSON string
        final String json = geoJsonWriter.write(featureCollection);

        // then
        assertNotNull(json);
        assertEquals(TestHelper.getFixture("featurecollection.json"),
                     json);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteEmptyFeatureCollection() throws
                                                  Exception {
        // given an empty FeatureCollection
        final FeatureCollection featureCollection = mock(FeatureCollection.class);
        doReturn("FeatureCollection")
                .when(featureCollection)
                .getType();

        // when write this FeatureCollection as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            featureCollection);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("featurecollection_empty.json"),
                     writer.toString());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testWriteFeatureCollection() throws
                                             Exception {
        // given Feature1 as Point
        final Feature feature1 = mock(Feature.class);
        doReturn("id1")
                .when(feature1)
                .getId();
        doReturn("Feature")
                .when(feature1)
                .getType();
        doReturn(createPoint(gf,
                             47.2256258d,
                             -1.5545135d))
                .when(feature1)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature1)
                .getProperties();

        // given Feature2 as MultiPoint
        final Feature feature2 = mock(Feature.class);
        doReturn("id2")
                .when(feature2)
                .getId();
        doReturn("Feature")
                .when(feature2)
                .getType();
        doReturn(createMultiPoint(gf,
                                  createPoint(gf,
                                              47.2256258d,
                                              -1.5545135d),
                                  createPoint(gf,
                                              47.225136d,
                                              -1.553913d)))
                .when(feature2)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature2)
                .getProperties();

        // given Feature3 as LineString
        final Feature feature3 = mock(Feature.class);
        doReturn("id3")
                .when(feature3)
                .getId();
        doReturn("Feature")
                .when(feature3)
                .getType();
        doReturn(createLineString(gf,
                                  createCoordinate(gf,
                                                   47.2256258d,
                                                   -1.5545135d),
                                  createCoordinate(gf,
                                                   47.225136d,
                                                   -1.553913d)))
                .when(feature3)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature3)
                .getProperties();

        // given Feature4 as MultiLineString
        final Feature feature4 = mock(Feature.class);
        doReturn("id4")
                .when(feature4)
                .getId();
        doReturn("Feature")
                .when(feature4)
                .getType();
        doReturn(createMultiLineString(gf,
                                       createLineString(gf,
                                                        createCoordinate(gf,
                                                                         47.2256258d,
                                                                         -1.5545135d),
                                                        createCoordinate(gf,
                                                                         47.225136d,
                                                                         -1.553913d))))
                .when(feature4)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature4)
                .getProperties();

        // given Feature5 as simple Polygon
        final Feature feature5 = mock(Feature.class);
        doReturn("id5")
                .when(feature5)
                .getId();
        doReturn("Feature")
                .when(feature5)
                .getType();
        doReturn(createPolygon(gf,
                               createCoordinate(gf,
                                                47.226219d,
                                                -1.554430d),
                               createCoordinate(gf,
                                                47.226237d,
                                                -1.554261d),
                               createCoordinate(gf,
                                                47.226122d,
                                                -1.554245d),
                               createCoordinate(gf,
                                                47.226106d,
                                                -1.554411d),
                               createCoordinate(gf,
                                                47.226219d,
                                                -1.554430d)))
                .when(feature5)
                .getGeometry();
        doReturn(new Bundle())
                .when(feature5)
                .getProperties();

        // given a FeatureCollection
        final FeatureCollection featureCollection = mock(FeatureCollection.class);
        doReturn("FeatureCollection")
                .when(featureCollection)
                .getType();
        doReturn(asList(feature1,
                        feature2,
                        feature3,
                        feature4,
                        feature5))
                .when(featureCollection)
                .getFeatures();

        // when write this FeatureCollection as JSON string
        final StringWriter writer = new StringWriter();
        geoJsonWriter.write(writer,
                            featureCollection);

        // then
        assertNotNull(writer.toString());
        assertEquals(TestHelper.getFixture("featurecollection.json"),
                     writer.toString());
    }
}