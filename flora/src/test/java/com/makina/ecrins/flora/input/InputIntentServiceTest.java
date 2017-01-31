package com.makina.ecrins.flora.input;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.makina.ecrins.commons.input.InputJsonReader;
import com.makina.ecrins.commons.input.InputJsonWriter;
import com.makina.ecrins.flora.TestHelper;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link InputIntentService}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class InputIntentServiceTest {

    private InputIntentService inputIntentService;
    private InputJsonReader inputJsonReader;
    private InputJsonWriter inputJsonWriter;

    @Before
    public void setUp() throws
                        Exception {
        inputIntentService = spy(new InputIntentService());
        inputJsonReader = spy(new InputJsonReader(inputIntentService));
        inputJsonWriter = spy(new InputJsonWriter(inputIntentService));
    }

    @Test
    public void testReadEmptyInput() throws
                                     Exception {
        // given a JSON string
        final String jsonString = TestHelper.getFixture("input_empty.json");

        // when read this JSON string
        final Input input = (Input) inputJsonReader.read(jsonString);


        verify(inputIntentService,
               never()).readAdditionalInputData(any(JsonReader.class),
                                                any(String.class),
                                                any(Input.class));

        verify(inputIntentService,
               never()).readAdditionalTaxonData(any(JsonReader.class),
                                                any(String.class),
                                                any(Taxon.class));

        // then
        assertNotNull(input);
    }

    @Test
    public void testReadInputWithEmptyAreas() throws
                                              Exception {
        // given a JSON string
        final String jsonString = TestHelper.getFixture("input_areas_empty.json");

        // when read this JSON string
        final Input input = (Input) inputJsonReader.read(jsonString);


        verify(inputIntentService,
               never()).readAdditionalInputData(any(JsonReader.class),
                                                any(String.class),
                                                any(Input.class));

        verify(inputIntentService,
               times(2)).readAdditionalTaxonData(any(JsonReader.class),
                                                 any(String.class),
                                                 any(Taxon.class));

        // then
        assertNotNull(input);
        assertNotNull(input.getCurrentSelectedTaxon());
        assertNotNull(((Taxon) input.getCurrentSelectedTaxon()).getAreas());
        assertTrue(((Taxon) input.getCurrentSelectedTaxon()).getAreas()
                                                            .isEmpty());
        assertNull(((Taxon) input.getCurrentSelectedTaxon()).getLastInsertedAreaId());
        assertNull(((Taxon) input.getCurrentSelectedTaxon()).getCurrentSelectedAreaId());
        assertNull(((Taxon) input.getCurrentSelectedTaxon()).getCurrentSelectedArea());
        assertNull(((Taxon) input.getCurrentSelectedTaxon()).getProspectingArea());
    }

    @Test
    public void testReadInputWithOneArea() throws
                                           Exception {
        // given a JSON string
        final String jsonString = TestHelper.getFixture("input_areas_one.json");

        // when read this JSON string
        final Input input = (Input) inputJsonReader.read(jsonString);

        verify(inputIntentService,
               never()).readAdditionalInputData(any(JsonReader.class),
                                                any(String.class),
                                                any(Input.class));

        verify(inputIntentService,
               times(2)).readAdditionalTaxonData(any(JsonReader.class),
                                                 any(String.class),
                                                 any(Taxon.class));

        // then
        assertNotNull(input);
        assertNotNull(input.getCurrentSelectedTaxon());
        assertNotNull(((Taxon) input.getCurrentSelectedTaxon()).getAreas());
        assertFalse(((Taxon) input.getCurrentSelectedTaxon()).getAreas()
                                                             .isEmpty());
        assertEquals("point_6",
                     ((Taxon) input.getCurrentSelectedTaxon()).getCurrentSelectedAreaId());
        assertEquals("point_6",
                     ((Taxon) input.getCurrentSelectedTaxon()).getLastInsertedAreaId());

        final Area area = ((Taxon) input.getCurrentSelectedTaxon()).getCurrentSelectedArea();
        assertNotNull(area);
        assertEquals(6,
                     area.getAreaId());
        assertEquals(0.1d,
                     area.getInclineValue());
        assertEquals(3.14d,
                     area.getArea());
        assertEquals(3.1d,
                     area.getComputedArea());
        assertNotNull(area.getFeature());
        assertEquals("point_6",
                     area.getFeature()
                         .getId());
        assertNotNull(area.getFeature()
                          .getGeometry());
        assertEquals("Point",
                     area.getFeature()
                         .getGeometry()
                         .getGeometryType());
        assertNull(area.getFrequency());
        assertEquals(1L,
                     area.getPhenologyId());
        assertNotNull(area.getCounting());
        assertEquals(Counting.CountingType.NONE,
                     area.getCounting()
                         .getType());
        assertTrue(area.getSelectedPhysiognomy()
                       .isEmpty());
        assertTrue(area.getSelectedDisturbances()
                       .isEmpty());
        assertNull(area.getComment());
    }

    @Test
    public void testWriteEmptyInput() throws
                                      Exception {
        // given an empty input
        final Input input = spy(new Input());
        doReturn(2L).when(input)
                    .getInputId();
        doReturn(getDefaultDate()).when(input)
                                  .getDate();

        // when write this input as JSON string
        final String jsonString = inputJsonWriter.write(input);

        verify(inputIntentService,
               atLeastOnce()).writeAdditionalInputData(any(JsonWriter.class),
                                                       eq(input));

        verify(inputIntentService,
               never()).writeAdditionalTaxonData(any(JsonWriter.class),
                                                 any(Taxon.class));

        // then
        assertNotNull(jsonString);
        assertEquals(TestHelper.getFixture("input_empty.json"),
                     jsonString);
    }

    @Test
    public void testWriteInputWithEmptyAreas() throws
                                               Exception {
        // given an empty input
        final Input input = spy(new Input());
        doReturn(2L).when(input)
                    .getInputId();
        doReturn(getDefaultDate()).when(input)
                                  .getDate();

        final Taxon taxon = spy(new Taxon(4));
        doReturn(3L).when(taxon)
                    .getId();
        taxon.setNameEntered("taxon");
        taxon.setComment(null);

        input.getTaxa()
             .put(taxon.getId(),
                  taxon);

        // when write this input as JSON string
        final String jsonString = inputJsonWriter.write(input);

        verify(inputIntentService,
               atLeastOnce()).writeAdditionalInputData(any(JsonWriter.class),
                                                       eq(input));

        verify(inputIntentService,
               times(1)).writeAdditionalTaxonData(any(JsonWriter.class),
                                                  any(Taxon.class));

        // then
        assertNotNull(jsonString);
        assertEquals(TestHelper.getFixture("input_areas_empty.json"),
                     jsonString);
    }

    @Test
    public void testWriteInputWithOneArea() throws
                                            Exception {
        // given an empty input
        final Input input = spy(new Input());
        doReturn(2L).when(input)
                    .getInputId();
        doReturn(getDefaultDate()).when(input)
                                  .getDate();

        final Taxon taxon = spy(new Taxon(4));
        doReturn(3L).when(taxon)
                    .getId();
        taxon.setNameEntered("taxon");
        taxon.setComment(null);

        final Area area = spy(new Area(new Feature("point_6",
                                                   new GeometryFactory().createPoint(new Coordinate(-1.5545135,
                                                                                                    47.2256258)))));
        doReturn(6L).when(area)
                    .getAreaId();
        area.setInclineValue(0.1d);
        area.setArea(3.14d);
        area.setComputedArea(3.1d);
        area.setPhenologyId(1);
        area.setComment(null);

        assert area.getFeature() != null;

        taxon.getAreas()
             .put(area.getFeature()
                      .getId(),
                  area);

        input.getTaxa()
             .put(taxon.getId(),
                  taxon);

        // when write this input as JSON string
        final String jsonString = inputJsonWriter.write(input);

        verify(inputIntentService,
               atLeastOnce()).writeAdditionalInputData(any(JsonWriter.class),
                                                       eq(input));

        verify(inputIntentService,
               times(1)).writeAdditionalTaxonData(any(JsonWriter.class),
                                                  any(Taxon.class));

        // then
        assertNotNull(jsonString);
        assertEquals(TestHelper.getFixture("input_areas_one.json"),
                     jsonString);
    }

    private Date getDefaultDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2015,
                     Calendar.NOVEMBER,
                     19);
        return calendar.getTime();
    }
}