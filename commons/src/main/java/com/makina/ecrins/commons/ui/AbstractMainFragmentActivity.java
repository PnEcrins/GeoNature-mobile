package com.makina.ecrins.commons.ui;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.net.NetworkConnectivityListener;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractSettingsService;
import com.makina.ecrins.commons.settings.ServiceStatus;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;
import com.makina.ecrins.commons.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the main <code>Activity</code> of this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractMainFragmentActivity
        extends AppCompatActivity
        implements OnClickListener,
                   OnItemLongClickListener,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ALERT_DIALOG_DELETE_INPUTS_FRAGMENT = "alert_dialog_delete_inputs";

    protected static final String KEY_SERVICE_STATUS = "service_status";
    private static final String KEY_SERVICE_INITIALIZED = "service_initialized";
    private static final String KEY_SELECTED_OBSERVER = "selected_observer";

    private static final String PROGRESS_DIALOG_SETTINGS_FRAGMENT = "progress_dialog_settings";

    protected final TreeSet<String> mDialogTags = new TreeSet<>();

    /**
     * Messenger for communicating with service.
     */
    protected Messenger mSettingsServiceMessenger = null;

    /**
     * Target we publish for clients to send messages to {@link MainFragmentHandler}.
     */
    private final Messenger mMessenger = new Messenger(new MainFragmentHandler(this));

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean mIsSettingsServiceBound;

    private final AtomicBoolean mIsActivityCreated = new AtomicBoolean();

    protected final Deque<Message> messagesQueue = new ArrayDeque<>();

    private Bundle mSavedState;

    protected NetworkConnectivityListener mNetworkConnectivityListener;

    private Button mButtonStartInput;
    private Button mButtonStartSynchronization;
    private ListView mListViewDeviceStatus;
    protected DeviceStatusAdapter mDeviceStatusAdapter;

    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(AbstractMainFragmentActivity.class.getName(), "onServiceConnected " + name);

            // This is called when the connection with the service has been established, giving us the service object we can use to interact with the service.
            // We are communicating with our service through an IDL interface, so get a client-side representation of that from the raw service object.
            mSettingsServiceMessenger = new Messenger(service);

            // We want to monitor the service for as long as we are connected to it.
            try {
                Message msg = Message.obtain(null, AbstractSettingsService.HANDLER_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mSettingsServiceMessenger.send(msg);
            }
            catch (RemoteException re) {
                // In this case the service has crashed before we could even do anything with it.
                // We can count on soon being disconnected (and then reconnected if it can be restarted) so there is no need to do anything here.
                Log.w(AbstractMainFragmentActivity.class.getName(), re.getMessage(), re);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is, its process crashed.

            Log.d(AbstractMainFragmentActivity.class.getName(), "onServiceDisconnected " + name);

            mSettingsServiceMessenger = null;
        }
    };

    private AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {
            try {
                if (FileUtils.deleteQuietly(FileUtils.getInputsFolder(AbstractMainFragmentActivity.this))) {
                    executeGetDeviceStatusAsyncTask();

                }
            }
            catch (IOException ioe) {
                Log.w(
                        AbstractMainFragmentActivity.class.getName(),
                        ioe.getMessage(),
                        ioe
                );
            }
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            // nothing to do ...
        }
    };

    private NetworkConnectivityListener.OnNetworkConnectivityChangeListener mOnNetworkConnectivityChangeListener = new NetworkConnectivityListener.OnNetworkConnectivityChangeListener() {

        @Override
        public void onNetworkConnectivityChange(@Nullable NetworkInfo networkInfo) {
            mButtonStartSynchronization.setEnabled((networkInfo != null) && networkInfo.isConnected());
        }
    };

    private static class MainFragmentHandler extends Handler {
        private final WeakReference<AbstractMainFragmentActivity> mMainFragmentActivity;

        public MainFragmentHandler(AbstractMainFragmentActivity pMainFragmentActivity) {
            super();
            mMainFragmentActivity = new WeakReference<>(pMainFragmentActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            AbstractMainFragmentActivity mainFragmentActivity = mMainFragmentActivity.get();

            if (msg.what == AbstractSettingsService.HANDLER_CLIENT_REGISTERED) {
                Log.d(getClass().getName(), "handleMessage HANDLER_CLIENT_REGISTERED " + mainFragmentActivity.messagesQueue.size());

                // tries to retrieve all pending messages
                Message messageGetPendingMessages = Message.obtain();
                messageGetPendingMessages.what = AbstractSettingsService.HANDLER_GET_PENDING_MESSAGES;

                if (mainFragmentActivity.mSettingsServiceMessenger != null) {
                    Log.d(getClass().getName(), "send message HANDLER_GET_PENDING_MESSAGES");

                    try {
                        mainFragmentActivity.mSettingsServiceMessenger.send(messageGetPendingMessages);
                    }
                    catch (RemoteException re) {
                        Log.w(getClass().getName(), re.getMessage(), re);
                    }
                }
                else {
                    Log.d(getClass().getName(), "mSyncServiceMessenger null : add message HANDLER_GET_PENDING_MESSAGES");
                    mainFragmentActivity.messagesQueue.add(messageGetPendingMessages);
                }

                // tries to send all awaiting messages
                while (!mainFragmentActivity.messagesQueue.isEmpty()) {
                    try {
                        mainFragmentActivity.mSettingsServiceMessenger.send(mainFragmentActivity.messagesQueue.removeFirst());
                    }
                    catch (RemoteException | NoSuchElementException ge) {
                        Log.w(getClass().getName(), ge.getMessage(), ge);
                    }
                }
            }
            else if (msg.what == AbstractSettingsService.HANDLER_STATUS) {
                ServiceStatus status = (ServiceStatus) msg.obj;
                Log.d(getClass().getName(), "HANDLER_STATUS : " + status.getStatus().name());
                mainFragmentActivity.mSavedState.putParcelable(KEY_SERVICE_STATUS, status);

                if (status.getStatus().equals(ServiceStatus.Status.FINISHED)) {
                    mainFragmentActivity.mButtonStartInput.setEnabled(true);

                    for (String dialogTag : new ArrayList<>(mainFragmentActivity.mDialogTags)) {
                        mainFragmentActivity.dismissProgressDialog(dialogTag);
                    }

                    mainFragmentActivity.startListeningNetworkConnectivity();
                }
            }
            else if (msg.what == mainFragmentActivity.whatSettingsLoadingStart()) {
                mainFragmentActivity.showProgressDialog(
                        PROGRESS_DIALOG_SETTINGS_FRAGMENT,
                        R.string.progress_title,
                        R.string.progress_message_loading_settings,
                        ProgressDialog.STYLE_SPINNER,
                        0,
                        0);
            }
            else if (msg.what == mainFragmentActivity.whatSettingsLoadingLoaded()) {
                mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_SETTINGS_FRAGMENT);
                mainFragmentActivity.setAppSettings((AbstractAppSettings) msg.obj);

                mainFragmentActivity.loadDefaultObserver();
                mainFragmentActivity.executeGetDeviceStatusAsyncTask();

                // performs additional task (loading unities)
                Message messageExecuteTask = Message.obtain();
                messageExecuteTask.what = AbstractSettingsService.HANDLER_EXECUTE_TASK;

                if (mainFragmentActivity.mSettingsServiceMessenger != null) {
                    Log.d(getClass().getName(), "send message HANDLER_EXECUTE_TASK");

                    try {
                        mainFragmentActivity.mSettingsServiceMessenger.send(messageExecuteTask);
                    }
                    catch (RemoteException re) {
                        Log.w(getClass().getName(), re.getMessage(), re);
                    }
                }
                else {
                    Log.d(getClass().getName(), "mSyncServiceMessenger null : add message HANDLER_EXECUTE_TASK");
                    mainFragmentActivity.messagesQueue.add(messageExecuteTask);
                }
            }
            else if (msg.what == mainFragmentActivity.whatSettingsLoadingFailed()) {
                mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_SETTINGS_FRAGMENT);
                Toast.makeText(mainFragmentActivity, String.format(mainFragmentActivity.getString(R.string.message_settings_not_found), msg.obj), Toast.LENGTH_LONG).show();
            }
            else {
                mainFragmentActivity.performMessageStatusTaskHandler(mainFragmentActivity, msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Log.d(AbstractMainFragmentActivity.class.getName(), "onCreate, savedInstanceState null");
            mSavedState = new Bundle();
            setCloseApplication(false);
        }
        else {
            Log.d(AbstractMainFragmentActivity.class.getName(), "onCreate, savedInstanceState initialized");
            mSavedState = savedInstanceState;
        }

        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(ALERT_DIALOG_DELETE_INPUTS_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }

        // starts the service if needed before binding on it
        if (!mSavedState.containsKey(KEY_SERVICE_INITIALIZED)) {
            startService(new Intent(getSettingsServiceAction()));
            mSavedState.putBoolean(KEY_SERVICE_INITIALIZED, true);
        }

        mButtonStartInput = (Button) findViewById(R.id.buttonStartInput);
        mButtonStartInput.setOnClickListener(this);

        mButtonStartSynchronization = (Button) findViewById(R.id.buttonStartSynchronization);
        mButtonStartSynchronization.setOnClickListener(this);

        mListViewDeviceStatus = (ListView) findViewById(R.id.listViewDeviceStatus);
        mListViewDeviceStatus.setEmptyView(findViewById(android.R.id.empty));
        mDeviceStatusAdapter = new DeviceStatusAdapter(this, android.R.layout.simple_list_item_2);
        mListViewDeviceStatus.setAdapter(mDeviceStatusAdapter);

        // only in debug mode
        if (BuildConfig.DEBUG) {
            mListViewDeviceStatus.setOnItemLongClickListener(this);
        }

        mNetworkConnectivityListener = new NetworkConnectivityListener(this);

        mIsActivityCreated.set(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(AbstractMainFragmentActivity.class.getName(), "onResume");

        if (isCloseApplication()) {
            finish();
        }

        if ((getAppSettings() == null) || !checkServiceMessageStatusTask()) {
            doBindService();
        }

        if (getAppSettings() == null) {
            // performs load settings task
            Message messageLoadSettings = Message.obtain();
            messageLoadSettings.what = AbstractSettingsService.HANDLER_LOAD_SETTINGS;

            if (mSettingsServiceMessenger != null) {
                Log.d(AbstractMainFragmentActivity.class.getName(), "send message HANDLER_LOAD_SETTINGS");

                try {
                    mSettingsServiceMessenger.send(messageLoadSettings);
                }
                catch (RemoteException re) {
                    Log.w(AbstractMainFragmentActivity.class.getName(), re.getMessage(), re);
                }
            }
            else {
                Log.d(AbstractMainFragmentActivity.class.getName(), "mSyncServiceMessenger null : add message HANDLER_LOAD_SETTINGS");
                messagesQueue.add(messageLoadSettings);
            }
        }
        else {
            loadDefaultObserver();
            executeGetDeviceStatusAsyncTask();
        }

        if ((getAppSettings() != null) && checkServiceMessageStatusTask()) {
            for (String dialogTag : new ArrayList<>(mDialogTags)) {
                dismissProgressDialog(dialogTag);
            }

            mButtonStartInput.setEnabled(true);
            startListeningNetworkConnectivity();
        }
    }

    @Override
    protected void onPause() {
        Log.d(AbstractMainFragmentActivity.class.getName(), "onPause");

        for (String dialogTag : new ArrayList<>(mDialogTags)) {
            dismissProgressDialog(dialogTag);
        }

        doUnbindService();
        mNetworkConnectivityListener.stopListening();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(AbstractMainFragmentActivity.class.getName(), "onDestroy");

        if (isFinishing()) {
            mSavedState.remove(KEY_SERVICE_INITIALIZED);
            mSavedState.remove(KEY_SERVICE_STATUS);
        }

        mIsActivityCreated.set(false);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(Menu.NONE, 0, Menu.NONE, R.string.action_settings).setIcon(R.drawable.ic_action_settings);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        // confirms before delete all inputs
        if (position == 1) {
            confirmBeforeDeleteAllInputs();
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection =
                {
                        MainDatabaseHelper.ObserversColumns._ID,
                        MainDatabaseHelper.ObserversColumns.LASTNAME,
                        MainDatabaseHelper.ObserversColumns.FIRSTNAME
                };

        return new CursorLoader(this, getObserverLoaderUri(args.getLong(KEY_SELECTED_OBSERVER)), projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ((data != null) && data.moveToFirst()) {
            setDefaultObserver(new Observer(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)), data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.LASTNAME)), data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.FIRSTNAME))));
            Log.d(AbstractMainFragmentActivity.class.getName(), "onLoadFinished, default observer : " + getDefaultObserver().getObserverId());
        }
        else {
            Log.w(AbstractMainFragmentActivity.class.getName(), "onLoadFinished, unable to fetch the default observer from database");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do ...
    }

    public void showProgressDialog(String tag, int title, int message, int progressStyle, int progress, int max) {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (dialogFragment != null) {
            dialogFragment.setProgress(progress);
        }
        else {
            if (!mDialogTags.contains(tag) && mIsActivityCreated.get() && !isFinishing() && (!((ServiceStatus) mSavedState.getParcelable(KEY_SERVICE_STATUS)).getStatus().equals(ServiceStatus.Status.FINISHED) && !((ServiceStatus) mSavedState.getParcelable(KEY_SERVICE_STATUS)).getStatus().equals(ServiceStatus.Status.FINISHED_WITH_ERRORS) && !((ServiceStatus) mSavedState.getParcelable(KEY_SERVICE_STATUS)).getStatus().equals(ServiceStatus.Status.ABORTED))) {
                Log.d(AbstractMainFragmentActivity.class.getName(), "showProgressDialog create " + tag);

                mDialogTags.add(tag);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(title, message, progressStyle, max);
                progressDialogFragment.show(ft, tag);
            }
        }
    }

    public void dismissProgressDialog(String tag) {
        Log.d(AbstractMainFragmentActivity.class.getName(), "dismissProgressDialog " + tag);

        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (dialogFragment != null) {
            mDialogTags.remove(tag);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            dialogFragment.dismiss();
            ft.remove(dialogFragment);
            ft.commit();
        }
    }

    protected abstract boolean isCloseApplication();

    protected abstract void setCloseApplication(boolean closeApplication);

    protected abstract AbstractAppSettings getAppSettings();

    protected abstract void setAppSettings(AbstractAppSettings appSettings);

    protected abstract String getSettingsServiceAction();

    protected abstract boolean checkServiceMessageStatusTask();

    protected abstract void performMessageStatusTaskHandler(AbstractMainFragmentActivity mainFragmentActivity, Message msg);

    protected abstract Uri getObserverLoaderUri(long ObserverId);

    protected abstract Observer getDefaultObserver();

    protected abstract void setDefaultObserver(Observer observer);

    protected abstract int whatSettingsLoadingStart();

    protected abstract int whatSettingsLoading();

    protected abstract int whatSettingsLoadingFailed();

    protected abstract int whatSettingsLoadingLoaded();

    protected void startListeningNetworkConnectivity() {
        mNetworkConnectivityListener.startListening(mOnNetworkConnectivityChangeListener);
    }

    protected void loadDefaultObserver() {
        long defaultObserverId = PreferenceManager.getDefaultSharedPreferences(this).getLong("default_observer", -1);

        Log.d(AbstractMainFragmentActivity.class.getName(), "loadDefaultObserver : " + defaultObserverId);

        if ((defaultObserverId != -1) && (mSavedState.getLong(KEY_SELECTED_OBSERVER, -1) != defaultObserverId)) {
            mSavedState.putLong(KEY_SELECTED_OBSERVER, defaultObserverId);
            getSupportLoaderManager().restartLoader(0, mSavedState, this);
        }
    }

    protected void executeGetDeviceStatusAsyncTask() {
        new GetDeviceStatusAsyncTask().execute();
    }

    /**
     * Establish a connection with the service.
     */
    private void doBindService() {
        mIsSettingsServiceBound = bindService(new Intent(getSettingsServiceAction()), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindService() {
        if (mIsSettingsServiceBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mSettingsServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, AbstractSettingsService.HANDLER_UNREGISTER_CLIENT);
                    // this is needed to unregister the message used by this client
                    msg.replyTo = mMessenger;
                    mSettingsServiceMessenger.send(msg);
                }
                catch (RemoteException re) {
                    // There is nothing special we need to do if the service has crashed.
                    Log.w(AbstractMainFragmentActivity.class.getName(), re.getMessage(), re);
                }
            }

            // Detach our existing connection.
            unbindService(mServiceConnection);
            mIsSettingsServiceBound = false;
        }
    }

    private void confirmBeforeDeleteAllInputs() {
        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                R.string.alert_dialog_confirm_delete_inputs_title,
                R.string.alert_dialog_confirm_delete_inputs_message
        );
        alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialogFragment.show(
                getSupportFragmentManager(),
                ALERT_DIALOG_DELETE_INPUTS_FRAGMENT
        );
    }

    /**
     * <code>AsyncTask</code> implementation to update the current device status :
     * <ul>
     * <li>count number of {@link com.makina.ecrins.commons.input.AbstractInput} saved as JSON file</li>
     * <li>get the last synchronization date</li>
     * </ul>
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class GetDeviceStatusAsyncTask extends AsyncTask<Void, Void, List<Long>> {
        @Override
        protected List<Long> doInBackground(Void... params) {
            List<Long> result = new ArrayList<>(2);

            try {
                result.add(FileUtils.getFileFromApplicationStorage(AbstractMainFragmentActivity.this, "databases" + File.separator + getAppSettings().getDbSettings().getDbName()).lastModified());
            }
            catch (IOException ioe) {
                Log.w(getClass().getName(), ioe);

                result.add(0l);
            }

            try {
                File inputDir = FileUtils.getInputsFolder(AbstractMainFragmentActivity.this);

                if (inputDir.exists()) {
                    result.add(
                            (long) inputDir.listFiles(
                                    new FileFilter() {
                                        @Override
                                        public boolean accept(File pathname) {
                                            return pathname.getName()
                                                    .startsWith("input_") && pathname.getName()
                                                    .endsWith(".json");
                                        }
                                    }
                            ).length
                    );
                }
                else {
                    result.add(0l);
                }
            }
            catch (IOException ioe) {
                Log.w(getClass().getName(), ioe.getMessage(), ioe);

                result.add(0l);
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<Long> result) {
            mDeviceStatusAdapter.clear();

            for (Long value : result) {
                mDeviceStatusAdapter.add(value);
            }

            mDeviceStatusAdapter.notifyDataSetChanged();
        }
    }

    private class DeviceStatusAdapter extends ArrayAdapter<Long> {

        private int mTextViewResourceId;
        private final LayoutInflater mInflater;

        public DeviceStatusAdapter(Context context, int textViewResourceId) {
            super(
                    context,
                    textViewResourceId
            );

            mTextViewResourceId = textViewResourceId;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(
                        mTextViewResourceId,
                        parent,
                        false
                );
            }
            else {
                view = convertView;
            }

            switch (position) {
                case 0:
                    ((TextView) view.findViewById(android.R.id.text1)).setText(R.string.synchro_last_synchronization);

                    if (getItem(position) == 0) {
                        ((TextView) view.findViewById(android.R.id.text2)).setText(R.string.synchro_last_synchronization_never);
                    }
                    else {
                        ((TextView) view.findViewById(android.R.id.text2)).setText(
                                android.text.format.DateFormat.format(
                                        getResources().getString(R.string.synchro_last_synchronization_date),
                                        new Date(getItem(position))
                                )
                        );
                    }

                    break;
                case 1:
                    ((TextView) view.findViewById(android.R.id.text1)).setText(R.string.synchro_inputs_not_synchronized);
                    ((TextView) view.findViewById(android.R.id.text2)).setText(getItem(position).toString());

                    break;
            }

            return view;
        }
    }
}
