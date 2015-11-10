package com.makina.ecrins.commons.service;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

/**
 * Default {@link com.makina.ecrins.commons.service.AbstractRequestHandler} implementation used to
 * unregister {@link RequestHandlerServiceClient} to
 * {@link com.makina.ecrins.commons.service.RequestHandlerService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DisconnectClientRequestHandler
        extends AbstractRequestHandler {

    private static final String TAG = DisconnectClientRequestHandler.class.getSimpleName();

    public static final String KEY_CLIENT_DISCONNECTED = "KEY_CLIENT_DISCONNECTED";

    public DisconnectClientRequestHandler(Context pContext) {

        super(pContext);
    }

    @Override
    protected void handleMessageFromService(Message message) {

        if (mRequestHandlerServiceListener == null) {
            Log.w(TAG,
                  "RequestHandlerServiceListener is not defined!");

            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "handleMessage");
        }

        if (checkMessage(message)) {
            message.getData()
                   .putBoolean(KEY_CLIENT_DISCONNECTED,
                               true);

            sendMessage(message.getData());
            mRequestHandlerServiceListener.removeClient(message.getData()
                                                               .getString(KEY_CLIENT_TOKEN));
        }
    }
}
