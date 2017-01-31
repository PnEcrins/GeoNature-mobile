package com.makina.ecrins.commons.service;

import android.os.Message;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

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
                                RuntimeEnvironment.application,
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
                        RuntimeEnvironment.application,
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
                        RuntimeEnvironment.application,
                        message
                );

        Assert.assertNotNull(disconnectClientRequestHandler);
        Assert.assertTrue(disconnectClientRequestHandler instanceof DisconnectClientRequestHandler);
    }

    @Test
    public void testGetRequestHandlerFromList() {
        final Message message = Message.obtain();
        final List<AbstractRequestHandler> requestHandlers = new ArrayList<>();

        Assert.assertNull(
                RequestHandlerFactory.getInstance()
                        .getRequestHandler(
                                message,
                                requestHandlers
                        )
        );

        message.getData()
                .putSerializable(
                        AbstractRequestHandler.KEY_HANDLER,
                        ConnectClientRequestHandler.class
                );

        Assert.assertNull(
                RequestHandlerFactory.getInstance()
                        .getRequestHandler(
                                message,
                                requestHandlers
                        )
        );

        requestHandlers.add(new ConnectClientRequestHandler(RuntimeEnvironment.application));

        final AbstractRequestHandler connectClientRequestHandler = RequestHandlerFactory.getInstance()
                .getRequestHandler(
                        message,
                        requestHandlers
                );

        Assert.assertNotNull(connectClientRequestHandler);
        Assert.assertTrue(connectClientRequestHandler instanceof ConnectClientRequestHandler);
    }
}
