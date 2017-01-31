package com.makina.ecrins.commons.settings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.makina.ecrins.commons.TestHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ServiceController;

import java.io.IOException;
import java.io.StringReader;

import static com.makina.ecrins.commons.settings.AbstractAppSettingsIntentService.buildIntent;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.buildService;

/**
 * Unit test for {@link AbstractAppSettingsIntentService}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class AppSettingsIntentServiceTest {

    private static final String DEFAULT_BROADCAST_ACTION = "broadcast";

    @Captor
    private ArgumentCaptor<DummyAppSettings> appSettingsArgumentCaptor;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadNoSuchAppSettings() throws
                                            Exception {
        // given intent to read an existing app settings
        final Intent intent = buildTestIntent(AbstractAppSettingsIntentService.ACTION_READ,
                                              "");
        // when invoking service
        final DummyAppSettingsIntentService appSettingsIntentService = invokeService(intent);

        // then
        verify(appSettingsIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractAppSettingsIntentService.Status.STARTING));
        verify(appSettingsIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractAppSettingsIntentService.Status.FINISHED_WITH_ERRORS),
                                       appSettingsArgumentCaptor.capture());

        final DummyAppSettings appSettings = appSettingsArgumentCaptor.getValue();
        assertNull(appSettings);
    }

    @Test
    public void testReadAppSettings() throws
                                      Exception {
        // given intent to read an existing app settings
        final Intent intent = buildTestIntent(AbstractAppSettingsIntentService.ACTION_READ,
                                              "app_settings_with_db.json");

        // when invoking service
        final DummyAppSettingsIntentService appSettingsIntentService = invokeService(intent,
                                                                                     "app_settings_with_db.json");

        // then
        verify(appSettingsIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractAppSettingsIntentService.Status.STARTING));
        verify(appSettingsIntentService,
               times(1)).sendBroadcast(eq(DEFAULT_BROADCAST_ACTION),
                                       eq(AbstractAppSettingsIntentService.Status.FINISHED),
                                       appSettingsArgumentCaptor.capture());

        final DummyAppSettings appSettings = appSettingsArgumentCaptor.getValue();
        assertNotNull(appSettings);
        assertNotNull(appSettings.getDbSettings());
        assertNotNull(appSettings.getSyncSettings());

        assertEquals("local.db",
                     appSettings.getDbSettings()
                                .getName());
        assertEquals(8,
                     appSettings.getDbSettings()
                                .getVersion());

        Assert.assertEquals("http://domain.com/sync/",
                            appSettings.getSyncSettings()
                                       .getServerUrl());
        Assert.assertEquals("my!token#",
                            appSettings.getSyncSettings()
                                       .getToken());
        Assert.assertEquals("status/",
                            appSettings.getSyncSettings()
                                       .getStatusUrl());
        Assert.assertEquals("import/",
                            appSettings.getSyncSettings()
                                       .getImportUrl());
        Assert.assertEquals(1,
                            appSettings.getSyncSettings()
                                       .getExports()
                                       .size());
        Assert.assertEquals("export/sqlite/",
                            appSettings.getSyncSettings()
                                       .getExports()
                                       .get(0)
                                       .getUrl());
        Assert.assertEquals("databases/data.db",
                            appSettings.getSyncSettings()
                                       .getExports()
                                       .get(0)
                                       .getFile());
    }

    @NonNull
    private Intent buildTestIntent(@NonNull final String action,
                                   @NonNull final String filename) {
        return buildIntent(RuntimeEnvironment.application,
                           DummyAppSettingsIntentService.class,
                           action,
                           DEFAULT_BROADCAST_ACTION,
                           filename);
    }

    @NonNull
    private DummyAppSettingsIntentService invokeService(@NonNull final Intent intent) {
        return invokeService(intent,
                             null);
    }

    @NonNull
    private DummyAppSettingsIntentService invokeService(@NonNull final Intent intent,
                                                        @Nullable final String filename) {
        final ServiceController<DummyAppSettingsIntentService> serviceController = buildService(DummyAppSettingsIntentService.class,
                                                                                                intent);
        final DummyAppSettingsIntentService appSettingsIntentService = spy(serviceController.attach()
                                                                                            .create()
                                                                                            .get());

        if (!TextUtils.isEmpty(filename)) {
            try {
                doReturn(new StringReader(TestHelper.getFixture(filename))).when(appSettingsIntentService)
                                                                           .getAppSettingsReader(eq(filename));
            }
            catch (IOException ignored) {

            }
        }

        appSettingsIntentService.onHandleIntent(intent);

        return appSettingsIntentService;
    }
}