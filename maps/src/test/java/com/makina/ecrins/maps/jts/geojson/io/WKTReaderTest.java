package com.makina.ecrins.maps.jts.geojson.io;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.StringReader;
import java.util.List;

import static com.makina.ecrins.maps.TestHelper.getFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link WKTReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class WKTReaderTest {

    @Captor
    private ArgumentCaptor<FeatureCollection> featureCollectionArgumentCaptor;

    @Mock
    private WKTReader.OnWKTReaderListener onWKTReaderListener;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadFeaturesThroughCallback() throws
                                                  Exception {
        // given a WKT file to read
        final String wkt = getFixture("features.wkt");

        // when parsing this file as WKT
        new WKTReader().readFeatures(new StringReader(wkt),
                                     onWKTReaderListener);

        // then
        verify(onWKTReaderListener,
               never()).onError(any(Throwable.class));
        verify(onWKTReaderListener,
               times(3)).onProgress(anyInt(),
                                    any(Feature.class));
        verify(onWKTReaderListener).onFinish(featureCollectionArgumentCaptor.capture());

        final FeatureCollection featureCollection = featureCollectionArgumentCaptor.getValue();
        assertNotNull(featureCollection);
        assertEquals(3,
                     featureCollection.getFeatures()
                                      .size());
        assertNotNull(featureCollection.getFeature("69"));
        assertNotNull(featureCollection.getFeature("19"));
        assertNotNull(featureCollection.getFeature("146"));
    }

    @Test
    public void testReadFeatures() throws
                                   Exception {
        // given a WKT file to read
        final String wkt = getFixture("features.wkt");

        // when parsing this file as WKT
        final List<Feature> features = new WKTReader().readFeatures(new StringReader(wkt));

        // then
        assertNotNull(features);
        assertEquals(3,
                     features.size());
        assertEquals("69",
                     features.get(0)
                             .getId());
        assertNotNull(features.get(0)
                              .getGeometry());
        assertEquals("Polygon",
                     features.get(0)
                             .getGeometry()
                             .getGeometryType());
        assertEquals("19",
                     features.get(1)
                             .getId());
        assertNotNull(features.get(1)
                              .getGeometry());
        assertEquals("Polygon",
                     features.get(1)
                             .getGeometry()
                             .getGeometryType());
        assertEquals("146",
                     features.get(2)
                             .getId());
        assertNotNull(features.get(2)
                              .getGeometry());
        assertEquals("Polygon",
                     features.get(2)
                             .getGeometry()
                             .getGeometryType());
    }

    @Test
    public void testReadFeatureCollection() throws
                                            Exception {
        // given a WKT file to read
        final String wkt = getFixture("features.wkt");

        // when parsing this file as WKT
        final FeatureCollection featureCollection = new WKTReader().readFeatureCollection(new StringReader(wkt));

        // then
        assertNotNull(featureCollection);
        assertEquals(3,
                     featureCollection.getFeatures()
                                      .size());
        assertNotNull(featureCollection.getFeature("69"));
        assertNotNull(featureCollection.getFeature("19"));
        assertNotNull(featureCollection.getFeature("146"));
    }
}