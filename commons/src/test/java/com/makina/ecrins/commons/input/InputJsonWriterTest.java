package com.makina.ecrins.commons.input;

import android.text.format.DateFormat;
import android.util.JsonWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link InputJsonWriter}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class InputJsonWriterTest {

    private InputJsonWriter inputJsonWriter;

    @Mock
    private InputJsonWriter.OnInputJsonWriterListener onInputJsonWriterListener;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);

        inputJsonWriter = spy(new InputJsonWriter(onInputJsonWriterListener));
    }

    @Test
    public void testWriteEmptyInput() throws
                                      Exception {
        // given an empty input
        final DummyInput input = new DummyInput(InputType.FAUNA);

        // when write this input as JSON string
        final StringWriter writer = new StringWriter();
        inputJsonWriter.write(writer,
                              input);

        verify(onInputJsonWriterListener,
               atLeastOnce()).writeAdditionalInputData(any(JsonWriter.class),
                                                       eq(input));

        verify(onInputJsonWriterListener,
               never()).writeAdditionalTaxonData(any(JsonWriter.class),
                                                 any(DummyTaxon.class));

        // then
        assertNotNull(writer.toString());

        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder expectedJsonString = new StringBuilder();
        expectedJsonString.append('{');
        expectedJsonString.append("\"id\":");
        expectedJsonString.append(input.getInputId());
        expectedJsonString.append(",\"input_type\":\"");
        expectedJsonString.append(input.getType()
                                       .getValue());
        expectedJsonString.append("\",\"initial_input\":\"nomade\",\"dateobs\":\"");
        expectedJsonString.append(DateFormat.format(inputJsonWriter.getDateFormat(),
                                                    input.getDate())
                                            .toString());
        expectedJsonString.append("\",\"observers_id\":[],\"taxons\":[]");
        expectedJsonString.append("}");

        assertEquals(expectedJsonString.toString(),
                     writer.toString());
    }

    @Test
    public void testWrite() throws
                            Exception {
        // given an empty input
        final DummyInput input = new DummyInput(InputType.FAUNA);
        final Observer observer1 = new Observer(1L,
                                                "lastname",
                                                "firstname");
        input.getObservers()
             .put(observer1.getObserverId(),
                  observer1);


        final DummyTaxon taxon1 = new DummyTaxon(1L);
        taxon1.setNameEntered("name1");
        taxon1.setCriterionId(4L);
        taxon1.setComment("comment for taxon 1");

        input.getTaxa()
             .put(taxon1.getId(),
                  taxon1);

        // when write this input as JSON string
        final StringWriter writer = new StringWriter();
        inputJsonWriter.write(writer,
                              input);

        verify(onInputJsonWriterListener,
               atLeastOnce()).writeAdditionalInputData(any(JsonWriter.class),
                                                       eq(input));

        verify(onInputJsonWriterListener,
               atLeastOnce()).writeAdditionalTaxonData(any(JsonWriter.class),
                                                       eq(taxon1));

        // then
        assertNotNull(writer.toString());

        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder expectedJsonString = new StringBuilder();
        expectedJsonString.append('{');
        expectedJsonString.append("\"id\":");
        expectedJsonString.append(input.getInputId());
        expectedJsonString.append(",\"input_type\":\"");
        expectedJsonString.append(input.getType()
                                       .getValue());
        expectedJsonString.append("\",\"initial_input\":\"nomade\",\"dateobs\":\"");
        expectedJsonString.append(DateFormat.format(inputJsonWriter.getDateFormat(),
                                                    input.getDate())
                                            .toString());
        expectedJsonString.append("\",\"observers_id\":[1],\"taxons\":[{\"id\":");
        expectedJsonString.append(taxon1.getId());
        expectedJsonString.append(",\"id_taxon\":");
        expectedJsonString.append(taxon1.getTaxonId());
        expectedJsonString.append(",\"name_entered\":\"");
        expectedJsonString.append(taxon1.getNameEntered());
        expectedJsonString.append("\",\"observation\":{\"criterion\":");
        expectedJsonString.append(taxon1.getCriterionId());
        expectedJsonString.append("},\"comment\":\"");
        expectedJsonString.append(taxon1.getComment());
        expectedJsonString.append("\"}]}");

        assertEquals(expectedJsonString.toString(),
                     writer.toString());
    }
}