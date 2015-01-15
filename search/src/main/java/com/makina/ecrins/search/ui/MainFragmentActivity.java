package com.makina.ecrins.search.ui;

import android.app.ProgressDialog;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.settings.ServiceStatus;
import com.makina.ecrins.commons.settings.ServiceStatus.Status;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.MainApplication;
import com.makina.ecrins.search.R;
import com.makina.ecrins.search.settings.AppSettings;
import com.makina.ecrins.search.settings.SettingsService;
import com.makina.ecrins.search.ui.dialog.FeatureDialogFragment;
import com.makina.ecrins.search.ui.maps.WebViewFragment;
import com.makina.ecrins.search.ui.maps.WebViewFragment.OnFeaturesFoundListener;
import com.makina.ecrins.search.ui.settings.MainPreferencesActivity;
import com.makina.ecrins.search.ui.sync.SynchronizationActivity;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the main {@code Activity} of this application used to initialize
 * * {@link com.makina.ecrins.search.ui.maps.WebViewFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity
        extends ActionBarActivity
        implements OnFeaturesFoundListener {

    protected static final String KEY_SELECTED_FEATURE = "selected_feature";
    protected static final String KEY_SERVICE_INITIALIZED = "service_initialized";
    protected static final String KEY_SERVICE_STATUS = "service_status";

    private static final String QUIT_ACTION_DIALOG_FRAGMENT = "quit_action_dialog";
    private static final String PROGRESS_DIALOG_SETTINGS_FRAGMENT = "progress_dialog_settings";
    private static final String FEATURE_DIALOG_FRAGMENT = "feature_dialog_settings";

    /**
     * Messenger for communicating with service.
     */
    protected Messenger mSettingsServiceMessenger = null;

    /**
     * Target we publish for clients to send messages to {@link com.makina.ecrins.search.ui.MainFragmentActivity.MainFragmentHandler}.
     */
    private final Messenger mMessenger = new Messenger(new MainFragmentHandler(this));

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean mIsSettingsServiceBound;

    private final AtomicBoolean mIsActivityCreated = new AtomicBoolean();

    protected Deque<Message> messagesQueue = new ArrayDeque<>();

    private Bundle mSavedState;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(
                ComponentName name,
                IBinder service) {
            Log.d(
                    getClass().getName(),
                    "onServiceConnected " + name
            );

            if (name.getClassName()
                    .equals(SettingsService.class.getName())) {
                // This is called when the connection with the service has been established, giving us the service object we can use to interact with the service.
                // We are communicating with our service through an IDL interface, so get a client-side representation of that from the raw service object.
                mSettingsServiceMessenger = new Messenger(service);

                // We want to monitor the service for as long as we are connected to it.
                try {
                    Message msg = Message.obtain(
                            null,
                            SettingsService.HANDLER_REGISTER_CLIENT
                    );
                    msg.replyTo = mMessenger;
                    mSettingsServiceMessenger.send(msg);
                }
                catch (RemoteException re) {
                    // In this case the service has crashed before we could even do anything with it.
                    // We can count on soon being disconnected (and then reconnected if it can be restarted) so there is no need to do anything here.
                    Log.w(
                            getClass().getName(),
                            re.getMessage(),
                            re
                    );
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is, its process crashed.

            Log.d(
                    getClass().getName(),
                    "onServiceDisconnected " + name
            );

            if (name.getClassName()
                    .equals(SettingsService.class.getName())) {
                mSettingsServiceMessenger = null;
            }
        }
    };

    private AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {
            finish();
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            // nothing to do ...
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_maps);

        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) {
                Log.d(
                        MainFragmentActivity.class.getName(),
                        "onCreate, savedInstanceState null"
                );
            }

            mSavedState = new Bundle();
            ((MainApplication) getApplication()).setCloseApplication(false);
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(
                        MainFragmentActivity.class.getName(),
                        "onCreate, savedInstanceState initialized"
                );
            }

            mSavedState = savedInstanceState;
        }

        // starts the service if needed before binding on it
        if (!mSavedState.containsKey(KEY_SERVICE_INITIALIZED)) {
            startService(
                    new Intent(
                            this,
                            SettingsService.class
                    )
            );
            mSavedState.putBoolean(
                    KEY_SERVICE_INITIALIZED,
                    true
            );
        }

        // restore AlertDialogFragment state after resume if needed
        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(QUIT_ACTION_DIALOG_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }

        mIsActivityCreated.set(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (BuildConfig.DEBUG) {
            Log.d(
                    MainFragmentActivity.class.getName(),
                    "onResume"
            );
        }

        if (((MainApplication) getApplication()).isCloseApplication()) {
            finish();
        }

        if (((MainApplication) getApplication()).getAppSettings() == null) {
            doBindService();
        }

        if ((((MainApplication) getApplication()).getAppSettings() == null)) {
            // performs load settings task
            Message messageLoadSettings = Message.obtain();
            messageLoadSettings.what = SettingsService.HANDLER_LOAD_SETTINGS;

            if (mSettingsServiceMessenger != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(
                            MainFragmentActivity.class.getName(),
                            "send message HANDLER_LOAD_SETTINGS"
                    );
                }

                try {
                    mSettingsServiceMessenger.send(messageLoadSettings);
                }
                catch (RemoteException re) {
                    Log.w(
                            MainFragmentActivity.class.getName(),
                            re.getMessage(),
                            re
                    );
                }
            }
            else {
                if (BuildConfig.DEBUG) {
                    Log.d(
                            MainFragmentActivity.class.getName(),
                            "mSyncServiceMessenger null: add message HANDLER_LOAD_SETTINGS"
                    );
                }

                messagesQueue.add(messageLoadSettings);
            }
        }

        if (((MainApplication) getApplication()).getAppSettings() != null) {
            dismissProgressDialog(PROGRESS_DIALOG_SETTINGS_FRAGMENT);
            setSupportProgressBarIndeterminateVisibility(false);

            Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);

            if (fragment == null) {
                if (BuildConfig.DEBUG) {
                    Log.d(
                            MainFragmentActivity.class.getName(),
                            "onResume: create WebViewFragment"
                    );
                }

                WebViewFragment mapsFragment = new WebViewFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(
                                android.R.id.content,
                                mapsFragment
                        )
                        .commit();
            }
            else {
                if (BuildConfig.DEBUG) {
                    Log.d(
                            MainFragmentActivity.class.getName(),
                            "onResume: reload WebViewFragment"
                    );
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(
                                android.R.id.content,
                                fragment
                        )
                        .commit();
            }
        }
    }

    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) {
            Log.d(
                    MainFragmentActivity.class.getName(),
                    "onPause"
            );
        }

        doUnbindService();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(
                    MainFragmentActivity.class.getName(),
                    "onDestroy"
            );
        }

        if (isFinishing()) {
            mSavedState.remove(KEY_SERVICE_INITIALIZED);
            mSavedState.remove(KEY_SERVICE_STATUS);
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if ((requestCode == 0) && (resultCode == RESULT_OK) && (data.getExtras() != null)) {
            final Feature selectedFeature = data.getExtras().getParcelable(FeaturesFragmentActivity.KEY_SELECTED_FEATURE);

            if (BuildConfig.DEBUG) {
                Log.d(
                        MainFragmentActivity.class.getName(),
                        "onActivityResult, selected feature: " + selectedFeature.getId()
                );
            }

            mSavedState.putParcelable(
                    KEY_SELECTED_FEATURE,
                    selectedFeature
            );

            setTitle(selectedFeature.getProperties().getString(MainDatabaseHelper.SearchColumns.TAXON));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.main,
                menu
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_synchro:
                startActivity(
                        new Intent(
                                this,
                                SynchronizationActivity.class
                        )
                );
                return true;
            case R.id.app_settings:
                startActivity(
                        new Intent(
                                this,
                                MainPreferencesActivity.class
                        )
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showConfirmDialogBeforeQuit();
    }

    @Override
    public void onFeaturesFound(List<Feature> features) {
        Intent intent = new Intent();
        intent.setClass(
                this,
                FeaturesFragmentActivity.class
        );
        intent.putParcelableArrayListExtra(
                FeaturesFragmentActivity.KEY_FEATURES,
                new ArrayList<>(features)
        );

        startActivityForResult(
                intent,
                0
        );
    }

    @Override
    public Feature getSelectedFeature() {
        if (mSavedState.containsKey(KEY_SELECTED_FEATURE)) {
            return mSavedState.getParcelable(KEY_SELECTED_FEATURE);
        }
        else {
            return null;
        }
    }

    @Override
    public void onFeatureSelected(
            GeoPoint geoPoint,
            Feature feature) {
        try {
            if (BuildConfig.DEBUG) {
                Log.d(
                        MainFragmentActivity.class.getName(),
                        "onFeatureSelected, geoPoint : " + geoPoint.toString() + ", feature : " + feature.getJSONObject().toString()
                );
            }

            FeatureDialogFragment dialogFragment = (FeatureDialogFragment) getSupportFragmentManager().findFragmentByTag(FEATURE_DIALOG_FRAGMENT);

            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }

            dialogFragment = FeatureDialogFragment.newInstance(
                    feature,
                    geoPoint
            );
            dialogFragment.show(
                    getSupportFragmentManager(),
                    FEATURE_DIALOG_FRAGMENT
            );
        }
        catch (JSONException je) {
            Log.w(
                    MainFragmentActivity.class.getName(),
                    je.getMessage()
            );
        }
    }

    /**
     * Establish a connection with the service.
     */
    private void doBindService() {
        if (BuildConfig.DEBUG) {
            Log.d(
                    MainFragmentActivity.class.getName(),
                    "doBindService"
            );
        }

        mIsSettingsServiceBound = bindService(
                new Intent(
                        this,
                        SettingsService.class
                ),
                mServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    private void doUnbindService() {
        if (mIsSettingsServiceBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mSettingsServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(
                            null,
                            SettingsService.HANDLER_UNREGISTER_CLIENT
                    );
                    // this is needed to unregister the message used by this client
                    msg.replyTo = mMessenger;
                    mSettingsServiceMessenger.send(msg);
                }
                catch (RemoteException re) {
                    // There is nothing special we need to do if the service has crashed.
                    Log.w(
                            MainFragmentActivity.class.getName(),
                            re.getMessage(),
                            re
                    );
                }
            }

            // Detach our existing connection.
            unbindService(mServiceConnection);
            mIsSettingsServiceBound = false;
        }
    }

    private void showConfirmDialogBeforeQuit() {
        final AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(
                R.string.alert_dialog_confirm_quit_title,
                R.string.alert_dialog_confirm_quit_message
        );
        alertDialog.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialog.show(
                getSupportFragmentManager(),
                QUIT_ACTION_DIALOG_FRAGMENT
        );
    }

    protected void showProgressDialog(
            String tag,
            int title,
            int message,
            int progressStyle,
            int progress,
            int max) {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (dialogFragment != null) {
            dialogFragment.setProgress(progress);
        }
        else {
            if (mIsActivityCreated.get() && !isFinishing() && (!((ServiceStatus) mSavedState.getParcelable(KEY_SERVICE_STATUS)).getStatus()
                    .equals(Status.FINISHED) && !((ServiceStatus) mSavedState.getParcelable(KEY_SERVICE_STATUS)).getStatus()
                    .equals(Status.FINISHED_WITH_ERRORS) && !((ServiceStatus) mSavedState.getParcelable(KEY_SERVICE_STATUS)).getStatus()
                    .equals(Status.ABORTED))) {
                if (BuildConfig.DEBUG) {
                    Log.d(
                            MainFragmentActivity.class.getName(),
                            "showProgressDialog create " + tag
                    );
                }

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(
                        title,
                        message,
                        progressStyle,
                        max
                );
                progressDialogFragment.show(
                        ft,
                        tag
                );
            }
        }
    }

    protected void dismissProgressDialog(String tag) {
        if (BuildConfig.DEBUG) {
            Log.d(
                    MainFragmentActivity.class.getName(),
                    "dismissProgressDialog " + tag
            );
        }

        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (dialogFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            dialogFragment.dismiss();
            ft.remove(dialogFragment);
            ft.commit();
        }
    }

    private static class MainFragmentHandler
            extends Handler {

        private final WeakReference<MainFragmentActivity> mMainFragmentActivity;

        public MainFragmentHandler(MainFragmentActivity pMainFragmentActivity) {
            super();
            mMainFragmentActivity = new WeakReference<>(pMainFragmentActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainFragmentActivity mainFragmentActivity = mMainFragmentActivity.get();

            switch (msg.what) {
                case SettingsService.HANDLER_CLIENT_REGISTERED:
                    Log.d(
                            getClass().getName(),
                            "handleMessage HANDLER_CLIENT_REGISTERED " + mainFragmentActivity.messagesQueue.size()
                    );

                    // tries to retrieve all pending messages
                    Message messageGetPendingMessages = Message.obtain();
                    messageGetPendingMessages.what = SettingsService.HANDLER_GET_PENDING_MESSAGES;

                    if (mainFragmentActivity.mSettingsServiceMessenger != null) {
                        Log.d(
                                getClass().getName(),
                                "send message HANDLER_GET_PENDING_MESSAGES"
                        );

                        try {
                            mainFragmentActivity.mSettingsServiceMessenger.send(messageGetPendingMessages);
                        }
                        catch (RemoteException re) {
                            Log.w(
                                    getClass().getName(),
                                    re.getMessage(),
                                    re
                            );
                        }
                    }
                    else {
                        Log.d(
                                getClass().getName(),
                                "mSyncServiceMessenger null : add message HANDLER_GET_PENDING_MESSAGES"
                        );
                        mainFragmentActivity.messagesQueue.add(messageGetPendingMessages);
                    }

                    // tries to send all awaiting messages
                    while (!mainFragmentActivity.messagesQueue.isEmpty()) {
                        try {
                            mainFragmentActivity.mSettingsServiceMessenger.send(mainFragmentActivity.messagesQueue.removeFirst());
                        }
                        catch (RemoteException | NoSuchElementException re) {
                            Log.w(
                                    getClass().getName(),
                                    re.getMessage(),
                                    re
                            );
                        }
                    }

                    break;
                case SettingsService.HANDLER_STATUS:
                    ServiceStatus status = (ServiceStatus) msg.obj;
                    Log.d(
                            getClass().getName(),
                            "HANDLER_STATUS : " + status.getStatus()
                                    .name()
                    );
                    mainFragmentActivity.mSavedState.putParcelable(
                            KEY_SERVICE_STATUS,
                            status
                    );

                    if (status.getStatus()
                            .equals(Status.FINISHED)) {
                        mainFragmentActivity.setSupportProgressBarIndeterminateVisibility(false);
                        mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_SETTINGS_FRAGMENT);

                        FragmentManager fm = mainFragmentActivity.getSupportFragmentManager();

                        if (fm.findFragmentById(android.R.id.content) == null) {
                            WebViewFragment mapsFragment = new WebViewFragment();
                            fm.beginTransaction()
                                    .add(
                                            android.R.id.content,
                                            mapsFragment
                                    )
                                    .commit();
                        }
                    }

                    break;
                case MainApplication.HANDLER_SETTINGS_LOADING_START:
                    mainFragmentActivity.showProgressDialog(
                            PROGRESS_DIALOG_SETTINGS_FRAGMENT,
                            R.string.progress_title,
                            R.string.progress_message_loading_settings,
                            ProgressDialog.STYLE_SPINNER,
                            0,
                            0
                    );
                    break;
                case MainApplication.HANDLER_SETTINGS_LOADED:
                    mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_SETTINGS_FRAGMENT);
                    ((MainApplication) mainFragmentActivity.getApplication()).setAppSettings((AppSettings) msg.obj);

                    // performs additional task (loading unities)
                    Message messageExecuteTask = Message.obtain();
                    messageExecuteTask.what = SettingsService.HANDLER_EXECUTE_TASK;

                    if (mainFragmentActivity.mSettingsServiceMessenger != null) {
                        Log.d(
                                getClass().getName(),
                                "send message HANDLER_EXECUTE_TASK"
                        );

                        try {
                            mainFragmentActivity.mSettingsServiceMessenger.send(messageExecuteTask);
                        }
                        catch (RemoteException re) {
                            Log.w(
                                    getClass().getName(),
                                    re.getMessage(),
                                    re
                            );
                        }
                    }
                    else {
                        Log.d(
                                getClass().getName(),
                                "mSyncServiceMessenger null : add message HANDLER_EXECUTE_TASK"
                        );
                        mainFragmentActivity.messagesQueue.add(messageExecuteTask);
                    }

                    break;
                case MainApplication.HANDLER_SETTINGS_LOADED_FAILED:
                    mainFragmentActivity.dismissProgressDialog(PROGRESS_DIALOG_SETTINGS_FRAGMENT);
                    Toast.makeText(
                            mainFragmentActivity,
                            String.format(
                                    mainFragmentActivity.getString(R.string.message_settings_not_found),
                                    msg.obj
                            ),
                            Toast.LENGTH_LONG
                    ).show();

                    break;
            }
        }
    }
}
