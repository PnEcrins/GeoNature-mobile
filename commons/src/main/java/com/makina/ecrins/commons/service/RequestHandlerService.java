package com.makina.ecrins.commons.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code Service} implementation to perform in background all asynchronous tasks.
 * This service should be use in conjunction with its {@link RequestHandlerServiceClient}
 * to execute these tasks.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("Registered")
public class RequestHandlerService
        extends Service {

    private static final String TAG = RequestHandlerService.class.getSimpleName();

    /**
     * Delay (in second) to automatically stop the service.
     */
    private static final int SERVICE_SHUTDOWN_DELAY = 20 * 1000;

    /**
     * keeps track of all current registered clients.
     */
    private final Map<String, Messenger> mClients = new HashMap<>();

    private final Map<String, List<AbstractRequestHandler>> mRequestHandlers = new HashMap<>();

    private final Handler mHandler = new Handler();

    private final StopSelfRunnable mStopSelfRunnable = new StopSelfRunnable();
    private final AtomicBoolean mStopSelfRunnableStarted = new AtomicBoolean();

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mInMessenger = new Messenger(new IncomingHandler(this));

    /**
     * The callbacks used by all {@link com.makina.ecrins.commons.service.AbstractRequestHandler} instances.
     */
    protected final AbstractRequestHandler.RequestHandlerServiceListener mRequestHandlerServiceListener = new AbstractRequestHandler.RequestHandlerServiceListener() {
        @Override
        public void addClient(
                @NonNull String token,
                Messenger messenger) {

            mClients.put(token,
                         messenger);

            // initialize an empty list of AbstractRequestHandler used for this token
            if (mRequestHandlers.get(token) == null) {
                mRequestHandlers.put(token,
                                     new ArrayList<AbstractRequestHandler>());
            }
        }

        @Override
        public void removeClient(@NonNull String token) {

            mClients.remove(token);

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "remaining clients: " + mClients.size());
            }

            shutdownDelayedIfNoClient();
        }

        @Override
        public void sendMessage(
                String token,
                @NonNull Bundle data) {

            RequestHandlerService.this.sendMessage(token,
                                                   data);
        }
    };

    @Override
    public void onCreate() {

        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onCreate");
        }

        shutdownDelayedIfNoClient();
    }

    @Override
    public IBinder onBind(Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onBind " + intent);
        }

        return mInMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onUnbind " + intent);
        }

        return true;
    }

    @Override
    public void onDestroy() {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onDestroy");
        }

        mStopSelfRunnableStarted.set(false);

        super.onDestroy();
    }

    /**
     * Sends a message to this Messenger's Handler.
     *
     * @param token the token used to retrieve the corresponding {@code Messenger}
     * @param data  the data as {@code Bundle} to be sent to the receiver
     */
    void sendMessage(
            final String token,
            @NonNull final Bundle data) {

        if (TextUtils.isEmpty(token)) {
            Log.w(TAG,
                  "sendMessage: token is not defined. The Message will not be sent.");

            return;
        }

        final Messenger messenger = mClients.get(token);

        if (messenger == null) {
            Log.w(TAG,
                  "sendMessage: no client registered for token '" +
                          token +
                          "'");

            return;
        }

        try {
            final Message message = Message.obtain();
            message.setData(data);
            messenger.send(message);
        }
        catch (final RemoteException re) {
            // The client is dead. Remove it from the list.
            mClients.remove(token);

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "sendMessage: client was dead, removing it.",
                      re);
            }
        }
    }

    private void shutdownDelayedIfNoClient() {

        if (mClients.isEmpty() && !mStopSelfRunnableStarted.getAndSet(true)) {
            // try to stop itself in SERVICE_SHUTDOWN_DELAY
            mHandler.postDelayed(mStopSelfRunnable,
                                 SERVICE_SHUTDOWN_DELAY);
        }
    }

    /**
     * Handler of incoming messages from {@link RequestHandlerServiceClient}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class IncomingHandler
            extends Handler {

        private final WeakReference<RequestHandlerService> mRequestHandlerServiceWeakReference;

        public IncomingHandler(RequestHandlerService pRequestHandlerService) {

            super();

            mRequestHandlerServiceWeakReference = new WeakReference<>(pRequestHandlerService);
        }

        @Override
        public void handleMessage(Message msg) {

            final RequestHandlerService requestHandlerService = mRequestHandlerServiceWeakReference.get();

            if (requestHandlerService == null) {
                Log.w(TAG,
                      "Service is dead. Ignoring incoming message from clients.");

                return;
            }

            final Bundle data = msg.peekData();

            if (data == null || (!data.containsKey(AbstractRequestHandler.KEY_HANDLER))) {
                Log.w(TAG,
                      "handleMessage: invalid Message " + msg);

                return;
            }

            AbstractRequestHandler requestHandler = null;

            // try to find an existing instance of the right AbstractRequestHandler for this Message
            final List<AbstractRequestHandler> requestHandlersForToken = requestHandlerService.mRequestHandlers.get(data.getString(AbstractRequestHandler.KEY_CLIENT_TOKEN));

            // should not be null
            if (requestHandlersForToken != null) {
                requestHandler = RequestHandlerFactory.getInstance()
                                                      .getRequestHandler(msg,
                                                                         requestHandlersForToken);
            }

            // try to instantiate the right AbstractRequestHandler for this Message
            if (requestHandler == null) {
                requestHandler = RequestHandlerFactory.getInstance()
                                                      .getRequestHandler(requestHandlerService,
                                                                         msg);
            }

            if (requestHandler != null) {

                // should not be null
                if (requestHandlerService.mRequestHandlers.get(data.getString(AbstractRequestHandler.KEY_CLIENT_TOKEN)) != null) {
                    // add this AbstractRequestHandler instance to this token
                    requestHandlerService.mRequestHandlers.get(data.getString(AbstractRequestHandler.KEY_CLIENT_TOKEN))
                                                          .add(requestHandler);
                }

                requestHandler.setRequestHandlerServiceListener(requestHandlerService.mRequestHandlerServiceListener);
                requestHandler.handleMessageFromService(msg);
            }
        }
    }

    /**
     * {@code Runnable} implementation used to stop automatically this
     * {@link com.makina.ecrins.commons.service.RequestHandlerService} instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private final class StopSelfRunnable
            implements Runnable {

        @Override
        public void run() {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "StopSelfRunnable is running");
            }

            if (mClients.isEmpty()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "StopSelfRunnable finished");
                }

                stopSelf();
            }
            else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "StopSelfRunnable aborted");
                }

                mStopSelfRunnableStarted.set(false);
            }
        }
    }
}
