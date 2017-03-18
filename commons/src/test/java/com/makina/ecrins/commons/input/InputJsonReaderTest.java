package com.makina.ecrins.commons.input;

import android.util.JsonReader;

import com.makina.ecrins.commons.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.StringReader;
import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link InputJsonReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class InputJsonReaderTest {

    private InputJsonReader inputJsonReader;

    @Mock
    private InputJsonReader.OnInputJsonReaderListener onInputJsonReaderListener;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);

        doReturn(new DummyInput(InputType.FAUNA)).when(onInputJsonReaderListener)
                                                 .createInput();
        doReturn(new DummyTaxon(0L)).when(onInputJsonReaderListener)
                                    .createTaxon();

        inputJsonReader = spy(new InputJsonReader(onInputJsonReaderListener));
    }

    @Test
    public void testReadEmptyInput() throws
                                     Exception {
        // given a JSON string
        final String json = TestHelper.getFixture("input_empty.json");

        // when read this JSON string
        final StringReader reader = new StringReader(json);
        final DummyInput input = (DummyInput) inputJsonReader.read(reader);

        verify(onInputJsonReaderListener,
               never()).readAdditionalInputData(any(JsonReader.class),
                                                any(String.class),
                                                any(DummyInput.class));

        verify(onInputJsonReaderListener,
               never()).readAdditionalTaxonData(any(JsonReader.class),
                                                any(String.class),
                                                any(DummyTaxon.class));

        // then
        assertNotNull(input);
        assertEquals(2,
                     input.getInputId());
        assertEquals(InputType.FAUNA,
                     input.getType());

        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR,
                     2015);
        calendar.set(Calendar.MONTH,
                     Calendar.NOVEMBER);
        calendar.set(Calendar.DAY_OF_MONTH,
                     19);
        assertEquals(calendar.getTime(),
                     input.getDate());

        assertNull(input.getQualification());
        assertEquals(0,
                     input.getObservers()
                          .size());
        assertEquals(0,
                     input.getTaxa()
                          .size());
        assertEquals(-1,
                     input.getCurrentSelectedTaxonId());
    }

    @Test
    public void testRead() throws
                           Exception {
        // given a JSON string
        final String json = TestHelper.getFixture("input.json");

        // when read this JSON string
        final StringReader reader = new StringReader(json);
        final DummyInput input = (DummyInput) inputJsonReader.read(reader);

        verify(onInputJsonReaderListener,
               never()).readAdditionalInputData(any(JsonReader.class),
                                                any(String.class),
                                                any(DummyInput.class));

        verify(onInputJsonReaderListener,
               never()).readAdditionalTaxonData(any(JsonReader.class),
                                                any(String.class),
                                                any(DummyTaxon.class));

        // then
        assertNotNull(input);
        assertEquals(2,
                     input.getInputId());
        assertEquals(InputType.FAUNA,
                     input.getType());

        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR,
                     2015);
        calendar.set(Calendar.MONTH,
                     Calendar.NOVEMBER);
        calendar.set(Calendar.DAY_OF_MONTH,
                     19);
        assertEquals(calendar.getTime(),
                     input.getDate());

        final Qualification qualification = input.getQualification();
        assertNotNull(qualification);
        assertEquals(2,
                     qualification.getOrganism());
        assertEquals(140,
                     qualification.getProtocol());
        assertEquals(4,
                     qualification.getLot());

        assertEquals(1,
                     input.getObservers()
                          .size());

        final Observer observer1 = input.getObservers()
                                        .get(1L);
        assertNotNull(observer1);
        assertEquals(1,
                     observer1.getObserverId());

        assertEquals(1,
                     input.getTaxa()
                          .size());

        final DummyTaxon taxon1 = (DummyTaxon) input.getTaxa()
                                                    .get(3L);
        assertNotNull(taxon1);
        assertEquals(3L,
                     taxon1.getId());
        assertEquals(4L,
                     taxon1.getTaxonId());
        assertEquals("name",
                     taxon1.getNameEntered());
        assertEquals(5L,
                     taxon1.getCriterionId());
        assertEquals("comment",
                     taxon1.getComment());

        assertEquals(3L,
                     input.getCurrentSelectedTaxonId());
        assertNotNull(input.getCurrentSelectedTaxon());
        assertEquals(3L,
                     input.getCurrentSelectedTaxon()
                          .getId());
        assertEquals(4L,
                     input.getCurrentSelectedTaxon()
                          .getTaxonId());
        assertEquals("name",
                     input.getCurrentSelectedTaxon()
                          .getNameEntered());
        assertEquals(5L,
                     input.getCurrentSelectedTaxon()
                          .getCriterionId());
        assertEquals("comment",
                     input.getCurrentSelectedTaxon()
                          .getComment());
    }
}