package com.makina.ecrins.maps.jts.geojson;

import android.app.Application;

import com.makina.ecrins.maps.BuildConfig;
import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for {@link FeatureStyle}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21,
        manifest = "src/main/AndroidManifest.xml",
        packageName = "com.makina.ecrins.maps")
public class FeatureStyleTest {

    private Application application;

    @Before
    public void setUp() throws
                        Exception {
        application = RuntimeEnvironment.application;
    }

    @Test
    public void testDefaultBuilder() throws
                                     Exception {
        // given a default FeatureStyle
        final FeatureStyle featureStyle = FeatureStyle.Builder.newInstance(application)
                                                              .build();
        // then
        assertTrue(featureStyle.isStroke());
        assertEquals(R.color.feature_dark_blue,
                     featureStyle.getColorResourceId());
        assertEquals(5,
                     featureStyle.getWeight());
        assertEquals(0.5,
                     featureStyle.getOpacity());
        assertTrue(featureStyle.isFill());
        assertEquals(R.color.feature_dark_blue,
                     featureStyle.getFillColorResourceId());
        assertEquals(0.2,
                     featureStyle.getFillOpacity());
        assertEquals(TestHelper.getFixture("featurestyle_default.json"),
                     featureStyle.toString());
    }

    @Test
    public void testBuilder() throws
                              Exception {
        // given a FeatureStyle
        final FeatureStyle featureStyle = FeatureStyle.Builder.newInstance(application)
                                                              .setStroke(false)
                                                              .setColorResourceId(R.color.feature_dark_blue)
                                                              .setWeight(1)
                                                              .setOpacity(0.2)
                                                              .setFill(false)
                                                              .setFillColorResourceId(android.R.color.darker_gray)
                                                              .setFillOpacity(0.7)
                                                              .build();

        // then
        assertFalse(featureStyle.isStroke());
        assertEquals(R.color.feature_dark_blue,
                     featureStyle.getColorResourceId());
        assertEquals(1,
                     featureStyle.getWeight());
        assertEquals(0.2,
                     featureStyle.getOpacity());
        assertFalse(featureStyle.isFill());
        assertEquals(android.R.color.darker_gray,
                     featureStyle.getFillColorResourceId());
        assertEquals(0.7,
                     featureStyle.getFillOpacity());
        assertEquals(TestHelper.getFixture("featurestyle.json"),
                     featureStyle.toString());
    }

    @Test
    public void testBuilderFromExistingFeatureStyle() throws
                                                      Exception {
        // given a FeatureStyle
        final FeatureStyle featureStyle = FeatureStyle.Builder.newInstance(application)
                                                              .setStroke(false)
                                                              .setColorResourceId(R.color.feature_dark_blue)
                                                              .setWeight(1)
                                                              .setOpacity(0.2)
                                                              .setFill(false)
                                                              .setFillColorResourceId(android.R.color.darker_gray)
                                                              .setFillOpacity(0.7)
                                                              .build();

        // when building a new FeatureStyle from an existing one
        final FeatureStyle newFeatureStyle = FeatureStyle.Builder.newInstance(application)
                                                                 .from(featureStyle)
                                                                 .build();

        // then
        assertFalse(newFeatureStyle.isStroke());
        assertEquals(R.color.feature_dark_blue,
                     newFeatureStyle.getColorResourceId());
        assertEquals(1,
                     newFeatureStyle.getWeight());
        assertEquals(0.2,
                     newFeatureStyle.getOpacity());
        assertFalse(newFeatureStyle.isFill());
        assertEquals(android.R.color.darker_gray,
                     newFeatureStyle.getFillColorResourceId());
        assertEquals(0.7,
                     newFeatureStyle.getFillOpacity());
        assertEquals(TestHelper.getFixture("featurestyle.json"),
                     newFeatureStyle.toString());
    }
}