package com.makina.ecrins.maps.jts.geojson;

import android.app.Application;

import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Unit test for {@link FeatureStyle}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class FeatureStyleTest {

    private Application application;

    @Before
    public void setUp() throws
                        Exception {
        application = spy(RuntimeEnvironment.application);
    }

    @Test
    public void testDefaultBuilder() throws
                                     Exception {
        doReturn("#03f").when(application).getString(anyInt());

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
        doReturn("#aaa").when(application).getString(anyInt());

        // given a FeatureStyle
        final FeatureStyle featureStyle = FeatureStyle.Builder.newInstance(application)
                                                              .setStroke(false)
                                                              .setColorResourceId(android.R.color.darker_gray)
                                                              .setWeight(1)
                                                              .setOpacity(0.2)
                                                              .setFill(false)
                                                              .setFillColorResourceId(android.R.color.darker_gray)
                                                              .setFillOpacity(0.7)
                                                              .build();

        // then
        assertFalse(featureStyle.isStroke());
        assertEquals(android.R.color.darker_gray,
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
}