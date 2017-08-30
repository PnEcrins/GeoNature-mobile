package com.makina.ecrins.commons.settings;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <Code>Service</code> implementation to execute in background {@link com.makina.ecrins.commons.settings.AbstractSettingsService.LoadSettingsFromFileAsyncTask} implementation.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @deprecated use instead {@link AbstractAppSettingsIntentService}
 */
@Deprecated
public abstract class AbstractSettingsService
        extends Service {

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
     * Command to the service to retrieve all pending messages.
     */
    public static final int HANDLER_GET_PENDING_MESSAGES = 2;

    /**
     * Command to the service to start load local settings.
     */
    public static final int HANDLER_LOAD_SETTINGS = 3;

    /**
     * Command to the service to start additional {@link IServiceMessageStatusTask}.
     */
    public static final int HANDLER_EXECUTE_TASK = 4;

    /**
     * Command to the service to get the current status.
     */
    public static final int HANDLER_STATUS = 5;

    /**
     * Command to send message that a given client was successfully registered.
     */
    public static final int HANDLER_CLIENT_REGISTERED = 6;

    protected LoadSettingsFromFileAsyncTask mLoadSettingsFromFileAsyncTask = null;

    private final AtomicBoolean mSettingsTaskInvoked = new AtomicBoolean();
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
    private ServiceStatus mServiceStatus = new ServiceStatus(ServiceStatus.Status.PENDING,
                                                             "");

    /**
     * keeps track of all current registered clients.
     */
    protected final List<Messenger> mClients = new ArrayList<>();

    protected final Deque<Message> mMessagesQueue = new ArrayDeque<>();

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mInMessenger = new Messenger(new IncomingHandler(this));

    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(getClass().getName(),
              "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(getClass().getName(),
              "onBind " + intent);

        return mInMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.d(getClass().getName(),
              "onUnbind " + intent);

        return true;
    }

    @Override
    public void onDestroy() {

        Log.d(getClass().getName(),
              "onDestroy");

        super.onDestroy();
    }

    /**
     * Gets a new instance of {@link com.makina.ecrins.commons.settings.AbstractSettingsService.LoadSettingsFromFileAsyncTask} or a previously created instance.
     *
     * @return a new instance of {@link com.makina.ecrins.commons.settings.AbstractSettingsService.LoadSettingsFromFileAsyncTask}
     */
    protected LoadSettingsFromFileAsyncTask getLoadSettingsFromFileAsyncTask() {

        if (mLoadSettingsFromFileAsyncTask == null) {
            Log.d(getClass().getName(),
                  "getLoadSettingsFromFileAsyncTask : create new instance");
            mLoadSettingsFromFileAsyncTask = new LoadSettingsFromFileAsyncTask(this);
        }
        else {
            Log.d(getClass().getName(),
                  "getLoadSettingsFromFileAsyncTask : initialized");
        }

        return mLoadSettingsFromFileAsyncTask;
    }

    protected abstract IServiceMessageStatusTask getServiceMessageStatusTask();

    protected abstract void executeServiceMessageStatusTask();

    /**
     * Sends a progress message to this Messenger's Handler.
     *
     * @param what     custom message code so that the recipient can identify what this message is about
     * @param progress the current progress as a value
     * @param max      the max value
     */
    protected void sendProgress(
            int what,
            int progress,
            int max) {

        Message message = Message.obtain(null,
                                         what,
                                         progress,
                                         max);

        if (mClients.isEmpty()) {
            mMessagesQueue.add(message);
        }
        else {
            for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                    mClients.get(i)
                            .send(message);
                }
                catch (RemoteException re) {
                    // The client is dead.
                    // Remove it from the list.
                    // We are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
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

        Message message = Message.obtain(null,
                                         what);

        if (obj != null) {
            message.obj = obj;
        }

        if (mClients.isEmpty()) {
            mMessagesQueue.add(message);
        }
        else {
            for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                    mClients.get(i)
                            .send(message);
                }
                catch (RemoteException re) {
                    // The client is dead.
                    // Remove it from the list.
                    // We are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }
        }
    }

    /**
     * Sends a message to this Messenger's Handler.
     *
     * @param message the {@link android.os.Message} instance to send to the recipient
     */
    protected void sendMessage(Message message) {

        if (mClients.isEmpty()) {
            mMessagesQueue.add(message);
        }
        else {
            for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                    mClients.get(i)
                            .send(message);
                }
                catch (RemoteException re) {
                    // The client is dead.
                    // Remove it from the list.
                    // We are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }
        }
    }

    /**
     * Gets the current status of this service.
     *
     * @return {@link ServiceStatus} instance describing the current status
     */
    protected ServiceStatus getServiceStatus() {

        if (getServiceMessageStatusTask() == null) {
            Log.d(getClass().getName(),
                  "getServiceStatus : " + getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                                            .getStatus()
                                                                            .name());
            mServiceStatus = getLoadSettingsFromFileAsyncTask().getServiceStatus();
        }
        else {
            Log.d(getClass().getName(),
                  "getServiceStatus : " + getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                                            .getStatus()
                                                                            .name() + " " + getServiceMessageStatusTask().getServiceStatus()
                                                                                                                         .getStatus()
                                                                                                                         .name());

            mServiceStatus = new ServiceStatus(ServiceStatus.Status.PENDING,
                                               ServiceStatus.Status.PENDING.name());

            if (getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                  .getStatus()
                                                  .equals(ServiceStatus.Status.RUNNING) || getServiceMessageStatusTask().getServiceStatus()
                                                                                                                        .getStatus()
                                                                                                                        .equals(ServiceStatus.Status.RUNNING)) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.RUNNING,
                                                   ServiceStatus.Status.RUNNING.name());
            }

            if (getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                  .getStatus()
                                                  .equals(ServiceStatus.Status.ABORTED) || getServiceMessageStatusTask().getServiceStatus()
                                                                                                                        .getStatus()
                                                                                                                        .equals(ServiceStatus.Status.ABORTED)) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.ABORTED,
                                                   ServiceStatus.Status.ABORTED.name());
            }

            if (getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                  .getStatus()
                                                  .equals(ServiceStatus.Status.FINISHED) && getServiceMessageStatusTask().getServiceStatus()
                                                                                                                         .getStatus()
                                                                                                                         .equals(ServiceStatus.Status.FINISHED_WITH_ERRORS)) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED_WITH_ERRORS,
                                                   ServiceStatus.Status.FINISHED_WITH_ERRORS.name());
            }

            if (getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                  .getStatus()
                                                  .equals(ServiceStatus.Status.FINISHED_WITH_ERRORS) && getServiceMessageStatusTask().getServiceStatus()
                                                                                                                                     .getStatus()
                                                                                                                                     .equals(ServiceStatus.Status.FINISHED)) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED_WITH_ERRORS,
                                                   ServiceStatus.Status.FINISHED_WITH_ERRORS.name());
            }

            if (getLoadSettingsFromFileAsyncTask().getServiceStatus()
                                                  .getStatus()
                                                  .equals(ServiceStatus.Status.FINISHED) && getServiceMessageStatusTask().getServiceStatus()
                                                                                                                         .getStatus()
                                                                                                                         .equals(ServiceStatus.Status.FINISHED)) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED,
                                                   ServiceStatus.Status.FINISHED.name());
            }
        }

        return mServiceStatus;
    }

    protected abstract String getSettingsFilename();

    protected abstract AbstractAppSettings getSettingsFromJsonObject(JSONObject settingsJsonObject) throws JSONException;

    protected abstract int whatSettingsLoadingStart();

    protected abstract int whatSettingsLoading();

    protected abstract int whatSettingsLoadingFailed();

    protected abstract int whatSettingsLoadingLoaded();

    /**
     * Checks the current status before stopping the service.
     */
    protected void checkStatusAndStop() {

        if (mMessagesQueue.isEmpty() && (mServiceStatus.getStatus()
                                                       .equals(ServiceStatus.Status.ABORTED) || mServiceStatus.getStatus()
                                                                                                              .equals(ServiceStatus.Status.FINISHED) || mServiceStatus.getStatus()
                                                                                                                                                                      .equals(ServiceStatus.Status.FINISHED_WITH_ERRORS))) {
            Log.d(getClass().getName(),
                  "stopSelf");

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

        private final WeakReference<AbstractSettingsService> mAbstractSettingsService;

        public IncomingHandler(AbstractSettingsService pAbstractSettingsService) {

            super();
            mAbstractSettingsService = new WeakReference<>(pAbstractSettingsService);
        }

        @Override
        public void handleMessage(Message msg) {

            AbstractSettingsService abstractSettingsService = mAbstractSettingsService.get();

            switch (msg.what) {
                case AbstractSettingsService.HANDLER_REGISTER_CLIENT:
                    Log.d(getClass().getName(),
                          "handleMessage HANDLER_REGISTER_CLIENT");

                    if (abstractSettingsService.mClients.add(msg.replyTo)) {
                        abstractSettingsService.sendMessage(AbstractSettingsService.HANDLER_STATUS,
                                                            abstractSettingsService.getServiceStatus());
                        abstractSettingsService.sendMessage(AbstractSettingsService.HANDLER_CLIENT_REGISTERED,
                                                            msg.replyTo);
                    }
                    else {
                        Log.w(getClass().getName(),
                              "AbstractSettingsService.HANDLER_REGISTER_CLIENT : failed to register client");
                    }

                    break;
                case AbstractSettingsService.HANDLER_UNREGISTER_CLIENT:
                    Log.d(getClass().getName(),
                          "handleMessage HANDLER_UNREGISTER_CLIENT");
                    abstractSettingsService.mClients.remove(msg.replyTo);
                    break;
                case AbstractSettingsService.HANDLER_GET_PENDING_MESSAGES:
                    Log.d(getClass().getName(),
                          "handleMessage HANDLER_GET_PENDING_MESSAGES " + abstractSettingsService.mMessagesQueue.size());

                    // tries to send all awaiting messages
                    while (!abstractSettingsService.mMessagesQueue.isEmpty()) {
                        abstractSettingsService.sendMessage(abstractSettingsService.mMessagesQueue.removeFirst());
                    }

                    break;
                case AbstractSettingsService.HANDLER_LOAD_SETTINGS:
                    Log.d(getClass().getName(),
                          "handleMessage HANDLER_LOAD_SETTINGS " + abstractSettingsService.mSettingsTaskInvoked.get() + " " + abstractSettingsService.getLoadSettingsFromFileAsyncTask()
                                                                                                                                                     .getServiceStatus()
                                                                                                                                                     .getStatus()
                                                                                                                                                     .name());

                    if ((!abstractSettingsService.mSettingsTaskInvoked.getAndSet(true)) && abstractSettingsService.getLoadSettingsFromFileAsyncTask()
                                                                                                                  .getServiceStatus()
                                                                                                                  .getStatus()
                                                                                                                  .equals(ServiceStatus.Status.PENDING)) {
                        Log.d(getClass().getName(),
                              "handleMessage HANDLER_LOAD_SETTINGS execute");
                        abstractSettingsService.sendMessage(AbstractSettingsService.HANDLER_STATUS,
                                                            abstractSettingsService.getServiceStatus());
                        abstractSettingsService.getLoadSettingsFromFileAsyncTask()
                                               .execute(abstractSettingsService.getSettingsFilename());
                    }
                    else {
                        Log.d(getClass().getName(),
                              "handleMessage HANDLER_LOAD_SETTINGS already invoked");
                    }

                    break;
                case AbstractSettingsService.HANDLER_EXECUTE_TASK:
                    if (!abstractSettingsService.mTaskInvoked.getAndSet(true) && (abstractSettingsService.getServiceMessageStatusTask() != null) && abstractSettingsService.getServiceMessageStatusTask()
                                                                                                                                                                           .getServiceStatus()
                                                                                                                                                                           .getStatus()
                                                                                                                                                                           .equals(ServiceStatus.Status.PENDING)) {
                        Log.d(getClass().getName(),
                              "handleMessage HANDLER_EXECUTE_TASK execute");
                        abstractSettingsService.sendMessage(AbstractSettingsService.HANDLER_STATUS,
                                                            abstractSettingsService.getServiceStatus());
                        abstractSettingsService.executeServiceMessageStatusTask();
                    }
                    else {
                        Log.d(getClass().getName(),
                              "handleMessage HANDLER_EXECUTE_TASK already invoked");
                    }

                    break;
            }
        }
    }

    /**
     * <code>AsyncTask</code> implementation to load global application settings from a JSON file.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class LoadSettingsFromFileAsyncTask
            extends AsyncTask<String, Void, AbstractAppSettings>
            implements IServiceMessageStatusTask {

        private volatile ServiceStatus mServiceStatus = new ServiceStatus(ServiceStatus.Status.PENDING,
                                                                          "");
        private Context mContext;
        private String mFilename;

        public LoadSettingsFromFileAsyncTask(Context pContext) {

            super();
            this.mContext = pContext;
            this.mFilename = "";
        }

        @Override
        public ServiceStatus getServiceStatus() {

            return mServiceStatus;
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected AbstractAppSettings doInBackground(String... params) {

            this.mFilename = params[0];

            Log.d(AbstractSettingsService.class.getName(),
                  "loading global settings from '" + mFilename + "' ...");

            mServiceStatus = new ServiceStatus(ServiceStatus.Status.RUNNING,
                                               "");
            sendMessage(whatSettingsLoadingStart(),
                        null);

            try {
                // noinspection ResultOfMethodCallIgnored
                FileUtils.getRootFolder(mContext,
                                        MountPoint.StorageType.INTERNAL)
                         .mkdirs();

                File settingsJsonFile = FileUtils.getFile(FileUtils.getRootFolder(mContext,
                                                                                  MountPoint.StorageType.INTERNAL),
                                                          this.mFilename);

                if (settingsJsonFile.exists()) {
                    Log.d(AbstractSettingsService.class.getName(),
                          "load file '" + settingsJsonFile.getPath() + "'");

                    String settingsAsJsonString = FileUtils.readFileToString(settingsJsonFile);
                    JSONObject settingsJsonObject = new JSONObject(settingsAsJsonString);

                    return getSettingsFromJsonObject(settingsJsonObject);
                }
                else {
                    Log.w(AbstractSettingsService.class.getName(),
                          "unable to load file from path '" + settingsJsonFile.getPath() + "'");
                }
            }
            catch (IOException | JSONException ge) {
                Log.w(AbstractSettingsService.class.getName(),
                      ge.getMessage(),
                      ge);
            }

            return null;
        }

        @Override
        protected void onPostExecute(AbstractAppSettings result) {

            if (result == null) {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED_WITH_ERRORS,
                                                   this.mFilename);
                sendMessage(whatSettingsLoadingFailed(),
                            this.mFilename);
            }
            else {
                mServiceStatus = new ServiceStatus(ServiceStatus.Status.FINISHED,
                                                   "");
                sendMessage(whatSettingsLoadingLoaded(),
                            result);
            }

            sendMessage(AbstractSettingsService.HANDLER_STATUS,
                        AbstractSettingsService.this.getServiceStatus());
            checkStatusAndStop();
        }
    }
}
