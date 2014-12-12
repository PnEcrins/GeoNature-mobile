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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * <Code>Service</code> implementation to check server status.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("Registered")
public class CheckServerService extends Service {

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

        Log.d(getClass().getName(), "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getName(), "onBind " + intent);

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
        Log.d(getClass().getName(), "onUnbind");
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
                Log.w(getClass().getName(), re.getMessage(), re);
            }
        }
    }

    private static class IncomingHandler extends Handler {
        private final WeakReference<CheckServerService> mCheckServerService;

        public IncomingHandler(CheckServerService pCheckServerService) {
            super();
            mCheckServerService = new WeakReference<>(pCheckServerService);
        }

        @Override
        public void handleMessage(Message msg) {
            CheckServerService checkServerService = mCheckServerService.get();

            switch (msg.what) {
                case CheckServerService.HANDLER_SYNC_CHECK_SERVER_STATUS:
                    Log.d(getClass().getName(), "handleMessage HANDLER_SYNC_CHECK_SERVER_STATUS");
                    checkServerService.getAsyncTask().execute();
                    break;
            }
        }
    }

    /**
     * <code>AsyncTask</code> basic implementation to check server status.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class CheckServerStatusAsyncTask extends AsyncTask<Void, Void, StatusLine> {
        @Override
        protected StatusLine doInBackground(Void... params) {
            sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_PENDING);

            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpParams httpParameters = client.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            final List<NameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("token", getSyncSettings().getToken()));

            String urlStatus = getSyncSettings().getServerUrl() + getSyncSettings().getStatusUrl();

            Log.d(getClass().getName(), "url to check : " + urlStatus);

            HttpPost httpPost = new HttpPost(urlStatus);

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = client.execute(httpPost);

                return httpResponse.getStatusLine();
            }
            catch (UnsupportedEncodingException | ClientProtocolException ge) {
                Log.w(getClass().getName(), ge.getMessage());
            }
            catch (IOException ioe) {
                Log.w(getClass().getName(), ioe);
            }

            return null;
        }

        @Override
        protected void onPostExecute(StatusLine result) {
            if (result == null) {
                sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_KO);
            }
            else {
                switch (result.getStatusCode()) {
                    case HttpStatus.SC_OK:
                        sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_OK);
                        break;
                    default:
                        sendMessage(CheckServerService.HANDLER_SYNC_SERVER_STATUS_KO);
                        break;
                }
            }
        }
    }
}
