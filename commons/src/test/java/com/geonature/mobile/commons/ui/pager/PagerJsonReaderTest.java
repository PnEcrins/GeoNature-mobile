package com.geonature.mobile.commons.ui.pager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link PagerJsonReader}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class PagerJsonReaderTest {

    private PagerJsonReader pagerJsonReader;

    @Before
    public void setUp() throws
                        Exception {
        pagerJsonReader = new PagerJsonReader();
    }

    @Test
    public void testReadEmptyPager() throws
                                     Exception {
        // given a JSON string
        @SuppressWarnings("StringBufferReplaceableByString")
        final String jsonString = new StringBuilder().append('{')
                                                     .append("\"id\":")
                                                     .append(0)
                                                     .append(",\"size\":")
                                                     .append(0)
                                                     .append(",\"position\":")
                                                     .append(0)
                                                     .append(",\"history\":[]")
                                                     .append('}')
                                                     .toString();

        // when read this JSON string
        final StringReader reader = new StringReader(jsonString);
        final Pager pager = pagerJsonReader.read(reader);

        // then
        assertNotNull(pager);
        assertEquals(0,
                     pager.getId());
        assertEquals(0,
                     pager.getSize());
        assertEquals(0,
                     pager.getPosition());
        assertTrue(pager.getHistory()
                        .isEmpty());
    }

    @Test
    public void testRead() throws
                           Exception {
        // given a JSON string
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

        // when read this JSON string
        final StringReader reader = new StringReader(jsonString);
        final Pager pager = pagerJsonReader.read(reader);

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
}