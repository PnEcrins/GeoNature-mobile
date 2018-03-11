package com.geonature.mobile.maps.jts.geojson;

/**
 * Defines all geographical constants.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IGeoConstants {

    // http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius
    int RADIUS_EARTH_METERS = 6378137;

    int EQUATOR_CIRCUMFERENCE = (int) (2 * Math.PI * RADIUS_EARTH_METERS);
}
