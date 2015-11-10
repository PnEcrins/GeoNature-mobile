package com.makina.ecrins.commons.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.service.AbstractRequestHandler;
import com.makina.ecrins.commons.service.RequestHandlerStatus;
import com.makina.ecrins.commons.util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * {@link com.makina.ecrins.commons.service.AbstractRequestHandler} implementation used to load
 * global application settings from a JSON file.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractLoadSettingsRequestHandler
        extends AbstractRequestHandler {

    private static final String TAG = AbstractLoadSettingsRequestHandler.class.getSimpleName();

    public static final String KEY_COMMAND = "KEY_COMMAND";
    public static final String KEY_STATUS = "KEY_STATUS";
    public static final String KEY_FILENAME = "KEY_FILENAME";
    public static final String KEY_APP_SETTINGS = "KEY_APP_SETTINGS";

    protected RequestHandlerStatus mStatus;
    protected AbstractAppSettings mAppSettings;

    public AbstractLoadSettingsRequestHandler(Context pContext) {
        super(pContext);

        this.mStatus = new RequestHandlerStatus(
                RequestHandlerStatus.Status.PENDING,
                ""
        );
        this.mAppSettings = null;
    }

    @Override
    protected void handleMessageFromService(Message message) {
        if (BuildConfig.DEBUG) {
            Log.d(
                    TAG,
                    "handleMessage"
            );
        }

        if (checkMessage(message) && message.getData().containsKey(KEY_COMMAND)) {
            switch ((Command) message.getData().getSerializable(KEY_COMMAND)) {
                case START:
                    if (!message.getData()
                            .containsKey(KEY_FILENAME)) {
                        Log.w(
                                TAG,
                                "'KEY_FILENAME' not defined!"
                        );
                    }

                    if (message.getData().containsKey(KEY_FILENAME)) {
                        new LoadSettingsFromFileAsyncTask(
                                message.getData()
                        ).execute();
                    }
                    break;
                case GET_STATUS:
                    message.getData()
                            .putParcelable(
                                    KEY_STATUS,
                                    mStatus
                            );

                    if (mAppSettings != null) {
                        message.getData().putParcelable(
                                KEY_APP_SETTINGS,
                                mAppSettings
                        );
                    }

                    sendMessage(message.getData());

                    break;
            }
        }
    }

    @NonNull
    protected abstract AbstractAppSettings getSettingsFromJsonObject(@NonNull JSONObject settingsJsonObject) throws JSONException;

    public enum Command {
        START,
        GET_STATUS
    }

    /**
     * {@code AsyncTask} implementation to load global application settings from a JSON file.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class LoadSettingsFromFileAsyncTask extends AsyncTask<Void, Void, AbstractAppSettings> {

        private final Bundle mData;

        public LoadSettingsFromFileAsyncTask(Bundle pData) {
            this.mData = pData;
        }

        @Override
        protected AbstractAppSettings doInBackground(Void... params) {

            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "loading global settings from '" + this.mData.getString(KEY_FILENAME) + "' ..."
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

            try {
                final File settingsJsonFile = FileUtils.getFileFromApplicationStorage(
                        getContext(),
                        this.mData.getString(KEY_FILENAME)
                );

                if (settingsJsonFile.exists()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                                TAG,
                                "load file '" + settingsJsonFile.getPath() + "'"
                        );
                    }

                    return getSettingsFromJsonObject(new JSONObject(FileUtils.readFileToString(settingsJsonFile)));
                }
                else {
                    Log.w(
                            TAG,
                            "unable to load file from path '" + settingsJsonFile.getPath() + "'"
                    );
                }
            }
            catch (IOException | JSONException ge) {
                Log.w(
                        TAG,
                        ge.getMessage(),
                        ge
                );
            }

            return null;
        }

        @Override
        protected void onPostExecute(AbstractAppSettings abstractAppSettings) {
            super.onPostExecute(abstractAppSettings);

            mAppSettings = abstractAppSettings;

            if (abstractAppSettings == null) {
                mStatus = new RequestHandlerStatus(
                        RequestHandlerStatus.Status.FINISHED_WITH_ERRORS,
                        this.mData.getString(KEY_FILENAME)
                );
            }
            else {
                mStatus = new RequestHandlerStatus(
                        RequestHandlerStatus.Status.FINISHED,
                        this.mData.getString(KEY_FILENAME)
                );

                this.mData.putParcelable(
                        KEY_APP_SETTINGS,
                        abstractAppSettings
                );
            }

            mData.putParcelable(
                    KEY_STATUS,
                    mStatus
            );

            sendMessage(this.mData);
        }
    }
}
