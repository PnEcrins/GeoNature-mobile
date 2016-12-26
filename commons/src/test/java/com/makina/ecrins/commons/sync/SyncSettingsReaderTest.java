package com.makina.ecrins.commons.sync;

import com.makina.ecrins.commons.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link SyncSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class SyncSettingsReaderTest {

    private SyncSettingsReader syncSettingsReader;

    @Before
    public void setUp() throws
                        Exception {
        syncSettingsReader = new SyncSettingsReader();
    }

    @Test
    public void testReadSyncSettingsFromJsonString() throws
                                                     Exception {
        // given a JSON settings
        final String json = TestHelper.getFixture("sync_settings.json");

        // when read the JSON as MapSettings
        final SyncSettings syncSettings = syncSettingsReader.readSyncSettings(json);

        // then
        assertNotNull(syncSettings);
        assertEquals("http://domain.com/sync/",
                     syncSettings.getServerUrl());
        assertEquals("my!token#",
                     syncSettings.getToken());
        assertEquals("status/",
                     syncSettings.getStatusUrl());
        assertEquals("import/",
                     syncSettings.getImportUrl());
        assertEquals(1,
                     syncSettings.getExports()
                                 .size());
        assertEquals("export/sqlite/",
                     syncSettings.getExports()
                                 .get(0)
                                 .getUrl());
        assertEquals("databases/data.db",
                     syncSettings.getExports()
                                 .get(0)
                                 .getFile());
    }

    @Test
    public void testReadSyncSettingsFromInvalidJsonString() throws
                                                            Exception {
        // when read an invalid JSON as MapSettings
        final SyncSettings syncSettings = syncSettingsReader.readSyncSettings("");

        // then
        assertNull(syncSettings);
    }
}