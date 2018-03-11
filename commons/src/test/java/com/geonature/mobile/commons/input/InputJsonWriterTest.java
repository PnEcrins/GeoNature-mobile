package com.geonature.mobile.commons.input;

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
        final String expectedJsonString = new StringBuilder().append('{')
                                                             .append("\"id\":")
                                                             .append(input.getInputId())
                                                             .append(",\"input_type\":\"")
                                                             .append(input.getType()
                                                                          .getValue())
                                                             .append("\",\"initial_input\":\"nomade\",\"dateobs\":\"")
                                                             .append(DateFormat.format(inputJsonWriter.getDateFormat(),
                                                                                       input.getDate())
                                                                               .toString())
                                                             .append("\",\"observers_id\":[],\"observers\":[],\"taxons\":[]")
                                                             .append("}")
                                                             .toString();

        assertEquals(expectedJsonString,
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

        input.setQualification(new Qualification(2,
                                                 140,
                                                 4));

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
        final String expectedJsonString = new StringBuilder().append('{')
                                                             .append("\"id\":")
                                                             .append(input.getInputId())
                                                             .append(",\"input_type\":\"")
                                                             .append(input.getType()
                                                                          .getValue())
                                                             .append("\",\"initial_input\":\"nomade\",\"dateobs\":\"")
                                                             .append(DateFormat.format(inputJsonWriter.getDateFormat(),
                                                                                       input.getDate())
                                                                               .toString())
                                                             .append("\",\"qualification\":{")
                                                             .append("\"organism\":2")
                                                             .append(",\"protocol\":140")
                                                             .append(",\"lot\":4}")
                                                             .append(",\"observers_id\":[")
                                                             .append(observer1.getObserverId())
                                                             .append("],\"observers\":[{\"id\":")
                                                             .append(observer1.getObserverId())
                                                             .append(",\"lastname\":\"")
                                                             .append(observer1.getLastname())
                                                             .append("\",\"firstname\":\"")
                                                             .append(observer1.getFirstname())
                                                             .append("\"}],\"taxons\":[{\"id\":")
                                                             .append(taxon1.getId())
                                                             .append(",\"id_taxon\":")
                                                             .append(taxon1.getTaxonId())
                                                             .append(",\"name_entered\":\"")
                                                             .append(taxon1.getNameEntered())
                                                             .append("\",\"observation\":{\"criterion\":")
                                                             .append(taxon1.getCriterionId())
                                                             .append("},\"comment\":\"")
                                                             .append(taxon1.getComment())
                                                             .append("\"}]}")
                                                             .toString();

        assertEquals(expectedJsonString,
                     writer.toString());
    }
}