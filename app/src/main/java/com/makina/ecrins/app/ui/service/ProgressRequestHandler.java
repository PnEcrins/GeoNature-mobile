package com.makina.ecrins.app.ui.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.makina.ecrins.app.BuildConfig;
import com.makina.ecrins.commons.service.AbstractRequestHandler;
import com.makina.ecrins.commons.service.RequestHandlerStatus;

/**
 * Dummy {@link com.makina.ecrins.commons.service.AbstractRequestHandler} to simulate a background
 * task sending a progress value.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ProgressRequestHandler extends AbstractRequestHandler {

    private static final String TAG = ProgressRequestHandler.class.getSimpleName();

    public static final String KEY_COMMAND = "KEY_COMMAND";
    public static final String KEY_PROGRESS_START = "KEY_PROGRESS_START";
    public static final String KEY_PROGRESS_END = "KEY_PROGRESS_END";
    public static final String KEY_PROGRESS_VALUE = "KEY_PROGRESS_VALUE";
    public static final String KEY_STATUS = "KEY_STATUS";

    protected RequestHandlerStatus mStatus;

    public ProgressRequestHandler(Context pContext) {
        super(pContext);

        this.mStatus = new RequestHandlerStatus(
                RequestHandlerStatus.Status.PENDING,
                ""
        );
    }

    @Override
    protected void handleMessageFromService(Message message) {

        if (BuildConfig.DEBUG) {
            Log.d(
                    TAG,
                    "handleMessageFromService"
            );
        }

        if (checkMessage(message) && message.getData().containsKey(KEY_COMMAND)) {
            switch ((Command) message.getData().getSerializable(KEY_COMMAND)) {
                case START:
                    if (message.getData().containsKey(KEY_PROGRESS_START) && message.getData().containsKey(KEY_PROGRESS_END)) {
                        new ProgressAsyncTask(message.getData()).execute();
                    }

                    break;
                case GET_STATUS:
                    message.getData()
                            .putParcelable(
                                    KEY_STATUS,
                                    mStatus
                            );
                    sendMessage(message.getData());

                    break;
            }
        }
    }

    public enum Command {
        START,
        GET_STATUS
    }

    /**
     * A dummy {@code AsyncTask} which send every second a progress message.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class ProgressAsyncTask extends AsyncTask<Void, Integer, Void> {

        private Bundle mData;

        public ProgressAsyncTask(Bundle data) {
            this.mData = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (BuildConfig.DEBUG) {
                Log.d(
                        getClass().getName(),
                        "doInBackground"
                );
            }

            mStatus = new RequestHandlerStatus(
                    RequestHandlerStatus.Status.RUNNING,
                    ""
            );
            mData.putParcelable(
                    KEY_STATUS,
                    mStatus
            );

            sendMessage(mData);

            int start = mData.getInt(KEY_PROGRESS_START);
            int end = mData.getInt(KEY_PROGRESS_END);

            if (start < end) {
                for (int i = start; i < end; i++) {
                    publishProgress(i);

                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ie) {
                        Log.w(
                                getClass().getName(),
                                ie.getMessage()
                        );
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mData.putInt(
                    KEY_PROGRESS_VALUE,
                    values[0]
            );

            sendMessage(mData);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mStatus = new RequestHandlerStatus(
                    RequestHandlerStatus.Status.FINISHED,
                    ""
            );
            mData.putParcelable(
                    KEY_STATUS,
                    mStatus
            );

            sendMessage(mData);
        }
    }
}
