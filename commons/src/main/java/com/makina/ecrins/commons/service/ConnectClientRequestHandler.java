package com.makina.ecrins.commons.service;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

import java.util.UUID;

/**
 * Default {@link com.makina.ecrins.commons.service.AbstractRequestHandler} implementation used to
 * register {@link RequestHandlerServiceClient} to
 * {@link com.makina.ecrins.commons.service.RequestHandlerService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ConnectClientRequestHandler
        extends AbstractRequestHandler {

    private static final String TAG = ConnectClientRequestHandler.class.getSimpleName();

    public static final String KEY_CLIENT_CONNECTED = "KEY_CLIENT_CONNECTED";

    public ConnectClientRequestHandler(Context pContext) {

        super(pContext);
    }

    @Override
    protected void handleMessageFromService(Message message) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "handleMessage");
        }

        if (mRequestHandlerServiceListener == null) {
            Log.w(TAG,
                  "RequestHandlerServiceListener is not defined!");

            return;
        }

        if (TextUtils.isEmpty(message.getData()
                                     .getString(KEY_CLIENT_TOKEN))) {
            message.getData()
                   .putString(KEY_CLIENT_TOKEN,
                              UUID.randomUUID()
                                  .toString());
        }

        if (checkMessage(message)) {
            message.getData()
                   .putBoolean(KEY_CLIENT_CONNECTED,
                               true);

            mRequestHandlerServiceListener.addClient(message.getData()
                                                            .getString(KEY_CLIENT_TOKEN),
                                                     message.replyTo);
            sendMessage(message.getData());
        }
    }
}
