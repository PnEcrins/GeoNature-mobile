package com.makina.ecrins.commons.ui.pager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link PagerJsonWriter}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class PagerJsonWriterTest {

    private PagerJsonWriter pagerJsonWriter;

    @Before
    public void setUp() throws
                        Exception {
        pagerJsonWriter = new PagerJsonWriter();
    }

    @Test
    public void testWriteEmptyPager() throws
                                      Exception {
        // given an empty pager metadata
        final Pager pager = new Pager(0L);

        // when write this pager as JSON string
        final StringWriter writer = new StringWriter();
        pagerJsonWriter.write(writer,
                              pager);

        // then
        assertNotNull(writer.toString());

        @SuppressWarnings("StringBufferReplaceableByString")
        final String expectedJsonString = new StringBuilder().append('{')
                                                             .append("\"id\":")
                                                             .append(0)
                                                             .append(",\"size\":")
                                                             .append(0)
                                                             .append(",\"position\":")
                                                             .append(0)
                                                             .append(",\"history\":[]")
                                                             .append('}')
                                                             .toString();

        assertEquals(expectedJsonString,
                     writer.toString());
    }

    @Test
    public void testWrite() throws
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

        // when write this pager as JSON string
        final StringWriter writer = new StringWriter();
        pagerJsonWriter.write(writer,
                              pager);

        // then
        assertNotNull(writer.toString());

        @SuppressWarnings("StringBufferReplaceableByString")
        final String expectedJsonString = new StringBuilder().append('{')
                                                             .append("\"id\":")
                                                             .append(1234L)
                                                             .append(",\"size\":")
                                                             .append(5)
                                                             .append(",\"position\":")
                                                             .append(3)
                                                             .append(",\"history\":[1,4,3,2]")
                                                             .append('}')
                                                             .toString();

        assertEquals(expectedJsonString,
                     writer.toString());
    }
}