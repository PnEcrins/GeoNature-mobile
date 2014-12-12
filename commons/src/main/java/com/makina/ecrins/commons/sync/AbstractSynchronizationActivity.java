package com.makina.ecrins.commons.sync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Synchronization view.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractSynchronizationActivity extends ActionBarActivity {
    private static final String ALERT_DIALOG_FRAGMENT = "alert_dialog_cancel_input";

    protected static final String KEY_SERVICE_INITIALIZED = "service_initialized";
    protected static final String KEY_SERVER_STATUS = "server_status";
    protected static final String KEY_SYNC_STATUS = "sync_status";
    protected static final String KEY_SYNC_PULL_DATA_MESSAGE = "pull_data_message";
    protected static final String KEY_SYNC_PUSH_DATA_MESSAGE = "push_data_message";

    private static final Animation sAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);

    static {
        sAlphaAnimation.setDuration(250);
        sAlphaAnimation.setStartOffset(10);
        sAlphaAnimation.setRepeatMode(Animation.REVERSE);
        sAlphaAnimation.setRepeatCount(Animation.INFINITE);
    }

    /**
     * Messenger for communicating with service.
     */
    protected Messenger mSyncServiceMessenger = null;

    /**
     * Target we publish for clients to send messages to {@link com.makina.ecrins.commons.sync.AbstractSynchronizationActivity.SynchronizationActivityHandler}.
     */
    private final Messenger mMessenger = new Messenger(new SynchronizationActivityHandler(this));

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean mIsSyncServiceBound;

    protected final Deque<Message> messagesQueue = new ArrayDeque<>();

    protected Bundle mSavedState;
    protected TextView mTextViewServerStatus;
    protected ProgressBar mProgressBarServerToDevice;
    protected TextView mTextViewProgressServerToDevice;
    protected ProgressBar mProgressBarDeviceToServer;
    protected TextView mTextViewProgressDeviceToServer;

    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(AbstractSynchronizationActivity.class.getName(), "onServiceConnected " + name);

            if (name.getClassName().equals(CheckServerService.class.getName())) {
                Messenger messenger = new Messenger(service);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(AbstractSynchronizationActivity.this, StartCheckServerServiceReceiver.class);
                intent.putExtra(StartCheckServerServiceReceiver.INTENT_MESSENGER, messenger);

                PendingIntent pending = PendingIntent.getBroadcast(AbstractSynchronizationActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // starts 2 seconds after resume
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 2);

                // repeats every 5 seconds
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, pending);
            }

            if (name.getClassName().equals(SyncService.class.getName())) {
                // This is called when the connection with the service has been established, giving us the service object we can use to interact with the service.
                // We are communicating with our service through an IDL interface, so get a client-side representation of that from the raw service object.
                mSyncServiceMessenger = new Messenger(service);

                // We want to monitor the service for as long as we are connected to it.
                try {
                    Message msg = Message.obtain(null, SyncService.HANDLER_REGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mSyncServiceMessenger.send(msg);
                }
                catch (RemoteException re) {
                    // In this case the service has crashed before we could even do anything with it.
                    // We can count on soon being disconnected (and then reconnected if it can be restarted) so there is no need to do anything here.
                    Log.w(AbstractSynchronizationActivity.class.getName(), re.getMessage(), re);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is, its process crashed.

            Log.d(AbstractSynchronizationActivity.class.getName(), "onServiceDisconnected " + name);

            if (name.getClassName().equals(SyncService.class.getName())) {
                mSyncServiceMessenger = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchro);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Log.d(AbstractSynchronizationActivity.class.getName(), "onCreate, savedInstanceState null");
            mSavedState = new Bundle();
        }
        else {
            Log.d(AbstractSynchronizationActivity.class.getName(), "onCreate, savedInstanceState initialized");
            mSavedState = savedInstanceState;
        }

        // starts the service if needed before binding on it
        if (!mSavedState.containsKey(KEY_SERVICE_INITIALIZED)) {
            startService(new Intent(this, SyncService.class));
            mSavedState.putBoolean(KEY_SERVICE_INITIALIZED, true);
        }

        mTextViewServerStatus = (TextView) findViewById(R.id.textViewServerStatus);
        mProgressBarServerToDevice = (ProgressBar) findViewById(R.id.progressBarServerToDevice);
        mProgressBarServerToDevice.setMax(100);
        mTextViewProgressServerToDevice = (TextView) findViewById(R.id.textViewProgressServerToDevice);
        mProgressBarDeviceToServer = (ProgressBar) findViewById(R.id.progressBarDeviceToServer);
        mProgressBarDeviceToServer.setMax(100);
        mTextViewProgressDeviceToServer = (TextView) findViewById(R.id.textViewProgressDeviceToServer);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(AbstractSynchronizationActivity.class.getName(), "onResume");

        setTextViewServerStatus();
        setTextViewProgress();

        Intent checkServerServiceIntent = new Intent(this, CheckServerService.class);
        checkServerServiceIntent.putExtra(CheckServerService.INTENT_EXTRA_SYNC_SETTINGS, getSyncSettings());

        // creates a new Messenger for the communication back from CheckServerService to the Activity
        Messenger checkServerServiceMessenger = new Messenger(new SynchronizationActivityHandler(this));
        checkServerServiceIntent.putExtra(CheckServerService.INTENT_EXTRA_MESSENGER, checkServerServiceMessenger);

        bindService(checkServerServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        doBindService();
    }

    @Override
    protected void onPause() {
        Log.d(AbstractSynchronizationActivity.class.getName(), "onPause");

        doUnbindService();

        // canceling alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AbstractSynchronizationActivity.this, StartCheckServerServiceReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(AbstractSynchronizationActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pending);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(AbstractSynchronizationActivity.class.getName(), "onDestroy");

        if (isFinishing()) {
            mSavedState.remove(KEY_SERVICE_INITIALIZED);
            mSavedState.remove(KEY_SYNC_STATUS);
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSavedState.containsKey(KEY_SYNC_STATUS) && ((SyncStatus) mSavedState.getParcelable(KEY_SYNC_STATUS)).getStatus().equals(SyncStatus.Status.FINISHED)) {
            finish();
        }
        else {
            showConfirmDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (((SyncStatus) mSavedState.getParcelable(KEY_SYNC_STATUS)).getStatus().equals(SyncStatus.Status.FINISHED)) {
                    finish();
                }
                else {
                    showConfirmDialog();
                }

                return true;
            default:
                return false;
        }
    }

    protected void setTextViewServerStatus() {
        switch (mSavedState.getInt(KEY_SERVER_STATUS)) {
            case CheckServerService.HANDLER_SYNC_SERVER_STATUS_PENDING:
                mTextViewServerStatus.setText(R.string.synchro_server_status_pending);
                mTextViewServerStatus.setTextColor(getResources().getColor(R.color.status_pending));
                mTextViewServerStatus.startAnimation(sAlphaAnimation);
                break;
            case CheckServerService.HANDLER_SYNC_SERVER_STATUS_OK:
                mTextViewServerStatus.setText(R.string.synchro_server_status_ok);
                mTextViewServerStatus.setTextColor(getResources().getColor(R.color.status_ok));
                mTextViewServerStatus.clearAnimation();
                break;
            case CheckServerService.HANDLER_SYNC_SERVER_STATUS_KO:
                mTextViewServerStatus.setText(R.string.synchro_server_status_ko);
                mTextViewServerStatus.setTextColor(getResources().getColor(R.color.status_ko));
                mTextViewServerStatus.clearAnimation();
                break;
        }
    }

    protected void setTextViewProgress() {
        if (mSavedState.containsKey(KEY_SYNC_PULL_DATA_MESSAGE)) {
            SyncMessage syncMessage = mSavedState.getParcelable(KEY_SYNC_PULL_DATA_MESSAGE);

            switch (syncMessage.getMessageType()) {
                case DOWNLOAD_STATUS:
                    mTextViewProgressServerToDevice.setText(syncMessage.getSyncStatus().getMessage());
                    break;
                default:
            }
        }

        if (mSavedState.containsKey(KEY_SYNC_PUSH_DATA_MESSAGE)) {
            SyncMessage syncMessage = mSavedState.getParcelable(KEY_SYNC_PUSH_DATA_MESSAGE);

            switch (syncMessage.getMessageType()) {
                case UPLOAD_STATUS:
                    mTextViewProgressDeviceToServer.setText(syncMessage.getSyncStatus().getMessage());
                    break;
                default:
            }
        }
    }

    protected abstract SyncSettings getSyncSettings();

    private void showConfirmDialog() {
        final DialogFragment dialogFragment = AlertDialogFragment.newInstance(
                R.string.alert_dialog_confirm_cancel_synchro_title,
                R.string.alert_dialog_confirm_cancel_synchro_message,
                new AlertDialogFragment.OnAlertDialogListener() {
                    @Override
                    public void onPositiveButtonListener(DialogInterface dialog) {
                        Message message = Message.obtain();
                        message.what = SyncService.HANDLER_SYNC_CANCEL;

                        if (mSyncServiceMessenger != null) {
                            try {
                                mSyncServiceMessenger.send(message);
                            }
                            catch (RemoteException re) {
                                Log.w(
                                        AbstractSynchronizationActivity.class.getName(),
                                        re.getMessage(),
                                        re
                                );
                            }
                        }
                        else {
                            messagesQueue.add(message);
                        }

                        finish();
                    }

                    @Override
                    public void onNegativeButtonListener(DialogInterface dialog) {
                        // nothing to do ...
                    }
                }
        );
        dialogFragment.show(getSupportFragmentManager(), ALERT_DIALOG_FRAGMENT);
    }

    /**
     * Establish a connection with the service.
     */
    private void doBindService() {
        Intent syncServiceIntent = new Intent(this, SyncService.class);
        syncServiceIntent.putExtra(SyncService.INTENT_EXTRA_SYNC_SETTINGS, getSyncSettings());

        mIsSyncServiceBound = bindService(syncServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindService() {
        if (mIsSyncServiceBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mSyncServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, SyncService.HANDLER_UNREGISTER_CLIENT);
                    // this is needed to unregister the message used by this client
                    msg.replyTo = mMessenger;
                    mSyncServiceMessenger.send(msg);
                }
                catch (RemoteException re) {
                    // There is nothing special we need to do if the service has crashed.
                    Log.w(AbstractSynchronizationActivity.class.getName(), re.getMessage(), re);
                }
            }

            // Detach our existing connection.
            unbindService(mServiceConnection);
            mIsSyncServiceBound = false;
        }
    }

    /**
     * Handler of incoming messages from services.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class SynchronizationActivityHandler extends Handler {
        private final WeakReference<AbstractSynchronizationActivity> mSynchronizationActivity;

        public SynchronizationActivityHandler(AbstractSynchronizationActivity pSynchronizationActivity) {
            super();
            mSynchronizationActivity = new WeakReference<>(pSynchronizationActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            AbstractSynchronizationActivity synchronizationActivity = mSynchronizationActivity.get();

            switch (msg.what) {
                case CheckServerService.HANDLER_SYNC_SERVER_STATUS_PENDING:

                    if (synchronizationActivity.mSavedState.getInt(KEY_SERVER_STATUS) != CheckServerService.HANDLER_SYNC_SERVER_STATUS_OK) {
                        synchronizationActivity.mSavedState.putInt(KEY_SERVER_STATUS, msg.what);
                        synchronizationActivity.setTextViewServerStatus();
                    }

                    break;
                case CheckServerService.HANDLER_SYNC_SERVER_STATUS_OK:

                    if (synchronizationActivity.mSavedState.getInt(KEY_SERVER_STATUS) != CheckServerService.HANDLER_SYNC_SERVER_STATUS_OK) {
                        synchronizationActivity.mSavedState.putInt(KEY_SERVER_STATUS, msg.what);
                        synchronizationActivity.setTextViewServerStatus();

                        Message messagePushData = Message.obtain();
                        messagePushData.what = SyncService.HANDLER_SYNC_PUSH_DATA;

                        // performs upload task
                        if (synchronizationActivity.mSyncServiceMessenger != null) {
                            try {
                                synchronizationActivity.mSyncServiceMessenger.send(messagePushData);
                            }
                            catch (RemoteException re) {
                                Log.w(getClass().getName(), re.getMessage(), re);
                            }
                        }
                        else {
                            synchronizationActivity.messagesQueue.add(messagePushData);
                        }
                    }

                    break;
                case CheckServerService.HANDLER_SYNC_SERVER_STATUS_KO:
                    synchronizationActivity.mSavedState.putInt(KEY_SERVER_STATUS, msg.what);
                    synchronizationActivity.setTextViewServerStatus();
                    break;
                case SyncService.HANDLER_CLIENT_REGISTERED:
                    // tries to send all awaiting messages
                    while (!synchronizationActivity.messagesQueue.isEmpty()) {
                        try {
                            synchronizationActivity.mSyncServiceMessenger.send(synchronizationActivity.messagesQueue.removeFirst());
                        }
                        catch (RemoteException | NoSuchElementException ge) {
                            Log.w(getClass().getName(), ge.getMessage(), ge);
                        }
                    }

                    break;
                case SyncService.HANDLER_SYNC_STATUS:
                    SyncStatus status = (SyncStatus) msg.obj;
                    Log.d(getClass().getName(), "HANDLER_SYNC_STATUS : " + status.getStatus().name());
                    synchronizationActivity.mSavedState.putParcelable(KEY_SYNC_STATUS, status);

                    if (status.getStatus().equals(SyncStatus.Status.FINISHED)) {
                        synchronizationActivity.mProgressBarServerToDevice.setProgress(100);
                        synchronizationActivity.mProgressBarDeviceToServer.setProgress(100);
                    }

                    break;
                case SyncService.HANDLER_SYNC_MESSAGE:
                    SyncMessage syncMessage = (SyncMessage) msg.obj;

                    switch (syncMessage.getMessageType()) {
                        case DOWNLOAD_STATUS:
                            synchronizationActivity.mSavedState.putParcelable(KEY_SYNC_PULL_DATA_MESSAGE, syncMessage);
                            synchronizationActivity.setTextViewProgress();
                            break;
                        case UPLOAD_STATUS:
                            Log.d(getClass().getName(), "HANDLER_SYNC_MESSAGE UPLOAD_STATUS " + syncMessage.getSyncStatus().getStatus().name());

                            synchronizationActivity.mSavedState.putParcelable(KEY_SYNC_PUSH_DATA_MESSAGE, syncMessage);
                            synchronizationActivity.setTextViewProgress();

                            // performs download task
                            if (syncMessage.getSyncStatus().getStatus().equals(SyncStatus.Status.FINISHED) || syncMessage.getSyncStatus().getStatus().equals(SyncStatus.Status.FINISHED_WITH_ERRORS)) {
                                Message messagePullData = Message.obtain();
                                messagePullData.what = SyncService.HANDLER_SYNC_PULL_DATA;

                                if (synchronizationActivity.mSyncServiceMessenger != null) {
                                    Log.d(getClass().getName(), "send message HANDLER_SYNC_PULL_DATA");

                                    try {
                                        synchronizationActivity.mSyncServiceMessenger.send(messagePullData);
                                    }
                                    catch (RemoteException re) {
                                        Log.w(getClass().getName(), re.getMessage(), re);
                                    }
                                }
                                else {
                                    Log.d(getClass().getName(), "mSyncServiceMessenger null : add message HANDLER_SYNC_PULL_DATA");
                                    synchronizationActivity.messagesQueue.add(messagePullData);
                                }
                            }

                            break;
                        default:
                            Log.d(getClass().getName(), "HANDLER_SYNC_MESSAGE : " + syncMessage.getSyncStatus().getMessage());
                            break;
                    }

                    break;
                case SyncService.HANDLER_SYNC_PROGRESS_DOWNLOAD:
                    synchronizationActivity.mProgressBarServerToDevice.setProgress(msg.arg1);
                    synchronizationActivity.mProgressBarServerToDevice.setSecondaryProgress(msg.arg2);
                    break;
                case SyncService.HANDLER_SYNC_PROGRESS_UPLOAD:
                    Log.d(getClass().getName(), "HANDLER_SYNC_MESSAGE HANDLER_SYNC_PROGRESS_UPLOAD : " + msg.arg1 + " " + msg.arg2);
                    synchronizationActivity.mProgressBarDeviceToServer.setProgress(msg.arg1);
                    synchronizationActivity.mProgressBarDeviceToServer.setSecondaryProgress(msg.arg2);
                    break;
            }
        }
    }
}
