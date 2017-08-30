package com.makina.ecrins.maps.content;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit test for {@link Metadata}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class MetadataTest {

    @Test
    public void testDefaultMetadata() throws
                                      Exception {
        // given a default metadata
        final Metadata metadata = new Metadata("default");

        // then
        Assert.assertEquals("{" +
                            "\"name\":" + '\"' + metadata.getName() + '\"' +
                            ",\"type\":" + '\"' + metadata.getType() + '\"' +
                            ",\"version\":" + metadata.getVersion() +
                            ",\"description\":" + "null" + '\"' +
                            ",\"format\":" + '\"' + "png" + '\"' +
                            '}',
                            metadata.toString());
    }
}