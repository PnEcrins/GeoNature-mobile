package com.makina.ecrins.commons.util;

import android.os.Build;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.model.MountPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.makina.ecrins.commons.model.MountPoint.StorageType.INTERNAL;
import static com.makina.ecrins.commons.util.MountPointUtils.formatStorageSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * Unit tests about {@link MountPointUtils}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.JELLY_BEAN)
public class MountPointUtilsTest {

    @Test
    public void testGetInternalStorage() {
        final MountPoint mountPoint = MountPointUtils.getInternalStorage();

        assertNotNull(mountPoint);
        assertEquals(INTERNAL,
                     mountPoint.getStorageType());
    }

    @Test
    public void testFormatStorageSize() {
        long storageInB = 128L;
        final String storageInBFormatted = formatStorageSize(application,
                                                             storageInB);
        assertNotNull(storageInB);
        assertEquals("128 B",
                     storageInBFormatted);

        long storageInKb = 1024L;
        final String storageInKbFormatted = formatStorageSize(application,
                                                              storageInKb);

        assertNotNull(storageInKb);
        assertEquals("1.0 kB",
                     storageInKbFormatted);

        long storageInMb = storageInKb * 1024;
        final String storageInMbFormatted = formatStorageSize(application,
                                                              storageInMb);

        assertNotNull(storageInMb);
        assertEquals("1.0 MB",
                     storageInMbFormatted);

        long storageInGb = storageInMb * 1024;
        final String storageInGbFormatted = formatStorageSize(application,
                                                              storageInGb);

        assertNotNull(storageInGb);
        assertEquals("1.0 GB",
                     storageInGbFormatted);
    }
}