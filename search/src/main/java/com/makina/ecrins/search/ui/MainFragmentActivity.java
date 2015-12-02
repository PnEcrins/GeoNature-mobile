package com.makina.ecrins.search.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.service.AbstractRequestHandler;
import com.makina.ecrins.commons.service.RequestHandlerServiceClient;
import com.makina.ecrins.commons.service.RequestHandlerStatus;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.MainApplication;
import com.makina.ecrins.search.R;
import com.makina.ecrins.search.settings.AppSettings;
import com.makina.ecrins.search.settings.LoadSettingsRequestHandler;
import com.makina.ecrins.search.ui.dialog.FeatureDialogFragment;
import com.makina.ecrins.search.ui.maps.WebViewFragment;
import com.makina.ecrins.search.ui.maps.WebViewFragment.OnFeaturesFoundListener;
import com.makina.ecrins.search.ui.settings.MainPreferencesActivity;
import com.makina.ecrins.search.ui.sync.SynchronizationActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main {@code Activity} of this application used to initialize
 * * {@link com.makina.ecrins.search.ui.maps.WebViewFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity
        extends AppCompatActivity
        implements OnFeaturesFoundListener {

    private static final String TAG = MainFragmentActivity.class.getSimpleName();

    private static final String KEY_REQUEST_HANDLER_SERVICE_CLIENT_TOKEN = "KEY_REQUEST_HANDLER_SERVICE_CLIENT_TOKEN";
    private static final String KEY_SELECTED_FEATURE = "KEY_SELECTED_FEATURE";

    private static final String QUIT_ACTION_DIALOG = "QUIT_ACTION_DIALOG";
    private static final String LOAD_SETTINGS_DIALOG = "LOAD_SETTINGS_DIALOG";
    private static final String SHOW_FEATURE_DIALOG = "SHOW_FEATURE_DIALOG";

    private Bundle mSavedState;

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

    private RequestHandlerServiceClient.ServiceClientListener mServiceClientListener = new RequestHandlerServiceClient.ServiceClientListener() {
        @Override
        public void onConnected(@NonNull String token) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onConnected: " + token);
            }

            mSavedState.putString(KEY_REQUEST_HANDLER_SERVICE_CLIENT_TOKEN,
                                  token);

            // send Message to get the current status of LoadSettingsRequestHandler
            final Bundle data = new Bundle();
            data.putSerializable(LoadSettingsRequestHandler.KEY_COMMAND,
                                 LoadSettingsRequestHandler.Command.GET_STATUS);

            mRequestHandlerServiceClient.send(LoadSettingsRequestHandler.class,
                                              data);
        }

        @Override
        public void onDisconnected() {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onDisconnected");
            }
        }

        @Override
        public void onHandleMessage(
                @NonNull AbstractRequestHandler requestHandler,
                @NonNull Bundle data) {

            if (requestHandler instanceof LoadSettingsRequestHandler) {
                handleMessageForLoadSettingsRequestHandler(data);
            }
        }
    };

    private RequestHandlerServiceClient mRequestHandlerServiceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_maps);

        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCreate, savedInstanceState null");
            }

            mSavedState = new Bundle();
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCreate, savedInstanceState initialized");
            }

            mSavedState = savedInstanceState;
        }

        // restore AlertDialogFragment state after resume if needed
        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(QUIT_ACTION_DIALOG);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onResume");
        }

        if (((MainApplication) getApplication()).getAppSettings() == null) {
            if (mRequestHandlerServiceClient == null) {
                mRequestHandlerServiceClient = new RequestHandlerServiceClient(this);
            }

            mRequestHandlerServiceClient.setServiceClientListener(mServiceClientListener);
            mRequestHandlerServiceClient.connect(mSavedState.getString(KEY_REQUEST_HANDLER_SERVICE_CLIENT_TOKEN));
        }
    }

    @Override
    protected void onPause() {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onPause");
        }

        if (mRequestHandlerServiceClient != null) {
            mRequestHandlerServiceClient.disconnect();
        }

        super.onPause();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode,
                               resultCode,
                               data);

        if ((requestCode == 0) && (resultCode == RESULT_OK) && (data.getExtras() != null)) {
            final Feature selectedFeature = data.getExtras()
                                                .getParcelable(FeaturesFragmentActivity.KEY_SELECTED_FEATURE);

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onActivityResult, selected feature: " + selectedFeature.getId());
            }

            mSavedState.putParcelable(KEY_SELECTED_FEATURE,
                                      selectedFeature);

            setTitle(selectedFeature.getProperties()
                                    .getString(MainDatabaseHelper.SearchColumns.TAXON));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,
                                  menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.app_synchro:
                startActivity(new Intent(this,
                                         SynchronizationActivity.class));
                return true;
            case R.id.app_settings:
                startActivity(new Intent(this,
                                         MainPreferencesActivity.class));
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

        final Intent intent = new Intent();
        intent.setClass(this,
                        FeaturesFragmentActivity.class);
        intent.putParcelableArrayListExtra(FeaturesFragmentActivity.KEY_FEATURES,
                                           new ArrayList<>(features));

        startActivityForResult(intent,
                               0);
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
                Log.d(TAG,
                      "onFeatureSelected, geoPoint : " + geoPoint.toString() + ", feature : " + feature.getJSONObject()
                                                                                                       .toString());
            }

            FeatureDialogFragment dialogFragment = (FeatureDialogFragment) getSupportFragmentManager().findFragmentByTag(SHOW_FEATURE_DIALOG);

            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }

            dialogFragment = FeatureDialogFragment.newInstance(feature,
                                                               geoPoint);
            dialogFragment.show(getSupportFragmentManager(),
                                SHOW_FEATURE_DIALOG);
        }
        catch (JSONException je) {
            Log.w(TAG,
                  je.getMessage());
        }
    }

    private void handleMessageForLoadSettingsRequestHandler(@NonNull final Bundle data) {

        if (data.containsKey(LoadSettingsRequestHandler.KEY_STATUS)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onHandleMessage: LoadSettingsRequestHandler status " + ((RequestHandlerStatus) data.getParcelable(LoadSettingsRequestHandler.KEY_STATUS)).getStatus()
                                                                                                                                                                .name());
            }

            switch (((RequestHandlerStatus) data.getParcelable(LoadSettingsRequestHandler.KEY_STATUS)).getStatus()) {
                case PENDING:
                    dismissProgressDialog(LOAD_SETTINGS_DIALOG);

                    // send Message to start loading AppSettings
                    data.putSerializable(LoadSettingsRequestHandler.KEY_COMMAND,
                                         LoadSettingsRequestHandler.Command.START);
                    data.putString(LoadSettingsRequestHandler.KEY_FILENAME,
                                   "settings_search.json");

                    mRequestHandlerServiceClient.send(LoadSettingsRequestHandler.class,
                                                      data);
                    break;
                case RUNNING:
                    showProgressDialog(LOAD_SETTINGS_DIALOG,
                                       R.string.progress_title,
                                       R.string.progress_message_loading_settings,
                                       ProgressDialog.STYLE_SPINNER,
                                       0,
                                       0);
                    break;
                case FINISHED:
                    dismissProgressDialog(LOAD_SETTINGS_DIALOG);
                    final AppSettings appSettings = data.getParcelable(LoadSettingsRequestHandler.KEY_APP_SETTINGS);

                    if (appSettings == null) {
                        Toast.makeText(MainFragmentActivity.this,
                                       String.format(getString(R.string.message_settings_not_found),
                                                     data.getString(LoadSettingsRequestHandler.KEY_FILENAME)),
                                       Toast.LENGTH_LONG)
                             .show();
                    }
                    else {
                        ((MainApplication) getApplication()).setAppSettings(appSettings);

                        final FragmentManager fm = getSupportFragmentManager();

                        if (fm.findFragmentById(android.R.id.content) == null) {
                            final WebViewFragment mapsFragment = new WebViewFragment();
                            fm.beginTransaction()
                              .add(android.R.id.content,
                                   mapsFragment)
                              .commit();
                        }
                    }
                    break;
                case FINISHED_WITH_ERRORS:
                    dismissProgressDialog(LOAD_SETTINGS_DIALOG);
                    Toast.makeText(MainFragmentActivity.this,
                                   String.format(getString(R.string.message_settings_not_found),
                                                 data.getString(LoadSettingsRequestHandler.KEY_FILENAME)),
                                   Toast.LENGTH_LONG)
                         .show();
                    break;
            }
        }
    }

    private void showConfirmDialogBeforeQuit() {

        final AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(R.string.alert_dialog_confirm_quit_title,
                                                                                R.string.alert_dialog_confirm_quit_message);
        alertDialog.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialog.show(getSupportFragmentManager(),
                         QUIT_ACTION_DIALOG);
    }

    private void showProgressDialog(
            String tag,
            int title,
            int message,
            int progressStyle,
            int progress,
            int max) {

        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (progressDialogFragment == null) {
            progressDialogFragment = ProgressDialogFragment.newInstance(title,
                                                                        message,
                                                                        progressStyle,
                                                                        max);
            progressDialogFragment.show(getSupportFragmentManager(),
                                        tag);
        }

        progressDialogFragment.setProgress(progress);
    }

    private void dismissProgressDialog(String tag) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "dismissProgressDialog " + tag);
        }

        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }
}
