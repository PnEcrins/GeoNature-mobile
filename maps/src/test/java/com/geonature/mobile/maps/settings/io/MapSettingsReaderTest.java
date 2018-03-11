package com.geonature.mobile.maps.settings.io;

import com.geonature.mobile.maps.TestHelper;
import com.geonature.mobile.maps.jts.geojson.GeoPoint;
import com.geonature.mobile.maps.settings.LayerSettings;
import com.geonature.mobile.maps.settings.MapSettings;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link MapSettingsReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class MapSettingsReaderTest {

    private MapSettingsReader mapSettingsReader;

    @Before
    public void setUp() throws
                        Exception {
        mapSettingsReader = new MapSettingsReader();
    }

    @Test
    public void testReadMapSettingsFromJsonString() throws
                                                    Exception {
        // given a JSON settings
        final String json = TestHelper.getFixture("map_settings.json");

        // when read the JSON as MapSettings
        final MapSettings mapSettings = mapSettingsReader.readMapSettings(json);

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

    @Test
    public void testReadMapSettingsFromInvalidJsonString() throws
                                                       Exception {
        // when read an invalid JSON as MapSettings
        final MapSettings mapSettings = mapSettingsReader.readMapSettings("");

        // then
        assertNull(mapSettings);
    }
}