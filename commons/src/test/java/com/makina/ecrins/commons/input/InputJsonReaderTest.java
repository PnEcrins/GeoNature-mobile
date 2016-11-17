package com.makina.ecrins.commons.input;

import android.util.JsonReader;

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
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder jsonString = new StringBuilder();
        jsonString.append('{');
        jsonString.append("\"id\":");
        jsonString.append(2);
        jsonString.append(",\"input_type\":\"");
        jsonString.append("fauna");
        jsonString.append("\",\"initial_input\":\"nomade\",\"dateobs\":\"");
        jsonString.append("2015/11/19");
        jsonString.append("\",\"observers_id\":[],\"taxons\":[]");
        jsonString.append("}");

        // when read this JSON string
        final StringReader reader = new StringReader(jsonString.toString());
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

        assertEquals(0,
                     input.getObservers()
                          .size());
        assertEquals(0,
                     input.getTaxa()
                          .size());
    }

    @Test
    public void testRead() throws
                           Exception {
        // given a JSON string
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder jsonString = new StringBuilder();
        jsonString.append('{');
        jsonString.append("\"id\":");
        jsonString.append(2);
        jsonString.append(",\"input_type\":\"");
        jsonString.append("fauna");
        jsonString.append("\",\"initial_input\":\"nomade\",\"dateobs\":\"");
        jsonString.append("2015/11/19");
        jsonString.append("\",\"observers_id\":[1],\"taxons\":[{\"id\":");
        jsonString.append(3);
        jsonString.append(",\"id_taxon\":");
        jsonString.append(4);
        jsonString.append(",\"name_entered\":\"");
        jsonString.append("name");
        jsonString.append("\",\"observation\":{\"criterion\":");
        jsonString.append(5);
        jsonString.append("},\"comment\":\"");
        jsonString.append("comment");
        jsonString.append("\"}]}");

        // when read this JSON string
        final StringReader reader = new StringReader(jsonString.toString());
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
    }
}