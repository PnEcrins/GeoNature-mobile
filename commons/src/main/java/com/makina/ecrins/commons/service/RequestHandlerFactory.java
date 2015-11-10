package com.makina.ecrins.commons.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * {@code Factory} used to create {@link com.makina.ecrins.commons.service.AbstractRequestHandler} instance
 * from a given {@code Message}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see com.makina.ecrins.commons.service.AbstractRequestHandler
 */
public class RequestHandlerFactory {

    private static final String TAG = RequestHandlerFactory.class.getName();

    private RequestHandlerFactory() {

    }

    /**
     * Returns a single instance of {@link com.makina.ecrins.commons.service.RequestHandlerFactory}.
     *
     * @return instance of {@link RequestHandlerFactory}
     */
    public static RequestHandlerFactory getInstance() {

        return RequestHandlerFactoryHolder.sInstance;
    }

    /**
     * Tries to find the right implementation of {@link com.makina.ecrins.commons.service.AbstractRequestHandler}
     * from a given {@code Message} received from {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     *
     * @param context the current context
     * @param message the {@code Message} to parse
     *
     * @return the right implementation of {@link com.makina.ecrins.commons.service.AbstractRequestHandler}
     * according to the given {@code Message} or {@code null} if not found
     */
    @Nullable
    public AbstractRequestHandler getRequestHandler(
            @NonNull final Context context,
            @NonNull final Message message) {

        final Bundle data = message.peekData();

        if ((data == null) || (!data.containsKey(AbstractRequestHandler.KEY_HANDLER))) {
            Log.w(TAG,
                  "getRequestHandler: message '" + message + "' contains no data to parse");

            return null;
        }

        final Class<?> requestHandlerClass = (Class<?>) data.getSerializable(AbstractRequestHandler.KEY_HANDLER);

        if (AbstractRequestHandler.class.isAssignableFrom(requestHandlerClass)) {
            final Class<? extends AbstractRequestHandler> abstractRequestHandler = requestHandlerClass.asSubclass(AbstractRequestHandler.class);

            try {
                final Constructor<? extends AbstractRequestHandler> constructorRequestHandlerClass = abstractRequestHandler.getConstructor(Context.class);

                return constructorRequestHandlerClass.newInstance(context);
            }
            catch (Exception ge) {
                Log.w(TAG,
                      "getRequestHandler: no RequestHandler implementation found for '" + requestHandlerClass.getName() + "'",
                      ge);
            }
        }

        Log.w(TAG,
              "getRequestHandler: no RequestHandler implementation found for '" + requestHandlerClass.getName() + "'");

        return null;
    }

    /**
     * Tries to find from the given {@code List} the right implementation of {@link com.makina.ecrins.commons.service.AbstractRequestHandler}
     * from a given {@code Message} received from {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     *
     * @param message         the {@code Message} to parse
     * @param requestHandlers a {@code List} of {@link com.makina.ecrins.commons.service.AbstractRequestHandler} on which to iterate
     *
     * @return the right implementation of {@link com.makina.ecrins.commons.service.AbstractRequestHandler}
     * according to the given {@code Message} or {@code null} if not found
     */
    @Nullable
    public AbstractRequestHandler getRequestHandler(
            @NonNull final Message message,
            @NonNull final List<AbstractRequestHandler> requestHandlers) {

        final Bundle data = message.peekData();

        if ((data == null) || (!data.containsKey(AbstractRequestHandler.KEY_HANDLER))) {
            Log.w(TAG,
                  "getRequestHandler: message '" + message + "' contains no data to parse");

            return null;
        }

        final Class<?> requestHandlerClass = (Class<?>) data.getSerializable(AbstractRequestHandler.KEY_HANDLER);

        for (AbstractRequestHandler requestHandler : requestHandlers) {
            if (requestHandlerClass.isInstance(requestHandler)) {
                return requestHandler;
            }
        }

        return null;
    }

    private static class RequestHandlerFactoryHolder {

        private final static RequestHandlerFactory sInstance = new RequestHandlerFactory();
    }
}
