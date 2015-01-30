package com.makina.ecrins.commons.service;

import android.os.Message;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit tests for {@link com.makina.ecrins.commons.service.RequestHandlerFactory} class.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class RequestHandlerFactoryTest {

    @Test
    public void testGetRequestHandler() {
        final Message message = Message.obtain();

        Assert.assertNull(
                RequestHandlerFactory.getInstance()
                        .getRequestHandler(
                                Robolectric.application,
                                message
                        )
        );

        message.getData()
                .putSerializable(
                        AbstractRequestHandler.KEY_HANDLER,
                        ConnectClientRequestHandler.class
                );

        final AbstractRequestHandler connectClientRequestHandler = RequestHandlerFactory.getInstance()
                .getRequestHandler(
                        Robolectric.application,
                        message
                );

        Assert.assertNotNull(connectClientRequestHandler);
        Assert.assertTrue(connectClientRequestHandler instanceof ConnectClientRequestHandler);

        message.getData()
                .putSerializable(
                        AbstractRequestHandler.KEY_HANDLER,
                        DisconnectClientRequestHandler.class
                );

        final AbstractRequestHandler disconnectClientRequestHandler = RequestHandlerFactory.getInstance()
                .getRequestHandler(
                        Robolectric.application,
                        message
                );

        Assert.assertNotNull(disconnectClientRequestHandler);
        Assert.assertTrue(disconnectClientRequestHandler instanceof DisconnectClientRequestHandler);
    }
}
