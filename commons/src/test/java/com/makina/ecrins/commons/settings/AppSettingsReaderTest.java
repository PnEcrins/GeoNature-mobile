package com.makina.ecrins.commons.settings;

import android.util.JsonReader;

import com.makina.ecrins.commons.TestHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link AppSettingsReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class AppSettingsReaderTest {

    private AppSettingsReader appSettingsReader;

    @Mock
    private AppSettingsReader.OnAppSettingsReaderListener onAppSettingsReaderListener;

    @Before
    public void setUp() throws
                        Exception {
        MockitoAnnotations.initMocks(this);

        doReturn(new DummyAppSettings()).when(onAppSettingsReaderListener)
                                        .createAppSettings();

        appSettingsReader = spy(new AppSettingsReader(onAppSettingsReaderListener));
    }

    @Test
    public void testReadAppSettingsWithDbFromJsonString() throws
                                                          Exception {
        // given a JSON settings
        final String json = TestHelper.getFixture("app_settings_with_db.json");

        // when read the JSON as DummyAppSettings
        final DummyAppSettings appSettings = (DummyAppSettings) appSettingsReader.read(json);

        verify(onAppSettingsReaderListener,
               never()).readAdditionalSettings(any(JsonReader.class),
                                               any(String.class),
                                               any(DummyAppSettings.class));

        // then
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

    @Test
    public void testReadAppSettingsWithoutQualificationFromJsonString() throws
                                                                        Exception {
        // given a JSON settings
        final String json = TestHelper.getFixture("app_settings.json");

        // when read the JSON as DummyAppSettings
        final DummyAppSettings appSettings = (DummyAppSettings) appSettingsReader.read(json);

        // then
        assertNotNull(appSettings);
        assertNull(appSettings.getQualificationSettings());
    }

    @Test
    public void testReadAppSettingsWithQualificationFromJsonString() throws
                                                                     Exception {
        // given a JSON settings
        final String json = TestHelper.getFixture("app_settings_with_qualification.json");

        // when read the JSON as DummyAppSettings
        final DummyAppSettings appSettings = (DummyAppSettings) appSettingsReader.read(json);

        // then
        assertNotNull(appSettings);

        final QualificationSettings qualificationSettings = appSettings.getQualificationSettings();
        assertNotNull(qualificationSettings);
        assertEquals(2,
                     qualificationSettings.getOrganism());
        assertEquals(140,
                     qualificationSettings.getProtocol());
        assertEquals(4,
                     qualificationSettings.getLot());
    }

    @Test
    public void testReadAppSettingsFromJsonString() throws
                                                    Exception {
        // given a JSON settings
        final String json = TestHelper.getFixture("app_settings.json");

        // when read the JSON as DummyAppSettings
        final DummyAppSettings appSettings = (DummyAppSettings) appSettingsReader.read(json);

        verify(onAppSettingsReaderListener,
               never()).readAdditionalSettings(any(JsonReader.class),
                                               any(String.class),
                                               any(DummyAppSettings.class));

        // then
        assertNotNull(appSettings);
        assertNotNull(appSettings.getDbSettings());
        assertNotNull(appSettings.getSyncSettings());

        assertEquals("data.db",
                     appSettings.getDbSettings()
                                .getName());
        assertEquals(1,
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

    @Test
    public void testReadAppSettingsFromInvalidJsonString() throws
                                                           Exception {
        // when read an invalid JSON as DummyAppSettings
        final DummyAppSettings appSettings = (DummyAppSettings) appSettingsReader.read("");

        // then
        assertNull(appSettings);
    }
}