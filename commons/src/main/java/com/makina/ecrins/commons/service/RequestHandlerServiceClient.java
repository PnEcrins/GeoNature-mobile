package com.makina.ecrins.commons.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base client implementation to use {@link com.makina.ecrins.commons.service.RequestHandlerService}
 * service.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class RequestHandlerServiceClient {

    private static final String TAG = RequestHandlerServiceClient.class.getSimpleName();

    protected final Context mContext;
    protected ServiceClientListener mServiceClientListener = null;

    protected final AtomicBoolean mConnected = new AtomicBoolean();
    protected String mToken;

    /**
     * Messenger for communicating with {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     */
    private Messenger mRequestHandlerServiceMessenger = null;

    /**
     * Target we publish for clients to send messages to
     * {@link RequestHandlerServiceClient.IncomingHandler}.
     */
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private final AtomicBoolean mIsRequestHandlerServiceBound = new AtomicBoolean();

    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "onServiceConnected '" + name + "'"
                );
            }

            if ((name != null) && name.getClassName().equals(RequestHandlerService.class.getName())) {
                // This is called when the connection with the service has been established,
                // giving us the service object we can use to interact with the service.
                mRequestHandlerServiceMessenger = new Messenger(service);

                // We want to monitor the service for as long as we are connected to it.
                try {
                    final Message message = Message.obtain();
                    message.getData()
                            .putSerializable(
                                    AbstractRequestHandler.KEY_HANDLER,
                                    ConnectClientRequestHandler.class
                            );
                    message.getData()
                            .putString(
                                    AbstractRequestHandler.KEY_CLIENT_TOKEN,
                                    mToken
                            );

                    // this is needed to register the Messenger with its token to RequestHandlerService
                    message.replyTo = mMessenger;

                    mRequestHandlerServiceMessenger.send(message);
                }
                catch (RemoteException re) {
                    // In this case the service has crashed before we could even do anything with it.
                    // We can count on soon being disconnected (and then reconnected if it can be
                    // restarted) so there is no need to do anything here.
                    Log.w(
                            TAG,
                            re.getMessage(),
                            re
                    );
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "onServiceDisconnected " + name
                );
            }

            if ((name != null) && name.getClassName().equals(RequestHandlerService.class.getName())) {
                mRequestHandlerServiceMessenger = null;
            }
        }
    };

    public RequestHandlerServiceClient(Context pContext) {
        this.mContext = pContext;
    }

    public void setServiceClientListener(ServiceClientListener pServiceClientListener) {
        this.mServiceClientListener = pServiceClientListener;
    }

    /**
     * Connects this {@link RequestHandlerServiceClient}
     * instance to {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     */
    public void connect() {
        connect(null);
    }

    /**
     * Connects this {@link RequestHandlerServiceClient}
     * instance to {@link com.makina.ecrins.commons.service.RequestHandlerService} using an existing token.
     *
     * @param token the token to use to connect if any
     */
    public void connect(@Nullable String token) {
        if ((!mIsRequestHandlerServiceBound.get()) && (!mConnected.get())) {
            mToken = token;
            doBindService();
        }
    }
    /**
     * Closes the connection to {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     */
    public void disconnect() {
        doUnbindService();
    }

    /**
     * Checks if this {@link RequestHandlerServiceClient}
     * instance is currently connected to {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     *
     * @return {@code true} if the client is connected to the service.
     */
    public boolean isConnected() {
        return mConnected.get() && (!TextUtils.isEmpty(mToken));
    }

    /**
     * Send a message data to {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     *
     * @param requestHandlerClass the {@link com.makina.ecrins.commons.service.AbstractRequestHandler} to invoke
     * @param data the message to send to the server through {@link com.makina.ecrins.commons.service.RequestHandlerService}
     */
    public void send(
            @NonNull Class<? extends AbstractRequestHandler> requestHandlerClass,
            @NonNull Bundle data) {
        if ((mRequestHandlerServiceMessenger != null) && isConnected()) {
            data.putSerializable(
                    AbstractRequestHandler.KEY_HANDLER,
                    requestHandlerClass
            );
            data.putString(
                    AbstractRequestHandler.KEY_CLIENT_TOKEN,
                    mToken
            );

            final Message message = Message.obtain();
            message.setData(data);

            try {
                mRequestHandlerServiceMessenger.send(message);
            }
            catch (RemoteException re) {
                Log.w(
                        TAG,
                        re.getMessage(),
                        re
                );
            }
        }
        else {
            Log.w(
                    TAG,
                    "send: client not registered !"
            );
        }
    }

    /**
     * Establishes a connection with {@link com.makina.ecrins.commons.service.RequestHandlerService}
     * service.
     */
    private void doBindService() {
        if (BuildConfig.DEBUG) {
            Log.d(
                    TAG,
                    "try to connect to RequestHandlerService ..."
            );
        }

        // keep running RequestHandlerService, regardless of how much clients are connected to it
        mContext.startService(
                new Intent(
                        mContext,
                        RequestHandlerService.class
                )
        );
        
        mIsRequestHandlerServiceBound.set(
                this.mContext.bindService(
                        new Intent(
                                this.mContext,
                                RequestHandlerService.class
                        ),
                        mServiceConnection,
                        Context.BIND_AUTO_CREATE
                )
        );
    }

    private void doUnbindService() {
        if (mIsRequestHandlerServiceBound.get()) {

            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "try to disconnect from AbstractService ..."
                );
            }

            // if we have received the service, and hence registered with it,
            // then now is the time to unregister.
            if (mRequestHandlerServiceMessenger != null) {
                try {
                    final Message message = Message.obtain();
                    message.getData()
                            .putSerializable(
                                    AbstractRequestHandler.KEY_HANDLER,
                                    DisconnectClientRequestHandler.class
                            );
                    message.getData()
                            .putString(
                                    AbstractRequestHandler.KEY_CLIENT_TOKEN,
                                    mToken
                            );

                    mRequestHandlerServiceMessenger.send(message);
                }
                catch (RemoteException re) {
                    // there is nothing special we need to do if the service has crashed.
                    Log.w(
                            TAG,
                            re.getMessage(),
                            re
                    );
                }
            }

            // detach our existing connection.
            this.mContext.unbindService(mServiceConnection);
            mIsRequestHandlerServiceBound.set(false);
            this.mConnected.set(false);
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "already disconnected from RequestHandlerService"
                );
            }
        }
    }

    /**
     * Handler of incoming messages from {@link com.makina.ecrins.commons.service.RequestHandlerService}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class IncomingHandler
            extends Handler {

        private final WeakReference<RequestHandlerServiceClient> mAbstractRequestHandlerServiceClient;

        public IncomingHandler(RequestHandlerServiceClient pAbstractServiceClient) {
            super();

            mAbstractRequestHandlerServiceClient = new WeakReference<>(pAbstractServiceClient);
        }

        @Override
        public void handleMessage(Message msg) {
            final RequestHandlerServiceClient requestHandlerServiceClient = mAbstractRequestHandlerServiceClient.get();

            if (requestHandlerServiceClient == null) {
                Log.w(
                        TAG,
                        "Service client is dead. Ignoring incoming message from the service."
                );

                return;
            }

            final AbstractRequestHandler abstractRequestHandler = RequestHandlerFactory.getInstance()
                    .getRequestHandler(
                            requestHandlerServiceClient.mContext,
                            msg
                    );

            if (abstractRequestHandler == null) {
                Log.w(
                        TAG,
                        "No AbstractRequestHandler found for Message '" + msg + "'"
                );

                return;
            }

            if (abstractRequestHandler instanceof ConnectClientRequestHandler)
                abstractRequestHandler.handleMessageFromClient(
                        msg,
                        new AbstractRequestHandler.RequestHandlerClientListener() {
                            @Override
                            public void onHandleMessage(
                                    @NonNull Class<? extends AbstractRequestHandler> requestHandlerClass,
                                    @NonNull Bundle data) {
                                requestHandlerServiceClient.mConnected.set(true);
                                requestHandlerServiceClient.mToken = data.getString(AbstractRequestHandler.KEY_CLIENT_TOKEN);

                                if ((requestHandlerServiceClient.mServiceClientListener != null) && data.getBoolean(ConnectClientRequestHandler.KEY_CLIENT_CONNECTED)) {
                                    requestHandlerServiceClient.mServiceClientListener.onConnected(requestHandlerServiceClient.mToken);
                                }
                            }
                        }
                );
            else if (abstractRequestHandler instanceof DisconnectClientRequestHandler) {
                abstractRequestHandler.handleMessageFromClient(
                        msg,
                        new AbstractRequestHandler.RequestHandlerClientListener() {
                            @Override
                            public void onHandleMessage(
                                    @NonNull Class<? extends AbstractRequestHandler> requestHandlerClass,
                                    @NonNull Bundle data) {
                                 if ((requestHandlerServiceClient.mServiceClientListener != null) && data.getBoolean(DisconnectClientRequestHandler.KEY_CLIENT_DISCONNECTED)) {
                                    requestHandlerServiceClient.mServiceClientListener.onDisconnected();
                                }
                            }
                        }
                );
            }
            else {
                abstractRequestHandler.handleMessageFromClient(
                        msg,
                        new AbstractRequestHandler.RequestHandlerClientListener() {
                            @Override
                            public void onHandleMessage(
                                    @NonNull Class<? extends AbstractRequestHandler> requestHandlerClass,
                                    @NonNull Bundle data) {
                                if (requestHandlerServiceClient.mServiceClientListener != null) {
                                    requestHandlerServiceClient.mServiceClientListener.onHandleMessage(
                                            requestHandlerClass,
                                            data
                                    );
                                }
                            }
                        }
                );
            }
        }
    }

    /**
     * Provides callbacks that are called when the client is connected, disconnected or receive
     * messages from {@link com.makina.ecrins.commons.service.RequestHandlerService} service.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static interface ServiceClientListener {

        /**
         * After calling {@link com.makina.ecrins.commons.service.RequestHandlerServiceClient#connect()},
         * this method will be invoked asynchronously when the connect request has successfully completed.
         *
         * @param token the token to use to send {@code Message} through {@link com.makina.ecrins.commons.service.RequestHandlerServiceClient#send(Class, android.os.Bundle)}
         */
        void onConnected(@NonNull final String token);

        /**
         * Called when the client is disconnected from {@link com.makina.ecrins.commons.service.RequestHandlerService}.
         */
        void onDisconnected();

        /**
         * Called when the {@code Message} was received from {@link RequestHandlerServiceClient}.
         *
         * @param requestHandlerClass the {@link com.makina.ecrins.commons.service.AbstractRequestHandler}
         *                            which perform the received {@code Message}
         * @param data                the {@code Message} data
         */
        void onHandleMessage(
                @NonNull final Class<? extends AbstractRequestHandler> requestHandlerClass,
                @NonNull final Bundle data);
    }
}
