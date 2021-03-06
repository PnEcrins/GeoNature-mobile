package com.makina.ecrins.commons.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * <code>BroadcastReceiver</code> implementation to check periodically the server status by sending
 * a message to the underlying {@link CheckServerService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class StartCheckServerServiceReceiver extends BroadcastReceiver {

    public static final String INTENT_MESSENGER = "messenger";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getName(), "onReceive");

        Bundle extras = intent.getExtras();

        // gets messenger
        if (extras != null) {
            Messenger messenger = (Messenger) extras.get(INTENT_MESSENGER);

            try {
                Message message = Message.obtain();
                message.what = CheckServerService.HANDLER_SYNC_CHECK_SERVER_STATUS;
                messenger.send(message);
            }
            catch (RemoteException re) {
                Log.w(getClass().getName(), re.getMessage(), re);
            }
        }
    }
}
