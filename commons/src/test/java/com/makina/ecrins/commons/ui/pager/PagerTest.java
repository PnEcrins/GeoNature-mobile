package com.makina.ecrins.commons.ui.pager;

import android.os.Parcel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit test for {@link Pager}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class PagerTest {

    @Test
    public void testParcelable() throws
                                 Exception {
        // given a pager metadata
        final Pager pager = new Pager(1234L);
        pager.setSize(5);
        pager.setPosition(3);
        pager.getHistory()
             .add(1);
        pager.getHistory()
             .add(4);
        pager.getHistory()
             .add(3);
        pager.getHistory()
             .add(2);

        // when we obtain a Parcel object to write the Pager instance to it
        final Parcel parcel = Parcel.obtain();
        pager.writeToParcel(parcel,
                            0);

        // reset the parcel for reading
        parcel.setDataPosition(0);

        // then
        Assert.assertEquals(pager,
                            Pager.CREATOR.createFromParcel(parcel));
    }
}