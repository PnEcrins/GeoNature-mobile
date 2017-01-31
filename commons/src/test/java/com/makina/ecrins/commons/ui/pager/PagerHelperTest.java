package com.makina.ecrins.commons.ui.pager;

import android.annotation.SuppressLint;
import android.support.v7.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link PagerHelper}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class PagerHelperTest {

    private PagerHelper pagerHelper;

    @Before
    public void setUp() throws
                        Exception {
        pagerHelper = new PagerHelper(RuntimeEnvironment.application);
    }

    @Test
    public void testLoadNullPager() throws
                                    Exception {
        // given a not found pager
        long pagerId = 1234L;

        // when trying to load this pager
        final Pager pager = pagerHelper.load(pagerId);

        // then
        assertNotNull(pager);
        assertEquals(pagerId,
                     pager.getId());
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void testLoadExistingPager() throws
                                        Exception {
        // given an existing pager
        @SuppressWarnings("StringBufferReplaceableByString")
        final String jsonString = new StringBuilder().append('{')
                                                     .append("\"id\":")
                                                     .append(1234L)
                                                     .append(",\"size\":")
                                                     .append(5)
                                                     .append(",\"position\":")
                                                     .append(3)
                                                     .append(",\"history\":[1,4,3,2]")
                                                     .append('}')
                                                     .toString();

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                         .edit()
                         .putString(pagerHelper.getPagerPreferenceKey(1234L),
                                    jsonString)
                         .commit();

        // when trying to load this pager
        final Pager pager = pagerHelper.load(1234L);

        // then
        assertNotNull(pager);
        assertEquals(1234L,
                     pager.getId());
        assertEquals(5,
                     pager.getSize());
        assertEquals(3,
                     pager.getPosition());
        assertEquals(4,
                     pager.getHistory()
                          .size());
        assertEquals(Integer.valueOf(2),
                     pager.getHistory()
                          .pollLast());
        assertEquals(Integer.valueOf(3),
                     pager.getHistory()
                          .pollLast());
        assertEquals(Integer.valueOf(4),
                     pager.getHistory()
                          .pollLast());
        assertEquals(Integer.valueOf(1),
                     pager.getHistory()
                          .pollLast());
    }

    @Test
    public void testSave() throws
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

        // when trying to save this pager
        pagerHelper.save(pager);

        // then
        assertEquals(pager,
                     pagerHelper.load(pager.getId()));
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void testDelete() throws
                             Exception {
        // given an existing pager
        @SuppressWarnings("StringBufferReplaceableByString")
        final String jsonString = new StringBuilder().append('{')
                                                     .append("\"id\":")
                                                     .append(1234L)
                                                     .append(",\"size\":")
                                                     .append(5)
                                                     .append(",\"position\":")
                                                     .append(3)
                                                     .append(",\"history\":[1,4,3,2]")
                                                     .append('}')
                                                     .toString();

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                         .edit()
                         .putString(pagerHelper.getPagerPreferenceKey(1234L),
                                    jsonString)
                         .commit();

        // when trying to delete this pager
        pagerHelper.delete(1234L);

        // then
        final Pager pagerLoader = pagerHelper.load(1234L);
        assertEquals(0,
                     pagerLoader.getSize());
        assertEquals(0,
                     pagerLoader.getPosition());
        assertTrue(pagerLoader.getHistory()
                              .isEmpty());
        assertNull(PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                                    .getString(pagerHelper.getPagerPreferenceKey(1234L),
                                               null));

    }
}