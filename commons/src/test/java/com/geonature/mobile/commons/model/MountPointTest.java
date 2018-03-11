package com.geonature.mobile.commons.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests about {@link MountPoint}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class MountPointTest {

    @Test
    public void testCompareTo() throws
                                Exception {
        // given two MountPoint of same type
        final MountPoint mountPoint1 = new MountPoint("/storage1",
                                                      MountPoint.StorageType.INTERNAL);
        final MountPoint mountPoint2 = new MountPoint("/storage2",
                                                      MountPoint.StorageType.INTERNAL);

        // then
        assertTrue(mountPoint1.compareTo(mountPoint2) < 0);
        assertTrue(mountPoint2.compareTo(mountPoint1) > 0);

        // given an identical MountPoint of same type
        final MountPoint mountPoint2a = new MountPoint("/storage2",
                                                       MountPoint.StorageType.INTERNAL);

        assertTrue(mountPoint2.compareTo(mountPoint2a) == 0);

        // given another MountPoint of different type
        final MountPoint mountPoint3 = new MountPoint("/mnt/sdcard1",
                                                      MountPoint.StorageType.EXTERNAL);

        // then
        assertTrue(mountPoint1.compareTo(mountPoint3) < 0);
        assertTrue(mountPoint3.compareTo(mountPoint1) > 0);

        // given another MountPoint of different type
        final MountPoint mountPoint4 = new MountPoint("/another",
                                                      MountPoint.StorageType.USB);

        // then
        assertTrue(mountPoint1.compareTo(mountPoint4) < 0);
        assertTrue(mountPoint4.compareTo(mountPoint1) > 0);
        assertTrue(mountPoint3.compareTo(mountPoint4) < 0);
        assertTrue(mountPoint4.compareTo(mountPoint3) > 0);
    }
}