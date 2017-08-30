package com.makina.ecrins.maps.settings;

import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link MapSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class MapSettingsTest {

    @Test
    public void testDefaultBuilder() throws
                                     Exception {
        // given a default Builder of MapSettings
        final MapSettings.Builder builder = MapSettings.Builder.newInstance();

        // when building a default instance of MapSettings
        final MapSettings mapSettings = builder.build();

        // then
        assertNotNull(mapSettings);
        assertTrue(mapSettings.isDisplayScale());
        assertFalse(mapSettings.isShowUnitiesLayer());
        assertNull(mapSettings.getCRSSettings());
        assertNotNull(mapSettings.getMaxBounds());
        assertTrue(mapSettings.getMaxBounds()
                              .isEmpty());
        assertNull(mapSettings.getPolygonBounds());
        assertNull(mapSettings.getCenter());
        assertEquals(0,
                     mapSettings.getZoom());
        assertEquals(0,
                     mapSettings.getMinZoom());
        assertEquals(0,
                     mapSettings.getMaxZoom());
        assertEquals(0,
                     mapSettings.getMinimumZoomPointing());
        assertTrue(mapSettings.getLayers()
                              .isEmpty());
        assertNull(mapSettings.getUnityLayer());
    }

    @Test
    public void testBuilder() throws
                              Exception {
        // given a default Builder of MapSettings
        final MapSettings.Builder builder = MapSettings.Builder.newInstance();


        // when building an instance of MapSettings
        final MapSettings mapSettings = builder.showScale(false)
                                               .setCRSSettings(new CRSSettings("EPSG:2154",
                                                                               "+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                                                                               Arrays.asList(914987,
                                                                                             6372012,
                                                                                             994987,
                                                                                             6460012)))
                                               .setMaxBounds(Arrays.asList(new GeoPoint(44.024060d,
                                                                                        5.31056d),
                                                                           new GeoPoint(45.576247d,
                                                                                        7.146750d)))
                                               .setCenter(new GeoPoint(44.795154d,
                                                                       6.228655d))
                                               .setZoom(2)
                                               .setMinZoom(1)
                                               .setMaxZoom(10)
                                               .setMinZoomPointing(7)
                                               .addLayerSettings(new LayerSettings("scan.mbtiles",
                                                                                   "Scan",
                                                                                   "mbtiles"))
                                               .addLayerSettings(new LayerSettings("ortho",
                                                                                   "Ortho",
                                                                                   "mbtiles_split"))
                                               .setUnitiesLayer(new LayerSettings("unities.mbtiles",
                                                                                  "Unities",
                                                                                  "mbtiles"))
                                               .build();

        // then
        assertNotNull(mapSettings);
        assertFalse(mapSettings.isDisplayScale());
        assertTrue(mapSettings.isShowUnitiesLayer());
        assertNotNull(mapSettings.getCRSSettings());
        assertEquals("EPSG:2154",
                     mapSettings.getCRSSettings()
                                .getCode());
        assertEquals("+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                     mapSettings.getCRSSettings()
                                .getDef());
        assertEquals(Arrays.asList(914987,
                                   6372012,
                                   994987,
                                   6460012),
                     mapSettings.getCRSSettings()
                                .getBbox());
        assertNotNull(mapSettings.getMaxBounds());
        assertEquals(Arrays.asList(new GeoPoint(44.024060d,
                                                5.31056d),
                                   new GeoPoint(45.576247d,
                                                7.146750d)),
                     mapSettings.getMaxBounds());
        assertNotNull(mapSettings.getPolygonBounds());
        assertEquals(new GeometryFactory().toGeometry(new Envelope(mapSettings.getMaxBounds()
                                                                              .get(0)
                                                                              .getPoint()
                                                                              .getCoordinate(),
                                                                   mapSettings.getMaxBounds()
                                                                              .get(1)
                                                                              .getPoint()
                                                                              .getCoordinate())),
                     mapSettings.getPolygonBounds());
        assertNotNull(mapSettings.getCenter());
        assertEquals(new GeoPoint(44.795154d,
                                  6.228655d),
                     mapSettings.getCenter());
        assertEquals(2,
                     mapSettings.getZoom());
        assertEquals(1,
                     mapSettings.getMinZoom());
        assertEquals(10,
                     mapSettings.getMaxZoom());
        assertEquals(7,
                     mapSettings.getMinimumZoomPointing());
        assertEquals(2,
                     mapSettings.getLayers()
                                .size());
        assertEquals("scan.mbtiles",
                     mapSettings.getLayers()
                                .get(0)
                                .getName());
        assertEquals("Scan",
                     mapSettings.getLayers()
                                .get(0)
                                .getLabel());
        assertEquals(LayerSettings.SOURCE_MBTILES,
                     mapSettings.getLayers()
                                .get(0)
                                .getSource());
        assertEquals("ortho",
                     mapSettings.getLayers()
                                .get(1)
                                .getName());
        assertEquals("Ortho",
                     mapSettings.getLayers()
                                .get(1)
                                .getLabel());
        assertEquals(LayerSettings.SOURCE_MBTILES_SPLIT,
                     mapSettings.getLayers()
                                .get(1)
                                .getSource());
        assertNotNull(mapSettings.getUnityLayer());
        assertEquals("unities.mbtiles",
                     mapSettings.getUnityLayer()
                                .getName());
        assertEquals("Unities",
                     mapSettings.getUnityLayer()
                                .getLabel());
        assertEquals(LayerSettings.SOURCE_MBTILES,
                     mapSettings.getUnityLayer()
                                .getSource());
    }
}