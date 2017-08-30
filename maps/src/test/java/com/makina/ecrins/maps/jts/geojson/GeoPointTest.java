package com.makina.ecrins.maps.jts.geojson;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link GeoPoint}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class GeoPointTest {
    @Test
    public void testDistanceTo() throws
                                 Exception {

        // given two GeoPoints
        final GeoPoint geoPoint1 = new GeoPoint(47.225782d,
                                                -1.554476d);
        final GeoPoint geoPoint2 = new GeoPoint(47.226468d,
                                                -1.554996d);

        // when compute the distance between these two GeoPoints
        final double distance = geoPoint1.distanceTo(geoPoint2);

        // then the computed distance should be roughly equals to 90m
        assertTrue(distance > 90d);
        assertTrue(distance < 91d);
    }
}