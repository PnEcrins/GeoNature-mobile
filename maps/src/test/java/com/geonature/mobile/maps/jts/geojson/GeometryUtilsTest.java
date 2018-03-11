package com.geonature.mobile.maps.jts.geojson;

import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.geonature.mobile.maps.jts.geojson.GeometryUtils.distanceTo;
import static com.geonature.mobile.maps.jts.geojson.GeometryUtils.getGeodesicArea;
import static com.geonature.mobile.maps.jts.geojson.GeometryUtils.getGeodesicLength;
import static com.geonature.mobile.maps.jts.geojson.JTSTestHelper.createCoordinate;
import static com.geonature.mobile.maps.jts.geojson.JTSTestHelper.createLineString;
import static com.geonature.mobile.maps.jts.geojson.JTSTestHelper.createLinearRing;
import static com.geonature.mobile.maps.jts.geojson.JTSTestHelper.createPoint;
import static com.geonature.mobile.maps.jts.geojson.JTSTestHelper.createPolygon;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link GeometryUtils}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class GeometryUtilsTest {

    private GeometryFactory gf;

    @Before
    public void setUp() throws
                        Exception {
        gf = new GeometryFactory();
    }

    @Test
    public void testDistanceBetweenTwoPoints() throws
                                               Exception {
        // given two Points
        final double distance = distanceTo(createPoint(gf,
                                                       47.225782d,
                                                       -1.554476d),
                                           createPoint(gf,
                                                       47.226468d,
                                                       -1.554996d));

        // then the computed distance should be roughly equals to 90m
        assertEquals(90d,
                     distance,
                     1d);
    }

    @Test
    public void testDistanceBetweenPointAndLineString() throws
                                                        Exception {
        // given Point and LineString
        final double distance1 = distanceTo(createPoint(gf,
                                                        47.225782d,
                                                        -1.554476d),
                                            createLineString(gf,
                                                             createCoordinate(gf,
                                                                              47.226468d,
                                                                              -1.554996d),
                                                             createCoordinate(gf,
                                                                              47.226600d,
                                                                              -1.554846d)));

        // then the computed distance should be roughly equals to 90m
        assertEquals(90d,
                     distance1,
                     1d);

        // given LineString and Point
        final double distance2 = distanceTo(createLineString(gf,
                                                             createCoordinate(gf,
                                                                              47.226468d,
                                                                              -1.554996d),
                                                             createCoordinate(gf,
                                                                              47.226600d,
                                                                              -1.554846d)),
                                            createPoint(gf,
                                                        47.225782d,
                                                        -1.554476d));

        // then the computed distance should be roughly equals to 90m
        assertEquals(90d,
                     distance2,
                     1d);
    }

    @Test
    public void testDistanceBetweenPointAndPolygon() throws
                                                     Exception {
        // given Point and Polygon
        final double distance1 = distanceTo(createPoint(gf,
                                                        47.225782d,
                                                        -1.554476d),
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
                                                                           -1.554430d)));

        // then the computed distance should be roughly equals to 36m
        assertEquals(36d,
                     distance1,
                     1d);

        // given Polygon and Point
        final double distance2 = distanceTo(createPolygon(gf,
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
                                            createPoint(gf,
                                                        47.225782d,
                                                        -1.554476d));

        // then the computed distance should be roughly equals to 36m
        assertEquals(36d,
                     distance2,
                     1d);
    }

    @Test
    public void testDistanceBetweenTwoLineStrings() throws
                                                    Exception {

        // given two LineStrings
        final double distance = distanceTo(createLineString(gf,
                                                            createCoordinate(gf,
                                                                             47.226468d,
                                                                             -1.554996d),
                                                            createCoordinate(gf,
                                                                             47.226600d,
                                                                             -1.554846d)),
                                           createLineString(gf,
                                                            createCoordinate(gf,
                                                                             47.226219d,
                                                                             -1.554430d),
                                                            createCoordinate(gf,
                                                                             47.226237d,
                                                                             -1.554261d),
                                                            createCoordinate(gf,
                                                                             47.226122d,
                                                                             -1.554245d)));

        // then the computed distance should be roughly equals to 59m
        assertEquals(58d,
                     distance,
                     1d);
    }

    @Test
    public void testDistanceBetweenLineStringAndPolygon() throws
                                                          Exception {
        // given LineString and Polygon
        final double distance1 = distanceTo(createLineString(gf,
                                                             createCoordinate(gf,
                                                                              47.226468d,
                                                                              -1.554996d),
                                                             createCoordinate(gf,
                                                                              47.226600d,
                                                                              -1.554846d)),
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
                                                                           -1.554430d)));

        // then the computed distance should be roughly equals to 59m
        assertEquals(58d,
                     distance1,
                     1d);

        // given Polygon and LineString
        final double distance2 = distanceTo(createPolygon(gf,
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
                                            createLineString(gf,
                                                             createCoordinate(gf,
                                                                              47.226468d,
                                                                              -1.554996d),
                                                             createCoordinate(gf,
                                                                              47.226600d,
                                                                              -1.554846d)));

        // then the computed distance should be roughly equals to 59m
        assertEquals(58d,
                     distance2,
                     1d);
    }

    @Test
    public void testDistanceBetweenTwoPolygons() throws
                                                 Exception {
        // given two Polygons
        final double distance = distanceTo(createPolygon(gf,
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
                                                                          -1.554430d)));

        // then the computed distance should be roughly equals to 19m
        assertEquals(19d,
                     distance,
                     1d);
    }

    @Test
    public void testGeodesicLengthForLineString() throws
                                                  Exception {
        // when computing the geodesic length of a given LineString
        final double length = getGeodesicLength(createLineString(gf,
                                                                 createCoordinate(gf,
                                                                                  47.226116d,
                                                                                  -1.554169d),
                                                                 createCoordinate(gf,
                                                                                  47.226126d,
                                                                                  -1.554097d),
                                                                 createCoordinate(gf,
                                                                                  47.225527d,
                                                                                  -1.553986d)));

        // then the computed length should be roughly equals to 74m
        assertEquals(74d,
                     length,
                     1d);
    }

    @Test
    public void testGeodesicLengthForPolygon() throws
                                               Exception {
        // when computing the geodesic length of a given LineString
        final double length = getGeodesicLength(createPolygon(gf,
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
                                                                               -1.554169d)));

        // then the computed length should be roughly equals to 148m
        assertEquals(148d,
                     length,
                     1d);
    }

    @Test
    public void testGeodesicAreaForLineString() throws
                                                Exception {
        // when computing the geodesic area of a given LineString (no closed)
        final double area1 = getGeodesicArea(createLineString(gf,
                                                              createCoordinate(gf,
                                                                               47.226116d,
                                                                               -1.554169d),
                                                              createCoordinate(gf,
                                                                               47.226126d,
                                                                               -1.554097d),
                                                              createCoordinate(gf,
                                                                               47.225527d,
                                                                               -1.553986d)));

        // then the computed area should be roughly equals to 186m
        assertEquals(186d,
                     area1,
                     1d);

        // when computing the geodesic area of the same LineString but closed
        final double area2 = getGeodesicArea(createLineString(gf,
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
                                                                               47.226116d,
                                                                               -1.554169d)));
        // then the computed area should be the same
        assertEquals(area1,
                     area2,
                     0d);
    }

    @Test
    public void testGeodesicAreaForSimplePolygon() throws
                                                   Exception {
        // when computing the geodesic area of a given Polygon
        final double area = getGeodesicArea(createPolygon(gf,
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
                                            false);

        // then the computed area should be roughly equals to 378m
        assertEquals(378d,
                     area,
                     1d);
    }

    @Test
    public void testGeodesicAreaForPolygonWithHoles() throws
                                                      Exception {
        // when computing the geodesic area of a given Polygon with holes
        final double areaWithHole = getGeodesicArea(createPolygon(gf,
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
                                                    true);

        // then the computed area should be roughly equals to 470m
        assertEquals(470d,
                     areaWithHole,
                     1d);

        // when computing the geodesic area of the same Polygon but without checking holes
        final double area = getGeodesicArea(createPolygon(gf,
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
                                            false);

        // then the computed area should be roughly equals to 634m
        assertEquals(634d,
                     area,
                     1d);
    }
}