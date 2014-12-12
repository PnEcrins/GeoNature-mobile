package com.makina.ecrins.maps.geojson.geometry;

/**
 * Defines all geographical constants.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IGeoConstants {

    // http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius
    public static final int RADIUS_EARTH_METERS = 6378137;

    public static final int EQUATOR_CIRCUMFERENCE = (int) (2 * Math.PI * RADIUS_EARTH_METERS);
}
