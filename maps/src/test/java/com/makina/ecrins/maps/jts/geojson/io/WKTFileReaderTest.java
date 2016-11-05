package com.makina.ecrins.maps.jts.geojson.io;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static com.makina.ecrins.maps.TestHelper.getFixtureAsFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link WKTFileReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class WKTFileReaderTest {

    @Captor
    private ArgumentCaptor<FeatureCollection> featureCollectionArgumentCaptor;

    @Mock
    private WKTFileReader.OnWKTFileReaderListener onWKTFileReaderListener;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadFeatures() throws
                                   Exception {
        // given a WKT file to read
        final File wkt = getFixtureAsFile("features.wkt");

        // when parsing this file as WKT
        new WKTFileReader().readFeatures(wkt,
                                         onWKTFileReaderListener);

        // then
        verify(onWKTFileReaderListener).onStart(3);
        verify(onWKTFileReaderListener,
               never()).onError(any(Throwable.class));
        verify(onWKTFileReaderListener,
               times(3)).onProgress(anyInt(),
                                    any(Feature.class));
        verify(onWKTFileReaderListener).onFinish(featureCollectionArgumentCaptor.capture());

        final FeatureCollection featureCollection = featureCollectionArgumentCaptor.getValue();
        assertNotNull(featureCollection);
        assertEquals(3,
                     featureCollection.getFeatures()
                                      .size());
        assertNotNull(featureCollection.getFeature("69"));
        assertNotNull(featureCollection.getFeature("19"));
        assertNotNull(featureCollection.getFeature("146"));
    }
}