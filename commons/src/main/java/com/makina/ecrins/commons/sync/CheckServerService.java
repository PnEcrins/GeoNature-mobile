package com.makina.ecrins.commons.sync;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

/**
 * {@code Service} implementation to check server status.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("Registered")
public class CheckServerService
        extends Service {

    private static final String TAG = CheckServerService.class.getName();

    public static final int HANDLER_SYNC_CHECK_SERVER_STATUS = 0;
    public static final int HANDLER_SYNC_SERVER_STATUS_PENDING = 1;
    public static final int HANDLER_SYNC_SERVER_STATUS_OK = 2;
    public static final int HANDLER_SYNC_SERVER_STATUS_KO = 3;

    public static final String INTENT_EXTRA_SYNC_SETTINGS = "sync_settings";
    public static final String INTENT_EXTRA_MESSENGER = "messenger";

    private final Messenger mInMessenger = new Messenger(new IncomingHandler(this));
    private Messenger mOutMessenger = null;

    private SyncSettings mSyncSettings;

    @Override
    public void onCreate() {

        super.onCreate();

        mSyncSettings = null;

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onCreate");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onBind " + intent);
        }

        Bundle extras = intent.getExtras();

        if ((extras != null) && extras.containsKey(INTENT_EXTRA_SYNC_SETTINGS)) {
            mSyncSettings = extras.getParcelable(INTENT_EXTRA_SYNC_SETTINGS);
        }

        // gets Messenger from the Activity
        if ((extras != null) && extras.containsKey(INTENT_EXTRA_MESSENGER)) {
            mOutMessenger = (Messenger) extras.get(INTENT_EXTRA_MESSENGER);
        }

        return mInMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onUnbind");
        }

        mOutMessenger = null;

        return super.onUnbind(intent);
    }

    protected SyncSettings getSyncSettings() {

        return mSyncSettings;
    }

    /**
     * Gets a new instance of {@link com.makina.ecrins.commons.sync.CheckServerService.CheckServerStatusAsyncTask}.
     *
     * @return a new instance of {@link com.makina.ecrins.commons.sync.CheckServerService.CheckServerStatusAsyncTask}
     */
    protected CheckServerStatusAsyncTask getAsyncTask() {

        return new CheckServerStatusAsyncTask();
    }

    /**
     * Sends a Message to this Messenger's Handler.
     *
     * @param what custom message code so that the recipient can identify what this message is about
     *             <p>Available values :
     *             <ul>
     *             <li>{@link #HANDLER_SYNC_SERVER_STATUS_PENDING}</li>
     *             <li>{@link #HANDLER_SYNC_SERVER_STATUS_OK}</li>
     *             <li>{@link #HANDLER_SYNC_SERVER_STATUS_KO}</li>
     *             </ul>
     *             </p>
     */
    private void sendMessage(int what) {

        Message message = Message.obtain();
        message.what = what;

        if (mOutMessenger != null) {
            try {
                mOutMessenger.send(message);
            }
            catch (RemoteException re) {
                Log.w(TAG,
                      re.getMessage(),
                      re);
            }
        }
    }

    private static class IncomingHandler
            extends Handler {

        private final WeakReference<CheckServerService> mCheckServerService;

        IncomingHandler(CheckServerService pCheckServerService) {

            super();
            mCheckServerService = new WeakReference<>(pCheckServerService);
        }

        @Override
        public void handleMessage(Message msg) {

            CheckServerService checkServerService = mCheckServerService.get();

            switch (msg.what) {
                case CheckServerService.HANDLER_SYNC_CHECK_SERVER_STATUS:

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "handleMessage HANDLER_SYNC_CHECK_SERVER_STATUS");
                    }

                    checkServerService.getAsyncTask()
                                      .execute();
                    break;
            }
        }
    }

    /**
     * {@code AsyncTask} basic implementation to check server status.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class CheckServerStatusAsyncTask
            extends AsyncTask<Void, Void, JSONObject> {

        private final WebAPIClient webAPIClient;

        CheckServerStatusAsyncTask() {

            this.webAPIClient = new WebAPIClient();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_PENDING);

            JSONObject response = null;

            try {
                final HttpURLConnection httpURLConnection = this.webAPIClient.post(getSyncSettings().getServerUrl() + getSyncSettings().getStatusUrl(),
                                                                                   getSyncSettings().getToken(),
                                                                                   null);
                httpURLConnection.connect();

                switch (httpURLConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        response = this.webAPIClient.readInputStreamAsJson(httpURLConnection.getInputStream());
                        break;
                    default:
                        break;
                }

                httpURLConnection.disconnect();
            }
            catch (IOException ioe) {
                Log.w(TAG,
                      ioe.getMessage());
            }

            return response;
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            if (result == null) {
                sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_KO);
            }
            else {
                if (this.webAPIClient.checkStatus(result)) {
                    sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_OK);
                }
                else {
                    sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_KO);
                }
            }
        }
    }
}
