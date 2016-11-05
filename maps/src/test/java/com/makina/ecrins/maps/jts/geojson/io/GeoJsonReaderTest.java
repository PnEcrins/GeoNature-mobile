package com.makina.ecrins.maps.jts.geojson.io;

import com.makina.ecrins.maps.TestHelper;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.StringReader;

import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createCoordinate;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createGeometryCollection;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createLineString;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createLinearRing;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createMultiLineString;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createMultiPoint;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createMultiPolygon;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createPoint;
import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createPolygon;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for {@link GeoJsonReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class GeoJsonReaderTest {

    private GeometryFactory gf;
    private GeoJsonReader geoJsonReader;

    @Before
    public void setUp() throws
                        Exception {
        gf = new GeometryFactory();
        geoJsonReader = new GeoJsonReader();
    }

    @Test
    public void testReadFeatureAsPoint() throws
                                         Exception {
        // given a JSON Feature as Point
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_point.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createPoint(gf,
                                 47.2256258d,
                                 -1.5545135d),
                     feature.getGeometry());
    }

    @Test
    public void testReadFeatureAsMultiPoint() throws
                                              Exception {
        // given a JSON Feature as MultiPoint
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_multipoint.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createMultiPoint(gf,
                                      createPoint(gf,
                                                  47.2256258d,
                                                  -1.5545135d),
                                      createPoint(gf,
                                                  47.225136d,
                                                  -1.553913d)),
                     feature.getGeometry());
    }

    @Test
    public void testReadFeatureAsLineString() throws
                                              Exception {
        // given a JSON Feature as LineString
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_linestring.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createLineString(gf,
                                      createCoordinate(gf,
                                                       47.2256258d,
                                                       -1.5545135d),
                                      createCoordinate(gf,
                                                       47.225136d,
                                                       -1.553913d)),
                     feature.getGeometry());
    }

    @Test
    public void testReadFeatureAsMultiLineString() throws
                                                   Exception {
        // given a JSON Feature as MultiLineString
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_multilinestring.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createMultiLineString(gf,
                                           createLineString(gf,
                                                            createCoordinate(gf,
                                                                             47.2256258d,
                                                                             -1.5545135d),
                                                            createCoordinate(gf,
                                                                             47.225136d,
                                                                             -1.553913d))),
                     feature.getGeometry());
    }

    @Test
    public void testReadFeatureAsSimplePolygon() throws
                                                 Exception {
        // given a JSON Feature as simple Polygon
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_polygon_simple.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createPolygon(gf,
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
                                                    -1.554430d)),
                     feature.getGeometry());

    }

    @Test
    public void testReadFeatureAsPolygonWithHoles() throws
                                                    Exception {
        // given a JSON Feature as Polygon with holes
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_polygon_holes.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createPolygon(gf,
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
                                                                     -1.554430d))),
                     feature.getGeometry());
    }

    @Test
    public void testReadFeatureAsMultiPolygon() throws
                                                Exception {
        // given a JSON Feature as MultiPolygon
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_multipolygon.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createMultiPolygon(gf,
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
                                                                                        -1.554430d)))),
                     feature.getGeometry());
    }

    @Test
    public void testReadFeatureAsGeometryCollection() throws
                                                      Exception {
        // given a JSON Feature as GeometryCollection
        final StringReader reader = new StringReader(TestHelper.getFixture("feature_geometrycollection.json"));

        // when read the JSON as Feature
        final Feature feature = geoJsonReader.readFeature(reader);

        // then
        assertNotNull(feature);
        assertEquals("id1",
                     feature.getId());
        assertEquals("Feature",
                     feature.getType());
        assertNotNull(feature.getGeometry());
        assertEquals(createGeometryCollection(gf,
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
                                                                             -1.554430d))),
                     feature.getGeometry());
    }

    @Test
    public void testReadEmptyFeatureCollection() throws
                                                 Exception {
        // given a JSON empty FeatureCollection
        final StringReader reader = new StringReader(TestHelper.getFixture("featurecollection_empty.json"));

        // when read the JSON as FeatureCollection
        final FeatureCollection featureCollection = geoJsonReader.readFeatureCollection(reader);

        // then
        assertNotNull(featureCollection);
        assertEquals("FeatureCollection",
                     featureCollection.getType());
        assertTrue(featureCollection.isEmpty());
    }

    @Test
    public void testReadFeatureCollection() throws
                                            Exception {
        // given a JSON empty FeatureCollection
        final StringReader reader = new StringReader(TestHelper.getFixture("featurecollection.json"));

        // when read the JSON as FeatureCollection
        final FeatureCollection featureCollection = geoJsonReader.readFeatureCollection(reader);

        // then
        assertNotNull(featureCollection);
        assertEquals("FeatureCollection",
                     featureCollection.getType());
        assertFalse(featureCollection.isEmpty());
        assertTrue(featureCollection.hasFeature("id1"));
        assertTrue(featureCollection.hasFeature("id2"));
        assertTrue(featureCollection.hasFeature("id3"));
        assertTrue(featureCollection.hasFeature("id4"));
        assertTrue(featureCollection.hasFeature("id5"));
        assertFalse(featureCollection.hasFeature("no_such_feature"));

        final Feature feature1 = featureCollection.getFeature("id1");
        assertNotNull(feature1);
        assertEquals("id1",
                     feature1.getId());
        assertEquals("Feature",
                     feature1.getType());
        assertNotNull(feature1.getGeometry());
        assertEquals(createPoint(gf,
                                 47.2256258d,
                                 -1.5545135d),
                     feature1.getGeometry());

        final Feature feature2 = featureCollection.getFeature("id2");
        assertNotNull(feature2);
        assertEquals("id2",
                     feature2.getId());
        assertEquals("Feature",
                     feature2.getType());
        assertNotNull(feature2.getGeometry());
        assertEquals(createMultiPoint(gf,
                                      createPoint(gf,
                                                  47.2256258d,
                                                  -1.5545135d),
                                      createPoint(gf,
                                                  47.225136d,
                                                  -1.553913d)),
                     feature2.getGeometry());

        final Feature feature3 = featureCollection.getFeature("id3");
        assertNotNull(feature3);
        assertEquals("id3",
                     feature3.getId());
        assertEquals("Feature",
                     feature3.getType());
        assertNotNull(feature3.getGeometry());
        assertEquals(createLineString(gf,
                                      createCoordinate(gf,
                                                       47.2256258d,
                                                       -1.5545135d),
                                      createCoordinate(gf,
                                                       47.225136d,
                                                       -1.553913d)),
                     feature3.getGeometry());

        final Feature feature4 = featureCollection.getFeature("id4");
        assertNotNull(feature4);
        assertEquals("id4",
                     feature4.getId());
        assertEquals("Feature",
                     feature4.getType());
        assertNotNull(feature4.getGeometry());
        assertEquals(createMultiLineString(gf,
                                           createLineString(gf,
                                                            createCoordinate(gf,
                                                                             47.2256258d,
                                                                             -1.5545135d),
                                                            createCoordinate(gf,
                                                                             47.225136d,
                                                                             -1.553913d))),
                     feature4.getGeometry());

        final Feature feature5 = featureCollection.getFeature("id5");
        assertNotNull(feature5);
        assertEquals("id5",
                     feature5.getId());
        assertEquals("Feature",
                     feature5.getType());
        assertNotNull(feature5.getGeometry());
        assertEquals(createPolygon(gf,
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
                                                    -1.554430d)),
                     feature5.getGeometry());
    }
}