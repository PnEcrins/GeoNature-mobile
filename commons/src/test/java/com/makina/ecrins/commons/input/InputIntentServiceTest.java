package com.makina.ecrins.commons.input;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateFormat;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;

import static com.makina.ecrins.commons.input.AbstractInputIntentService.KEY_PREFERENCE_CURRENT_INPUT;
import static com.makina.ecrins.commons.input.AbstractInputIntentService.buildIntent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.buildService;

/**
 * Unit test for {@link AbstractInputIntentService}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class InputIntentServiceTest {

    private static final String DEFAULT_BROADCAST_ACTION = "broadcast";
    private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

    @Captor
    private ArgumentCaptor<DummyInput> inputArgumentCaptor;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadNullInput() throws
                                    Exception {
        // given intent to read an existing input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_READ,
                                              null);
        // when invoking service
        final DummyInputIntentService inputIntentService = invokeService(intent);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED_NOT_FOUND),
                                       inputArgumentCaptor.capture());

        final DummyInput input = inputArgumentCaptor.getValue();
        assertNull(input);
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void testReadExistingInput() throws
                                        Exception {
        // given intent to read an existing input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_READ,
                                              null);

        // and an existing input to read
        @SuppressWarnings("StringBufferReplaceableByString")
        final String jsonString = new StringBuilder().append('{')
                                                     .append("\"id\":")
                                                     .append(2)
                                                     .append(",\"input_type\":\"")
                                                     .append("fauna")
                                                     .append("\",\"initial_input\":\"nomade\",\"dateobs\":\"")
                                                     .append("2015/11/19")
                                                     .append("\",\"observers_id\":[],\"observers\":[],\"taxons\":[]")
                                                     .append("}")
                                                     .toString();

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                         .edit()
                         .putString(KEY_PREFERENCE_CURRENT_INPUT,
                                    jsonString)
                         .commit();

        // when invoking service
        final DummyInputIntentService inputIntentService = invokeService(intent);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED),
                                       inputArgumentCaptor.capture());

        final DummyInput input = inputArgumentCaptor.getValue();
        assertNotNull(input);
        Assert.assertEquals(2,
                            input.getInputId());
        Assert.assertEquals(InputType.FAUNA,
                            input.getType());

        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR,
                     2015);
        calendar.set(Calendar.MONTH,
                     Calendar.NOVEMBER);
        calendar.set(Calendar.DAY_OF_MONTH,
                     19);
        Assert.assertEquals(calendar.getTime(),
                            input.getDate());

        Assert.assertEquals(0,
                            input.getObservers()
                                 .size());
        Assert.assertEquals(0,
                            input.getTaxa()
                                 .size());
    }

    @Test
    public void testSaveNullInput() throws
                                    Exception {
        // given intent to save a null input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_SAVE,
                                              null);

        // when invoking service
        final DummyInputIntentService inputIntentService = invokeService(intent);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED_WITH_ERRORS),
                                       inputArgumentCaptor.capture());

        final DummyInput input = inputArgumentCaptor.getValue();
        assertNull(input);
    }

    @Test
    public void testSaveInput() throws
                                Exception {
        // given an empty input to save
        final DummyInput input = new DummyInput(InputType.FAUNA);

        // and an intent to save this input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_SAVE,
                                              input);

        // when invoking service
        final DummyInputIntentService inputIntentService = invokeService(intent);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED),
                                       inputArgumentCaptor.capture());

        final DummyInput inputSaved = inputArgumentCaptor.getValue();
        assertNotNull(input);
        assertEquals(input.getInputId(),
                     inputSaved.getInputId());
        assertEquals(input.getType(),
                     inputSaved.getType());

        final String jsonSaved = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                                                  .getString(KEY_PREFERENCE_CURRENT_INPUT,
                                                             null);
        assertNotNull(jsonSaved);
        // noinspection StringBufferReplaceableByString
        assertEquals(new StringBuilder().append('{')
                                        .append("\"id\":")
                                        .append(input.getInputId())
                                        .append(",\"input_type\":\"")
                                        .append(input.getType()
                                                     .getValue())
                                        .append("\",\"initial_input\":\"nomade\",\"dateobs\":\"")
                                        .append(DateFormat.format(DEFAULT_DATE_FORMAT,
                                                                  input.getDate())
                                                          .toString())
                                        .append("\",\"observers_id\":[],\"observers\":[],\"taxons\":[]")
                                        .append("}")
                                        .toString(),
                     jsonSaved);
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void testDeleteInput() throws
                                  Exception {
        // given intent to delete an existing input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_DELETE,
                                              null);

        // and an existing input to delete
        @SuppressWarnings("StringBufferReplaceableByString")
        final String jsonString = new StringBuilder().append('{')
                                                     .append("\"id\":")
                                                     .append(2)
                                                     .append(",\"input_type\":\"")
                                                     .append("fauna")
                                                     .append("\",\"initial_input\":\"nomade\",\"dateobs\":\"")
                                                     .append("2015/11/19")
                                                     .append("\",\"observers_id\":[],\"observers\":[],\"taxons\":[]")
                                                     .append("}")
                                                     .toString();

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                         .edit()
                         .putString(KEY_PREFERENCE_CURRENT_INPUT,
                                    jsonString)
                         .commit();

        // when invoking service
        final DummyInputIntentService inputIntentService = invokeService(intent);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED),
                                       inputArgumentCaptor.capture());

        final DummyInput input = inputArgumentCaptor.getValue();
        assertNull(input);

        final String noJsonFound = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                                                    .getString(KEY_PREFERENCE_CURRENT_INPUT,
                                                               null);
        assertNull(noJsonFound);
    }

    @Test
    public void testExportNullInput() throws
                                      Exception {
        // given intent to save a null input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_EXPORT,
                                              null);

        // when invoking service
        final DummyInputIntentService inputIntentService = invokeService(intent);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED_WITH_ERRORS),
                                       inputArgumentCaptor.capture());

        final DummyInput input = inputArgumentCaptor.getValue();
        assertNull(input);
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void testExportInput() throws
                                  Exception {
        // given an empty input to export
        final DummyInput input = new DummyInput(InputType.FAUNA);

        // and an existing input to delete after export
        @SuppressWarnings("StringBufferReplaceableByString")
        final String jsonString = new StringBuilder().append('{')
                                                     .append("\"id\":")
                                                     .append(input.getInputId())
                                                     .append(",\"input_type\":\"")
                                                     .append(input.getType()
                                                                  .getValue())
                                                     .append("\",\"initial_input\":\"nomade\",\"dateobs\":\"")
                                                     .append(DateFormat.format(DEFAULT_DATE_FORMAT,
                                                                               input.getDate())
                                                                       .toString())
                                                     .append("\",\"observers_id\":[],\"observers\":[],\"taxons\":[]")
                                                     .append("}")
                                                     .toString();

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                         .edit()
                         .putString(KEY_PREFERENCE_CURRENT_INPUT,
                                    jsonString)
                         .commit();

        // and an intent to export this input
        final Intent intent = buildTestIntent(DummyInputIntentService.ACTION_EXPORT,
                                              input);

        // when invoking service
        final StringWriter exportWriter = new StringWriter();
        final DummyInputIntentService inputIntentService = invokeService(intent,
                                                                         exportWriter);

        // then
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.STARTING));
        verify(inputIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractInputIntentService.Status.FINISHED),
                                       inputArgumentCaptor.capture());

        final DummyInput inputExported = inputArgumentCaptor.getValue();
        assertNotNull(inputExported);
        assertEquals(input.getInputId(),
                     inputExported.getInputId());
        assertEquals(input.getType(),
                     inputExported.getType());

        final String noJsonFound = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                                                    .getString(KEY_PREFERENCE_CURRENT_INPUT,
                                                               null);
        assertNull(noJsonFound);

        assertNotNull(exportWriter.toString());
        assertEquals(jsonString,
                     exportWriter.toString());
    }

    @NonNull
    private Intent buildTestIntent(@NonNull final String action,
                                   @Nullable DummyInput input) {
        return buildIntent(RuntimeEnvironment.application,
                           DummyInputIntentService.class,
                           action,
                           DEFAULT_BROADCAST_ACTION,
                           DEFAULT_DATE_FORMAT,
                           input);
    }

    @NonNull
    private DummyInputIntentService invokeService(@NonNull final Intent intent) {
        return invokeService(intent,
                             null);
    }

    @NonNull
    private DummyInputIntentService invokeService(@NonNull final Intent intent,
                                                  @Nullable final Writer writer) {
        final ServiceController<DummyInputIntentService> serviceController = buildService(DummyInputIntentService.class,
                                                                                          intent);
        final DummyInputIntentService inputIntentService = spy(serviceController.create()
                                                                                .get());

        if (writer != null) {
            try {
                doReturn(writer).when(inputIntentService)
                                .getInputExportWriter(any(DummyInput.class));
            }
            catch (IOException ignored) {

            }
        }

        inputIntentService.onHandleIntent(intent);

        return inputIntentService;
    }
}
