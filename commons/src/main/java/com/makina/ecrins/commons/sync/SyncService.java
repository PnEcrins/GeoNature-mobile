package com.makina.ecrins.commons.sync;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.util.FileUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * <Code>Service</code> implementation to perform in background the synchronization process.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SyncService extends Service {

    /**
     * Command to the service to register a client, receiving callbacks from the service.
     * The Message's replyTo field must be a Messenger of the client where callbacks should be sent.
     */
    public static final int HANDLER_REGISTER_CLIENT = 0;
    /**
     * Command to the service to unregister a client, to stop receiving callbacks from the service.
     * The Message's replyTo field must be a Messenger of the client as previously given with HANDLER_REGISTER_CLIENT.
     */
    public static final int HANDLER_UNREGISTER_CLIENT = 1;

    /**
     * Command to the service to start downloading data from the server
     */
    public static final int HANDLER_SYNC_PULL_DATA = 2;

    /**
     * Command to the service to start uploading data to the server
     */
    public static final int HANDLER_SYNC_PUSH_DATA = 3;

    /**
     * Command to the service to canceling the current task.
     */
    public static final int HANDLER_SYNC_CANCEL = 4;

    /**
     * Command to the service to get the current status
     */
    public static final int HANDLER_SYNC_STATUS = 5;

    /**
     * Command to send message that a given client was successfully registered.
     */
    public static final int HANDLER_CLIENT_REGISTERED = 6;

    /**
     * Command to send message to all registered clients
     */
    public static final int HANDLER_SYNC_MESSAGE = 7;

    /**
     * Command to send a progress message to all registered clients while downloading files
     */
    public static final int HANDLER_SYNC_PROGRESS_DOWNLOAD = 8;

    /**
     * Command to send a progress message to all registered clients while uploading files
     */
    public static final int HANDLER_SYNC_PROGRESS_UPLOAD = 9;

    public static final String INTENT_EXTRA_SYNC_SETTINGS = "sync_settings";

    protected PullDataAsyncTask mPullDataAsyncTask = null;
    protected PushDataAsyncTask mPushDataAsyncTask = null;
    protected SyncStatus mPullDataAsyncTaskStatus;
    protected SyncStatus mPushDataAsyncTaskStatus;

    /**
     * keeps track of all current registered clients.
     */
    protected final List<Messenger> mClients = new ArrayList<Messenger>();

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mInMessenger = new Messenger(new IncomingHandler(this));

    private SyncSettings mSyncSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.PENDING, "idle");
        mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.PENDING, "idle");
        mSyncSettings = null;

        Log.d(getClass().getName(), "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getName(), "onBind " + intent);

        if ((intent.getExtras() != null) && intent.getExtras().containsKey(INTENT_EXTRA_SYNC_SETTINGS)) {
            mSyncSettings = intent.getExtras().getParcelable(INTENT_EXTRA_SYNC_SETTINGS);
        }

        return mInMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(getClass().getName(), "onUnbind " + intent);

        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "onDestroy");

        super.onDestroy();
    }

    protected SyncSettings getSyncSettings() {
        return mSyncSettings;
    }

    /**
     * Gets a new instance of {@link com.makina.ecrins.commons.sync.SyncService.PullDataAsyncTask} or a previously created instance.
     *
     * @return a new instance of {@link com.makina.ecrins.commons.sync.SyncService.PullDataAsyncTask}
     */
    protected PullDataAsyncTask getPullDataAsyncTask() {
        if (mPullDataAsyncTask == null) {
            Log.d(getClass().getName(), "getPullDataAsyncTask : create new instance");
            mPullDataAsyncTask = new PullDataAsyncTask();
        }
        else {
            Log.d(getClass().getName(), "getPullDataAsyncTask : initialized");
        }

        return mPullDataAsyncTask;
    }

    /**
     * Gets a new instance of {@link com.makina.ecrins.commons.sync.SyncService.PushDataAsyncTask} or a previously created instance.
     *
     * @return a new instance of {@link com.makina.ecrins.commons.sync.SyncService.PushDataAsyncTask}
     */
    protected PushDataAsyncTask getPushDataAsyncTask() {
        if (mPushDataAsyncTask == null) {
            Log.d(getClass().getName(), "getPushDataAsyncTask : create new instance");
            mPushDataAsyncTask = new PushDataAsyncTask();
        }
        else {
            Log.d(getClass().getName(), "getPushDataAsyncTask : initialized");
        }

        return mPushDataAsyncTask;
    }

    /**
     * Sends a progress message to this Messenger's Handler.
     *
     * @param what              custom message code so that the recipient can identify what this message is about
     * @param mainProgress      the current progress as percentage
     * @param secondaryProgress the secondary progress as percentage
     */
    protected void sendProgress(int what, int mainProgress, int secondaryProgress) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null, what, mainProgress, secondaryProgress));
            }
            catch (RemoteException re) {
                // The client is dead.
                // Remove it from the list.
                // We are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    /**
     * Sends a message to this Messenger's Handler.
     *
     * @param what custom message code so that the recipient can identify what this message is about
     * @param obj  object message to send to the recipient
     */
    protected void sendMessage(int what, Object obj) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null, what, obj));
            }
            catch (RemoteException re) {
                // The client is dead.
                // Remove it from the list.
                // We are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    /**
     * Gets the current status of this service.
     *
     * @return {@link SyncStatus} instance describing the current status
     */
    protected SyncStatus getSyncStatus() {
        Log.d(getClass().getName(), "getSyncStatus : " + mPushDataAsyncTaskStatus.getStatus().name() + " " + mPullDataAsyncTaskStatus.getStatus().name());

        SyncStatus syncStatus = new SyncStatus(SyncStatus.Status.PENDING, SyncStatus.Status.PENDING.name());

        if (mPullDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.RUNNING) || mPushDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.RUNNING)) {
            syncStatus = new SyncStatus(SyncStatus.Status.RUNNING, SyncStatus.Status.RUNNING.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.ABORTED) || mPushDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.ABORTED)) {
            syncStatus = new SyncStatus(SyncStatus.Status.ABORTED, SyncStatus.Status.ABORTED.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.FINISHED) && mPushDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.FINISHED_WITH_ERRORS)) {
            syncStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, SyncStatus.Status.FINISHED_WITH_ERRORS.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.FINISHED_WITH_ERRORS) && mPushDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.FINISHED)) {
            syncStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, SyncStatus.Status.FINISHED_WITH_ERRORS.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.FINISHED) && mPushDataAsyncTaskStatus.getStatus().equals(SyncStatus.Status.FINISHED)) {
            syncStatus = new SyncStatus(SyncStatus.Status.FINISHED, SyncStatus.Status.FINISHED.name());
        }

        return syncStatus;
    }

    /**
     * Checks the current status before stopping the service.
     */
    protected void checkStatusAndStop() {
        if (getSyncStatus().getStatus().equals(SyncStatus.Status.ABORTED) || getSyncStatus().getStatus().equals(SyncStatus.Status.FINISHED) || getSyncStatus().getStatus().equals(SyncStatus.Status.FINISHED_WITH_ERRORS)) {
            mPullDataAsyncTask = null;
            mPushDataAsyncTask = null;

            stopSelf();
        }
    }

    /**
     * Handler of incoming messages from clients.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class IncomingHandler extends Handler {
        private final WeakReference<SyncService> mSyncService;

        public IncomingHandler(SyncService pSyncService) {
            super();
            mSyncService = new WeakReference<SyncService>(pSyncService);
        }

        @Override
        public void handleMessage(Message msg) {
            SyncService syncService = mSyncService.get();

            switch (msg.what) {
                case SyncService.HANDLER_REGISTER_CLIENT:
                    Log.d(getClass().getName(), "handleMessage HANDLER_REGISTER_CLIENT");

                    if (syncService.mClients.add(msg.replyTo)) {
                        syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS, syncService.getSyncStatus());
                        syncService.sendMessage(SyncService.HANDLER_CLIENT_REGISTERED, msg.replyTo);
                    }
                    else {
                        Log.w(getClass().getName(), "SyncService.HANDLER_REGISTER_CLIENT : failed to register client");
                    }

                    break;
                case SyncService.HANDLER_UNREGISTER_CLIENT:
                    Log.d(getClass().getName(), "handleMessage HANDLER_UNREGISTER_CLIENT");
                    syncService.mClients.remove(msg.replyTo);
                    break;
                case SyncService.HANDLER_SYNC_PULL_DATA:
                    if (syncService.getPullDataAsyncTask().getStatus().equals(Status.PENDING) && (syncService.getSyncSettings() != null)) {
                        Log.d(getClass().getName(), "handleMessage HANDLER_SYNC_PULL_DATA");
                        syncService.mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.RUNNING, "starting the synchronization process ...");
                        syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS, syncService.getSyncStatus());
                        syncService.getPullDataAsyncTask().execute(syncService.getSyncSettings().getExports().toArray(new ExportSettings[]{}));
                    }

                    break;
                case SyncService.HANDLER_SYNC_PUSH_DATA:
                    if (syncService.getPushDataAsyncTask().getStatus().equals(Status.PENDING) && (syncService.getSyncSettings() != null)) {
                        Log.d(getClass().getName(), "handleMessage HANDLER_SYNC_PUSH_DATA");
                        syncService.mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.RUNNING, "starting the synchronization process ...");
                        syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS, syncService.getSyncStatus());

                        try {
                            File inputDir = FileUtils.getInputsFolder(syncService);

                            if (inputDir.exists()) {
                                syncService.getPushDataAsyncTask().execute(inputDir.listFiles(new FileFilter() {
                                    @Override
                                    public boolean accept(File pathname) {
                                        return pathname.getName().startsWith("input_") && pathname.getName().endsWith(".json");
                                    }
                                }));
                            }
                            else {
                                Log.d(getClass().getName(), "handleMessage HANDLER_SYNC_PUSH_DATA : no inputs to upload");

                                syncService.mPushDataAsyncTask = null;
                                syncService.mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED, syncService.getString(R.string.synchro_message_upload_no_data));

                                syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS, syncService.getSyncStatus());
                                syncService.sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD, 100, 100);
                                syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, syncService.mPushDataAsyncTaskStatus));
                                syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, syncService.mPushDataAsyncTaskStatus));
                            }
                        }
                        catch (IOException ioe) {
                            Log.w(getClass().getName(), "handleMessage HANDLER_SYNC_PUSH_DATA : no inputs to upload");

                            syncService.mPushDataAsyncTask = null;
                            syncService.mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, syncService.getString(R.string.synchro_message_upload_unable_to_load_dir));

                            syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS, syncService.getSyncStatus());
                            syncService.sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD, 100, 100);
                            syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, syncService.mPushDataAsyncTaskStatus));
                            syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, syncService.mPushDataAsyncTaskStatus));
                        }
                    }

                    break;
                case SyncService.HANDLER_SYNC_STATUS:
                    Log.d(getClass().getName(), "handleMessage HANDLER_SYNC_STATUS " + syncService.getSyncStatus().getStatus().name());
                    syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS, syncService.getSyncStatus());
                    break;
                case SyncService.HANDLER_SYNC_CANCEL:
                    Log.d(getClass().getName(), "handleMessage HANDLER_SYNC_CANCEL");
                    syncService.getPullDataAsyncTask().cancel(true);
                    syncService.mPullDataAsyncTask = null;
                    syncService.stopSelf();
                    break;
            }
        }
    }

    /**
     * <code>AsyncTask</code> implementation to download files from the server.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class PullDataAsyncTask extends AsyncTask<ExportSettings, Void, Boolean> {
        @Override
        protected Boolean doInBackground(ExportSettings... params) {
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, new SyncStatus(SyncStatus.Status.RUNNING, "starting downloading files ...")));

            final DefaultHttpClient httpClient = new DefaultHttpClient();
            final HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            //HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("token", getSyncSettings().getToken()));
            int currentExport = 0;

            for (ExportSettings exportSettings : params) {
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, new SyncStatus(SyncStatus.Status.RUNNING, String.format(getString(R.string.synchro_message_downloading), exportSettings.getFile()))));
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, new SyncStatus(SyncStatus.Status.RUNNING, String.format(getString(R.string.synchro_message_downloading), exportSettings.getFile()))));
                sendProgress(SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD, (int) (((double) currentExport / (double) params.length) * 100), 0);

                HttpPost httpPost = new HttpPost(getSyncSettings().getServerUrl() + exportSettings.getUrl());

                try {
                    File originalFile = FileUtils.getFileFromApplicationStorage(SyncService.this, exportSettings.getFile());
                    File tempFile = FileUtils.getFileFromApplicationStorage(SyncService.this, exportSettings.getFile() + ".tmp");

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    // checks if server response is valid
                    StatusLine status = httpResponse.getStatusLine();

                    if (status.getStatusCode() == HttpStatus.SC_OK) {
                        // pulls content stream from response
                        HttpEntity entity = httpResponse.getEntity();
                        InputStream inputStream = entity.getContent();

                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                        else {
                            tempFile.createNewFile();
                        }

                        if (copyInputStream(inputStream, new FileOutputStream(tempFile), entity.getContentLength(), currentExport, params.length)) {
                            originalFile.delete();

                            if (tempFile.renameTo(FileUtils.getFileFromApplicationStorage(SyncService.this, exportSettings.getFile()))) {
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, new SyncStatus(SyncStatus.Status.RUNNING, getString(R.string.synchro_message_downloading_file_ok))));
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, new SyncStatus(SyncStatus.Status.RUNNING, getString(R.string.synchro_message_downloading_file_ok))));
                            }
                            else {
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, String.format(getString(R.string.synchro_message_downloading_copy_file_ko), exportSettings.getFile()))));
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.ERROR, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, String.format(getString(R.string.synchro_message_downloading_copy_file_ko), exportSettings.getFile()))));

                                return false;
                            }
                        }
                        else {
                            if (!isCancelled()) {
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, String.format(getString(R.string.synchro_message_downloading_file_ko), exportSettings.getFile()))));
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.ERROR, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, "failed to load file '" + exportSettings.getFile() + "' from URL '" + httpPost.getURI().toString() + "'")));
                            }

                            return false;
                        }

                        if (tempFile.exists()) {
                            tempFile.delete();
                        }

                        if (isCancelled()) {
                            return false;
                        }
                    }
                    else {
                        sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, String.format(getString(R.string.synchro_message_downloading_file_ko), exportSettings.getFile()))));
                        sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.ERROR, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, "unable to download file from URL '" + httpPost.getURI().toString() + "', HTTP status : " + status.getStatusCode())));

                        return false;
                    }
                }
                catch (IOException ioe) {
                    Log.w(getClass().getName(), ioe.getMessage(), ioe);
                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.WARNING, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, ioe.getLocalizedMessage())));

                    return false;
                }

                currentExport++;
            }

            sendProgress(SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD, 100, 100);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(getClass().getName(), "onPostExecute");

            if (result) {
                mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED, getString(R.string.synchro_message_downloading_complete));
            }
            else {
                mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, getString(R.string.synchro_message_downloading_complete_with_errors));
            }

            sendMessage(SyncService.HANDLER_SYNC_STATUS, getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, mPullDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, mPullDataAsyncTaskStatus));

            Log.d(getClass().getName(), "onPostExecute " + mPullDataAsyncTaskStatus.getStatus().name());

            checkStatusAndStop();
        }

        @Override
        protected void onCancelled() {
            Log.d(getClass().getName(), "onCancelled");

            mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.ABORTED, getString(R.string.synchro_message_downloading_aborted));
            sendMessage(SyncService.HANDLER_SYNC_STATUS, getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS, mPullDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, mPullDataAsyncTaskStatus));

            Log.d(getClass().getName(), "onCancelled " + mPullDataAsyncTaskStatus.getStatus().name());

            checkStatusAndStop();
        }

        private boolean copyInputStream(InputStream in, OutputStream out, long contentLength, int currentExport, int numberOfExports) {
            Log.d(getClass().getName(), "content-length : " + contentLength);

            byte[] buffer = new byte[1024];

            int len;
            long totalBytesRead = 0;

            try {
                while ((len = in.read(buffer)) >= 0) {
                    if (isCancelled()) {
                        return false;
                    }

                    out.write(buffer, 0, len);
                    totalBytesRead += len;

                    int currentProgress = (int) (((double) totalBytesRead / (double) contentLength) * 100);
                    sendProgress(SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD, (int) (((double) currentExport / (double) numberOfExports) * 100) + (currentProgress / numberOfExports), currentProgress);
                }

                out.flush();

                in.close();
                out.close();
            }
            catch (IOException ioe) {
                Log.w(getClass().getName(), ioe.getMessage(), ioe);
                return false;
            }

            return true;
        }
    }

    /**
     * <code>AsyncTask</code> implementation to upload all saved inputs to the server.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class PushDataAsyncTask extends AsyncTask<File, Void, Boolean> {
        @Override
        protected Boolean doInBackground(File... params) {
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, new SyncStatus(SyncStatus.Status.RUNNING, "starting uploading inputs ...")));

            final DefaultHttpClient httpClient = new DefaultHttpClient();
            final HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            //HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("token", getSyncSettings().getToken()));
            nameValuePairs.add(new BasicNameValuePair("data", "{}"));

            int currentInput = 0;

            for (File input : params) {
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, new SyncStatus(SyncStatus.Status.RUNNING, String.format(getString(R.string.synchro_message_uploading), input.getName()))));
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, new SyncStatus(SyncStatus.Status.RUNNING, String.format(getString(R.string.synchro_message_uploading), input.getName()))));
                sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD, (int) (((double) currentInput / (double) params.length) * 100), 0);

                HttpPost httpPost = new HttpPost(getSyncSettings().getServerUrl() + getSyncSettings().getImportUrl());

                try {
                    Log.d(getClass().getName(), FileUtils.readFileToString(input));

                    // reads input as JSON file
                    nameValuePairs.set(1, new BasicNameValuePair("data", FileUtils.readFileToString(input)));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    // checks if server response is valid
                    StatusLine status = httpResponse.getStatusLine();

                    if (status.getStatusCode() == HttpStatus.SC_OK) {
                        // pulls content stream from response
                        HttpEntity entity = httpResponse.getEntity();
                        InputStream inputStream = entity.getContent();

                        if (readInputStreamAsJson(inputStream, entity.getContentLength(), currentInput, params.length)) {
                            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, new SyncStatus(SyncStatus.Status.RUNNING, getString(R.string.synchro_message_uploading_file_ok))));
                            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, new SyncStatus(SyncStatus.Status.RUNNING, getString(R.string.synchro_message_uploading_file_ok))));
                            // copy file into a new directory with prefix ok for back up
                            SaveFileWithPrefix(input, "ok_");
                        }
                        else {
                            if (!isCancelled()) {
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, String.format(getString(R.string.synchro_message_uploading_file_ko), input.getName()))));
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.ERROR, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, "failed to upload input '" + input.getName() + "' from URL '" + httpPost.getURI().toString() + "'")));
                                // copy file into a new directory with prefix error for back up and delete it
                                SaveFileWithPrefix(input, "ko_");
                            }

                            return false;
                        }

                        if (isCancelled()) {
                            return false;
                        }
                    }
                    else {
                        sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, String.format(getString(R.string.synchro_message_uploading_file_ko), input.getName()))));
                        sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.ERROR, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, "unable to upload input from URL '" + httpPost.getURI().toString() + "', HTTP status : " + status.getStatusCode())));
                        // copy file into a new directory with prefix error for back up and delete it
                        SaveFileWithPrefix(input, "ko_");

                        return false;
                    }
                }
                catch (IOException ioe) {
                    Log.w(getClass().getName(), ioe.getMessage(), ioe);
                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.WARNING, new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, ioe.getLocalizedMessage())));

                    return false;
                }

                currentInput++;
            }

            sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD, 100, 100);

            return true;
        }

        /*
         * Save the File input.json in a back up directory specify with current date
         * @param file : the file to copy
         * @param prefix : the prefix to add to the destination file
         */
        private void SaveFileWithPrefix(File file, String prefix) throws IOException {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = dateformat.format(new Date());
            File dest = new File(file.getParent() + "/" + date + "/" + prefix + file.getName());
            FileUtils.copyFile(file, dest);
            FileUtils.deleteQuietly(file);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(getClass().getName(), "onPostExecute");

            if (result) {
                mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED, getString(R.string.synchro_message_uploading_complete));
            }
            else {
                mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS, getString(R.string.synchro_message_uploading_complete_with_errors));
            }

            sendMessage(SyncService.HANDLER_SYNC_STATUS, getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, mPushDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, mPushDataAsyncTaskStatus));

            Log.d(getClass().getName(), "onPostExecute " + mPushDataAsyncTaskStatus.getStatus().name());

            checkStatusAndStop();
        }

        @Override
        protected void onCancelled() {
            Log.d(getClass().getName(), "onCancelled");

            mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.ABORTED, "aborted");
            sendMessage(SyncService.HANDLER_SYNC_STATUS, getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS, mPushDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE, new SyncMessage(SyncMessage.MessageType.INFO, mPushDataAsyncTaskStatus));

            Log.d(getClass().getName(), "onCancelled " + mPushDataAsyncTaskStatus.getStatus().name());

            checkStatusAndStop();
        }

        private boolean readInputStreamAsJson(InputStream in, long contentLength, int currentInput, int numberOfInputs) {
            Log.d(getClass().getName(), "content-length : " + contentLength);

            final OutputStream out = new OutputStream() {
                private StringBuilder string = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    this.string.append((char) b);
                }

                @Override
                public String toString() {
                    return this.string.toString();
                }
            };

            byte[] buffer = new byte[1024];

            int len;
            long totalBytesRead = 0;

            try {
                while ((len = in.read(buffer)) >= 0) {
                    if (isCancelled()) {
                        out.flush();
                        in.close();
                        out.close();

                        return false;
                    }

                    out.write(buffer, 0, len);
                    totalBytesRead += len;

                    int currentProgress = (int) (((double) totalBytesRead / (double) contentLength) * 100);
                    sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD, (int) (((double) currentInput / (double) numberOfInputs) * 100) + (currentProgress / numberOfInputs), currentProgress);
                }

                out.flush();
                in.close();
                out.close();

                // Try to build the server response as JSON and check the status code
                JSONObject jsonResponse = new JSONObject(out.toString());
                int status = jsonResponse.getInt("status_code");

                return status == 0;
            }
            catch (IOException ioe) {
                Log.w(getClass().getName(), ioe.getMessage(), ioe);
                return false;
            }
            catch (JSONException je) {
                Log.w(getClass().getName(), je.getMessage(), je);
                return false;
            }
        }
    }
}
