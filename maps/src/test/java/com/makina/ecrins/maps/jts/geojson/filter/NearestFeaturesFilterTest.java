package com.makina.ecrins.maps.jts.geojson.filter;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.makina.ecrins.maps.jts.geojson.JTSTestHelper.createPoint;
import static com.makina.ecrins.maps.jts.geojson.filter.NearestFeaturesFilter.getFilteredFeatures;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link NearestFeaturesFilter}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class NearestFeaturesFilterTest {

    private GeometryFactory gf;

    @Before
    public void setUp() throws
                        Exception {
        gf = new GeometryFactory();
    }

    @Test
    public void testNearestFeaturesFromFeatures() throws
                                                  Exception {
        // given a GeoPoint
        final GeoPoint geoPoint = new GeoPoint(47.225782d,
                                               -1.554476d);
        // and a list of Features to check
        final List<Feature> features = asList(new Feature("id1",
                                                          createPoint(gf,
                                                                      47.226468d,
                                                                      -1.554996d)),
                                              new Feature("id2",
                                                          createPoint(gf,
                                                                      47.226126d,
                                                                      -1.554381d)));

        // when applying filter
        final List<Feature> filteredFeatures = getFilteredFeatures(geoPoint,
                                                                   45d,
                                                                   features);

        // then
        assertNotNull(filteredFeatures);
        assertEquals(1,
                     filteredFeatures.size());
        assertEquals(features.get(1),
                     filteredFeatures.get(0));
    }

    @Test
    public void testNearestFeaturesFromFeatureCollection() throws
                                                           Exception {
        // given a GeoPoint
        final GeoPoint geoPoint = new GeoPoint(47.225782d,
                                               -1.554476);

        // and a FeatureCollection to check
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.addFeature(new Feature("id1",
                                                 createPoint(gf,
                                                             47.226468d,
                                                             -1.554996d)));
        featureCollection.addFeature(new Feature("id2",
                                                 createPoint(gf,
                                                             47.226126d,
                                                             -1.554381d)));

        // when applying filter
        final List<Feature> filteredFeatures = getFilteredFeatures(geoPoint,
                                                                   45d,
                                                                   featureCollection);

        // then
        assertNotNull(filteredFeatures);
        assertEquals(1,
                     filteredFeatures.size());
        assertEquals(featureCollection.getFeature("id2"),
                     filteredFeatures.get(0));
    }
}