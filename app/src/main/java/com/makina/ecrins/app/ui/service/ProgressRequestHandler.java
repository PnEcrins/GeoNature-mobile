package com.makina.ecrins.app.ui.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.makina.ecrins.app.BuildConfig;
import com.makina.ecrins.commons.service.AbstractRequestHandler;

/**
 * Dummy {@link com.makina.ecrins.commons.service.AbstractRequestHandler} to simulate a background
 * task sending a progress value.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ProgressRequestHandler extends AbstractRequestHandler {

    private static final String TAG = ProgressRequestHandler.class.getSimpleName();

    public static final String KEY_PROGRESS_START = "KEY_PROGRESS_START";
    public static final String KEY_PROGRESS_END = "KEY_PROGRESS_END";
    public static final String KEY_PROGRESS_VALUE = "KEY_PROGRESS_VALUE";
    public static final String KEY_PROGRESS_FINISH = "KEY_PROGRESS_FINISH";

    public ProgressRequestHandler(Context pContext) {
        super(pContext);
    }

    @Override
    protected void handleMessageFromService(Message message) {

        if (BuildConfig.DEBUG) {
            Log.d(
                    TAG,
                    "handleMessage"
            );
        }

        if (checkMessage(message) && message.getData().containsKey(KEY_PROGRESS_START) && message.getData().containsKey(KEY_PROGRESS_END)) {
            new ProgressAsyncTask(message.getData()).execute();
        }
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

            mData.putBoolean(
                    KEY_PROGRESS_FINISH,
                    true
            );

            sendMessage(mData);
        }
    }
}
