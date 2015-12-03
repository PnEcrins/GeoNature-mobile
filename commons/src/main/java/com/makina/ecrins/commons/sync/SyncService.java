package com.makina.ecrins.commons.sync;

import android.annotation.SuppressLint;
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

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code Service} implementation to perform in background the synchronization process.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("Registered")
public class SyncService
        extends Service {

    private static final String TAG = SyncService.class.getName();

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
    protected final List<Messenger> mClients = new ArrayList<>();

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mInMessenger = new Messenger(new IncomingHandler(this));

    private SyncSettings mSyncSettings;

    @Override
    public void onCreate() {

        super.onCreate();

        mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.PENDING,
                                                  "idle");
        mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.PENDING,
                                                  "idle");
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

        if ((intent.getExtras() != null) && intent.getExtras()
                                                  .containsKey(INTENT_EXTRA_SYNC_SETTINGS)) {
            mSyncSettings = intent.getExtras()
                                  .getParcelable(INTENT_EXTRA_SYNC_SETTINGS);
        }

        return mInMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onUnbind " + intent);
        }

        return true;
    }

    @Override
    public void onDestroy() {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onDestroy");
        }

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
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "getPullDataAsyncTask: create new instance");
            }

            mPullDataAsyncTask = new PullDataAsyncTask();
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "getPullDataAsyncTask: initialized");
            }
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "getPushDataAsyncTask: create new instance");
            }

            mPushDataAsyncTask = new PushDataAsyncTask();
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "getPushDataAsyncTask: initialized");
            }
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
    protected void sendProgress(
            int what,
            int mainProgress,
            int secondaryProgress) {

        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i)
                        .send(Message.obtain(null,
                                             what,
                                             mainProgress,
                                             secondaryProgress));
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
    protected void sendMessage(
            int what,
            Object obj) {

        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i)
                        .send(Message.obtain(null,
                                             what,
                                             obj));
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

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "getSyncStatus : " + mPushDataAsyncTaskStatus.getStatus()
                                                               .name() + " " + mPullDataAsyncTaskStatus.getStatus()
                                                                                                       .name());
        }

        SyncStatus syncStatus = new SyncStatus(SyncStatus.Status.PENDING,
                                               SyncStatus.Status.PENDING.name());

        if (mPullDataAsyncTaskStatus.getStatus()
                                    .equals(SyncStatus.Status.RUNNING) || mPushDataAsyncTaskStatus.getStatus()
                                                                                                  .equals(SyncStatus.Status.RUNNING)) {
            syncStatus = new SyncStatus(SyncStatus.Status.RUNNING,
                                        SyncStatus.Status.RUNNING.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus()
                                    .equals(SyncStatus.Status.ABORTED) || mPushDataAsyncTaskStatus.getStatus()
                                                                                                  .equals(SyncStatus.Status.ABORTED)) {
            syncStatus = new SyncStatus(SyncStatus.Status.ABORTED,
                                        SyncStatus.Status.ABORTED.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus()
                                    .equals(SyncStatus.Status.FINISHED) && mPushDataAsyncTaskStatus.getStatus()
                                                                                                   .equals(SyncStatus.Status.FINISHED_WITH_ERRORS)) {
            syncStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                        SyncStatus.Status.FINISHED_WITH_ERRORS.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus()
                                    .equals(SyncStatus.Status.FINISHED_WITH_ERRORS) && mPushDataAsyncTaskStatus.getStatus()
                                                                                                               .equals(SyncStatus.Status.FINISHED)) {
            syncStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                        SyncStatus.Status.FINISHED_WITH_ERRORS.name());
        }

        if (mPullDataAsyncTaskStatus.getStatus()
                                    .equals(SyncStatus.Status.FINISHED) && mPushDataAsyncTaskStatus.getStatus()
                                                                                                   .equals(SyncStatus.Status.FINISHED)) {
            syncStatus = new SyncStatus(SyncStatus.Status.FINISHED,
                                        SyncStatus.Status.FINISHED.name());
        }

        return syncStatus;
    }

    /**
     * Checks the current status before stopping the service.
     */
    protected void checkStatusAndStop() {

        if (getSyncStatus().getStatus()
                           .equals(SyncStatus.Status.ABORTED) || getSyncStatus().getStatus()
                                                                                .equals(SyncStatus.Status.FINISHED) || getSyncStatus().getStatus()
                                                                                                                                      .equals(SyncStatus.Status.FINISHED_WITH_ERRORS)) {
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
    private static class IncomingHandler
            extends Handler {

        private final WeakReference<SyncService> mSyncService;

        public IncomingHandler(SyncService pSyncService) {

            super();
            mSyncService = new WeakReference<>(pSyncService);
        }

        @Override
        public void handleMessage(Message msg) {

            SyncService syncService = mSyncService.get();

            switch (msg.what) {
                case SyncService.HANDLER_REGISTER_CLIENT:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "handleMessage HANDLER_REGISTER_CLIENT");
                    }

                    if (syncService.mClients.add(msg.replyTo)) {
                        syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS,
                                                syncService.getSyncStatus());
                        syncService.sendMessage(SyncService.HANDLER_CLIENT_REGISTERED,
                                                msg.replyTo);
                    }
                    else {
                        Log.w(TAG,
                              "SyncService.HANDLER_REGISTER_CLIENT: failed to register client");
                    }

                    break;
                case SyncService.HANDLER_UNREGISTER_CLIENT:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "handleMessage HANDLER_UNREGISTER_CLIENT");
                    }

                    syncService.mClients.remove(msg.replyTo);
                    break;
                case SyncService.HANDLER_SYNC_PULL_DATA:
                    if (syncService.getPullDataAsyncTask()
                                   .getStatus()
                                   .equals(Status.PENDING) && (syncService.getSyncSettings() != null)) {

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG,
                                  "handleMessage HANDLER_SYNC_PULL_DATA");
                        }

                        syncService.mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.RUNNING,
                                                                              "starting the synchronization process ...");
                        syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS,
                                                syncService.getSyncStatus());

                        final List<ExportSettings> exports = syncService.getSyncSettings()
                                                                        .getExports();
                        syncService.getPullDataAsyncTask()
                                   .execute(exports.toArray(new ExportSettings[exports.size()]));
                    }

                    break;
                case SyncService.HANDLER_SYNC_PUSH_DATA:
                    if (syncService.getPushDataAsyncTask()
                                   .getStatus()
                                   .equals(Status.PENDING) && (syncService.getSyncSettings() != null)) {

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG,
                                  "handleMessage HANDLER_SYNC_PUSH_DATA");
                        }

                        syncService.mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.RUNNING,
                                                                              "starting the synchronization process ...");
                        syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS,
                                                syncService.getSyncStatus());

                        try {
                            File inputDir = FileUtils.getInputsFolder(syncService);

                            if (inputDir.exists()) {
                                syncService.getPushDataAsyncTask()
                                           .execute(inputDir.listFiles(new FileFilter() {
                                               @Override
                                               public boolean accept(File pathname) {

                                                   return pathname.getName()
                                                                  .startsWith("input_") && pathname.getName()
                                                                                                   .endsWith(".json");
                                               }
                                           }));
                            }
                            else {
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG,
                                          "handleMessage HANDLER_SYNC_PUSH_DATA: no inputs to upload");
                                }

                                syncService.mPushDataAsyncTask = null;
                                syncService.mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED,
                                                                                      syncService.getString(R.string.synchro_message_upload_no_data));

                                syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS,
                                                        syncService.getSyncStatus());
                                syncService.sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD,
                                                         100,
                                                         100);
                                syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                        new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                                                        syncService.mPushDataAsyncTaskStatus));
                                syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                        new SyncMessage(SyncMessage.MessageType.INFO,
                                                                        syncService.mPushDataAsyncTaskStatus));
                            }
                        }
                        catch (IOException ioe) {
                            Log.w(TAG,
                                  "handleMessage HANDLER_SYNC_PUSH_DATA: no inputs to upload");

                            syncService.mPushDataAsyncTask = null;
                            syncService.mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                                  syncService.getString(R.string.synchro_message_upload_unable_to_load_dir));

                            syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS,
                                                    syncService.getSyncStatus());
                            syncService.sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD,
                                                     100,
                                                     100);
                            syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                    new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                                                    syncService.mPushDataAsyncTaskStatus));
                            syncService.sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                    new SyncMessage(SyncMessage.MessageType.INFO,
                                                                    syncService.mPushDataAsyncTaskStatus));
                        }
                    }

                    break;
                case SyncService.HANDLER_SYNC_STATUS:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "handleMessage HANDLER_SYNC_STATUS " + syncService.getSyncStatus()
                                                                                .getStatus()
                                                                                .name());
                    }

                    syncService.sendMessage(SyncService.HANDLER_SYNC_STATUS,
                                            syncService.getSyncStatus());
                    break;
                case SyncService.HANDLER_SYNC_CANCEL:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "handleMessage HANDLER_SYNC_CANCEL");
                    }

                    syncService.getPullDataAsyncTask()
                               .cancel(true);
                    syncService.mPullDataAsyncTask = null;
                    syncService.stopSelf();
                    break;
            }
        }
    }

    /**
     * {@code AsyncTask} implementation to download files from the server.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class PullDataAsyncTask
            extends AsyncTask<ExportSettings, Void, Boolean> {

        private final WebAPIClient webAPIClient;

        public PullDataAsyncTask() {

            this.webAPIClient = new WebAPIClient();
        }

        @Override
        protected Boolean doInBackground(final ExportSettings... params) {

            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.INFO,
                                        new SyncStatus(SyncStatus.Status.RUNNING,
                                                       "starting downloading files ...")));

            final AtomicInteger currentExport = new AtomicInteger();

            for (ExportSettings exportSettings : params) {
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                            new SyncMessage(SyncMessage.MessageType.INFO,
                                            new SyncStatus(SyncStatus.Status.RUNNING,
                                                           String.format(getString(R.string.synchro_message_downloading),
                                                                         exportSettings.getFile()))));
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                            new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                            new SyncStatus(SyncStatus.Status.RUNNING,
                                                           String.format(getString(R.string.synchro_message_downloading),
                                                                         exportSettings.getFile()))));
                sendProgress(SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD,
                             (int) (((double) currentExport.get() / (double) params.length) * 100),
                             0);

                try {
                    final HttpURLConnection httpURLConnection = this.webAPIClient.post(getSyncSettings().getServerUrl() + exportSettings.getUrl(),
                                                                                       getSyncSettings().getToken(),
                                                                                       null);
                    httpURLConnection.connect();

                    switch (httpURLConnection.getResponseCode()) {
                        case HttpURLConnection.HTTP_OK:
                            File originalFile = FileUtils.getFile(FileUtils.getRootFolder(SyncService.this,
                                                                                          MountPoint.StorageType.INTERNAL),
                                                                  exportSettings.getFile());
                            File tempFile = FileUtils.getFile(FileUtils.getRootFolder(SyncService.this,
                                                                                      MountPoint.StorageType.INTERNAL),
                                                              exportSettings.getFile() + ".tmp");

                            if (tempFile.exists()) {
                                // noinspection ResultOfMethodCallIgnored
                                tempFile.delete();
                            }
                            else {
                                // noinspection ResultOfMethodCallIgnored
                                tempFile.createNewFile();
                            }

                            if (copyInputStream(httpURLConnection.getInputStream(),
                                                new FileOutputStream(tempFile),
                                                httpURLConnection.getContentLength(),
                                                currentExport.get(),
                                                params.length)) {
                                // noinspection ResultOfMethodCallIgnored
                                originalFile.delete();

                                if (tempFile.renameTo(FileUtils.getFile(FileUtils.getRootFolder(SyncService.this,
                                                                                                MountPoint.StorageType.INTERNAL),
                                                                        exportSettings.getFile()))) {
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                                                new SyncStatus(SyncStatus.Status.RUNNING,
                                                                               getString(R.string.synchro_message_downloading_file_ok))));
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.INFO,
                                                                new SyncStatus(SyncStatus.Status.RUNNING,
                                                                               getString(R.string.synchro_message_downloading_file_ok))));
                                }
                                else {
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                               String.format(getString(R.string.synchro_message_downloading_copy_file_ko),
                                                                                             exportSettings.getFile()))));
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.ERROR,
                                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                               String.format(getString(R.string.synchro_message_downloading_copy_file_ko),
                                                                                             exportSettings.getFile()))));

                                    return false;
                                }
                            }
                            else {
                                if (!isCancelled()) {
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                               String.format(getString(R.string.synchro_message_downloading_file_ko),
                                                                                             exportSettings.getFile()))));
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.ERROR,
                                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                               "failed to load file '" + exportSettings.getFile() + "' from URL '" + getSyncSettings().getServerUrl() + exportSettings.getUrl() + "'")));
                                }

                                return false;
                            }

                            if (tempFile.exists()) {
                                // noinspection ResultOfMethodCallIgnored
                                tempFile.delete();
                            }

                            if (isCancelled()) {
                                return false;
                            }

                            break;
                        default:
                            if (!isCancelled()) {
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                            new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                                            new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                           String.format(getString(R.string.synchro_message_downloading_file_ko),
                                                                                         exportSettings.getFile()))));
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                            new SyncMessage(SyncMessage.MessageType.ERROR,
                                                            new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                           "failed to load file '" + exportSettings.getFile() + "' from URL '" + getSyncSettings().getServerUrl() + exportSettings.getUrl() + "'")));
                            }

                            return false;
                    }

                    httpURLConnection.disconnect();
                }
                catch (IOException ioe) {
                    Log.w(TAG,
                          ioe.getMessage(),
                          ioe);
                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                new SyncMessage(SyncMessage.MessageType.WARNING,
                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                               ioe.getLocalizedMessage())));

                    return false;
                }

                currentExport.incrementAndGet();
            }

            sendProgress(SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD,
                         100,
                         100);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onPostExecute");
            }

            if (result) {
                mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED,
                                                          getString(R.string.synchro_message_downloading_complete));
            }
            else {
                mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                          getString(R.string.synchro_message_downloading_complete_with_errors));
            }

            sendMessage(SyncService.HANDLER_SYNC_STATUS,
                        getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                        mPullDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.INFO,
                                        mPullDataAsyncTaskStatus));

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onPostExecute " + mPullDataAsyncTaskStatus.getStatus()
                                                                 .name());
            }

            checkStatusAndStop();
        }

        @Override
        protected void onCancelled() {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCancelled");
            }

            mPullDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.ABORTED,
                                                      getString(R.string.synchro_message_downloading_aborted));
            sendMessage(SyncService.HANDLER_SYNC_STATUS,
                        getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.DOWNLOAD_STATUS,
                                        mPullDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.INFO,
                                        mPullDataAsyncTaskStatus));

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCancelled " + mPullDataAsyncTaskStatus.getStatus()
                                                               .name());
            }

            checkStatusAndStop();
        }

        private boolean copyInputStream(
                InputStream in,
                OutputStream out,
                long contentLength,
                int currentExport,
                int numberOfExports) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "content-length: " + contentLength);
            }

            byte[] buffer = new byte[1024];

            int len;
            long totalBytesRead = 0;

            try {
                while ((len = in.read(buffer)) >= 0) {
                    if (isCancelled()) {
                        return false;
                    }

                    out.write(buffer,
                              0,
                              len);
                    totalBytesRead += len;

                    int currentProgress = (int) (((double) totalBytesRead / (double) contentLength) * 100);
                    sendProgress(SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD,
                                 (int) (((double) currentExport / (double) numberOfExports) * 100) + (currentProgress / numberOfExports),
                                 currentProgress);
                }

                out.flush();

                in.close();
                out.close();
            }
            catch (IOException ioe) {
                Log.w(TAG,
                      ioe.getMessage(),
                      ioe);
                return false;
            }

            return true;
        }
    }

    /**
     * {@code AsyncTask} implementation to upload all saved inputs to the server.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class PushDataAsyncTask
            extends AsyncTask<File, Void, Boolean> {

        private final WebAPIClient webAPIClient;

        public PushDataAsyncTask() {

            this.webAPIClient = new WebAPIClient();
        }

        @Override
        protected Boolean doInBackground(File... params) {

            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.INFO,
                                        new SyncStatus(SyncStatus.Status.RUNNING,
                                                       "starting uploading inputs ...")));

            int currentInput = 0;

            for (File input : params) {
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                            new SyncMessage(SyncMessage.MessageType.INFO,
                                            new SyncStatus(SyncStatus.Status.RUNNING,
                                                           String.format(getString(R.string.synchro_message_uploading),
                                                                         input.getName()))));
                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                            new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                            new SyncStatus(SyncStatus.Status.RUNNING,
                                                           String.format(getString(R.string.synchro_message_uploading),
                                                                         input.getName()))));
                sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD,
                             (int) (((double) currentInput / (double) params.length) * 100),
                             0);

                try {
                    final HttpURLConnection httpURLConnection = this.webAPIClient.post(getSyncSettings().getServerUrl() + getSyncSettings().getImportUrl(),
                                                                                       getSyncSettings().getToken(),
                                                                                       FileUtils.readFileToString(input));
                    httpURLConnection.connect();

                    switch (httpURLConnection.getResponseCode()) {
                        case HttpURLConnection.HTTP_OK:
                            // pulls content stream from response
                            final JSONObject response = this.webAPIClient.readInputStreamAsJson(httpURLConnection.getInputStream());

                            if ((response != null) && (this.webAPIClient.checkStatus(response))) {
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                            new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                                            new SyncStatus(SyncStatus.Status.RUNNING,
                                                                           getString(R.string.synchro_message_uploading_file_ok))));
                                sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                            new SyncMessage(SyncMessage.MessageType.INFO,
                                                            new SyncStatus(SyncStatus.Status.RUNNING,
                                                                           getString(R.string.synchro_message_uploading_file_ok))));
                                // copy file into a new directory with prefix ok for back up
                                SaveFileWithPrefix(input,
                                                   "ok_");
                            }
                            else {
                                if (!isCancelled()) {
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                               String.format(getString(R.string.synchro_message_uploading_file_ko),
                                                                                             input.getName()))));
                                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                                new SyncMessage(SyncMessage.MessageType.ERROR,
                                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                               "failed to upload input '" + input.getName() + "' from URL '" + getSyncSettings().getServerUrl() + getSyncSettings().getImportUrl() + "'")));
                                    // copy file into a new directory with prefix error for back up and delete it
                                    SaveFileWithPrefix(input,
                                                       "ko_");
                                }

                                return false;
                            }

                            break;
                        default:
                            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                        new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                                        new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                       String.format(getString(R.string.synchro_message_uploading_file_ko),
                                                                                     input.getName()))));
                            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                        new SyncMessage(SyncMessage.MessageType.ERROR,
                                                        new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                                       "unable to upload input from URL '" + getSyncSettings().getServerUrl() + getSyncSettings().getImportUrl() + "', HTTP status : " + httpURLConnection.getResponseCode())));
                            // copy file into a new directory with prefix error for back up and delete it
                            SaveFileWithPrefix(input,
                                               "ko_");

                            return false;
                    }
                }
                catch (IOException ioe) {
                    Log.w(TAG,
                          ioe.getMessage(),
                          ioe);
                    sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                                new SyncMessage(SyncMessage.MessageType.WARNING,
                                                new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                               ioe.getLocalizedMessage())));

                    return false;
                }

                currentInput++;
            }

            sendProgress(SyncService.HANDLER_SYNC_PROGRESS_UPLOAD,
                         100,
                         100);

            return true;
        }

        /**
         * Save the File {@code input.json} in a backup directory specify with current date
         *
         * @param file   the file to copy
         * @param prefix the prefix to add to the destination file
         */
        private void SaveFileWithPrefix(
                File file,
                String prefix) throws IOException {

            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd",
                                                               Locale.getDefault());
            String date = dateformat.format(new Date());
            File dest = new File(file.getParent() + "/" + date + "/" + prefix + file.getName());
            FileUtils.copyFile(file,
                               dest);
            FileUtils.deleteQuietly(file);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onPostExecute");
            }

            if (result) {
                mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED,
                                                          getString(R.string.synchro_message_uploading_complete));
            }
            else {
                mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.FINISHED_WITH_ERRORS,
                                                          getString(R.string.synchro_message_uploading_complete_with_errors));
            }

            sendMessage(SyncService.HANDLER_SYNC_STATUS,
                        getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                        mPushDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.INFO,
                                        mPushDataAsyncTaskStatus));

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onPostExecute " + mPushDataAsyncTaskStatus.getStatus()
                                                                 .name());
            }

            checkStatusAndStop();
        }

        @Override
        protected void onCancelled() {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCancelled");
            }

            mPushDataAsyncTaskStatus = new SyncStatus(SyncStatus.Status.ABORTED,
                                                      "aborted");
            sendMessage(SyncService.HANDLER_SYNC_STATUS,
                        getSyncStatus());
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.UPLOAD_STATUS,
                                        mPushDataAsyncTaskStatus));
            sendMessage(SyncService.HANDLER_SYNC_MESSAGE,
                        new SyncMessage(SyncMessage.MessageType.INFO,
                                        mPushDataAsyncTaskStatus));

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCancelled " + mPushDataAsyncTaskStatus.getStatus()
                                                               .name());
            }

            checkStatusAndStop();
        }
    }
}
