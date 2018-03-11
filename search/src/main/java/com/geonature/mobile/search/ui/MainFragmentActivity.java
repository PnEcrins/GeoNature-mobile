package com.geonature.mobile.search.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.geonature.mobile.commons.content.MainDatabaseHelper;
import com.geonature.mobile.commons.settings.AbstractAppSettings;
import com.geonature.mobile.commons.settings.AbstractAppSettingsIntentService;
import com.geonature.mobile.commons.ui.dialog.AlertDialogFragment;
import com.geonature.mobile.commons.util.PermissionUtils;
import com.geonature.mobile.maps.jts.geojson.Feature;
import com.geonature.mobile.maps.jts.geojson.GeoPoint;
import com.geonature.mobile.search.BuildConfig;
import com.geonature.mobile.search.MainApplication;
import com.geonature.mobile.search.R;
import com.geonature.mobile.search.settings.AppSettings;
import com.geonature.mobile.search.settings.AppSettingsIntentService;
import com.geonature.mobile.search.ui.dialog.FeatureDialogFragment;
import com.geonature.mobile.search.ui.maps.WebViewFragment;
import com.geonature.mobile.search.ui.maps.WebViewFragment.OnFeaturesFoundListener;
import com.geonature.mobile.search.ui.settings.MainPreferencesActivity;
import com.geonature.mobile.search.ui.sync.SynchronizationActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the main {@code Activity} of this application used to initialize
 * {@link WebViewFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity
        extends AppCompatActivity
        implements OnFeaturesFoundListener {

    private static final String TAG = MainFragmentActivity.class.getSimpleName();

    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_SELECT_FEATURE = 1;
    private static final String KEY_SELECTED_FEATURE = "KEY_SELECTED_FEATURE";

    private static final String QUIT_ACTION_DIALOG = "QUIT_ACTION_DIALOG";
    private static final String SHOW_FEATURE_DIALOG = "SHOW_FEATURE_DIALOG";

    private View mLayout;
    private ProgressBar mProgressBar;
    private Bundle mSavedState;
    private boolean mRequestPermissionsResult;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,
                              Intent intent) {
            if ((intent == null) || (intent.getAction() == null)) {
                return;
            }

            final AbstractAppSettingsIntentService.Status status = (AbstractAppSettingsIntentService.Status) intent.getSerializableExtra(AbstractAppSettingsIntentService.EXTRA_STATUS);
            final AbstractAppSettings appSettings = intent.getParcelableExtra(AbstractAppSettingsIntentService.EXTRA_SETTINGS);

            if (status == null) {
                Log.w(TAG,
                      "onReceive, no status defined for action " + intent.getAction());

                return;
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onReceive, action: " + intent.getAction() + ", status: " + status);
            }

            if (intent.getAction()
                      .equals(getBroadcastActionReadAppSettings())) {
                switch (status) {
                    case FINISHED_WITH_ERRORS:
                    case FINISHED_NOT_FOUND:
                        showAppSettingsLoadingFailedAlert();
                        break;
                    case FINISHED:
                        if (appSettings != null) {
                            ((MainApplication) getApplication()).setAppSettings((AppSettings) appSettings);
                            ActivityCompat.invalidateOptionsMenu(MainFragmentActivity.this);

                            final FragmentManager fm = getSupportFragmentManager();

                            if (fm.findFragmentById(R.id.fragment_content) == null) {
                                final WebViewFragment mapsFragment = new WebViewFragment();
                                fm.beginTransaction()
                                  .add(R.id.fragment_content,
                                       mapsFragment)
                                  .commit();
                            }
                        }

                        break;
                }
            }
        }
    };

    private final AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
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

        mRequestPermissionsResult = true;

        setContentView(R.layout.activity_maps);

        mLayout = findViewById(android.R.id.content);

        final Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mProgressBar = toolbar.findViewById(R.id.progressBar);
        }

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

        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(mBroadcastReceiver,
                                               new IntentFilter(getBroadcastActionReadAppSettings()));

        if (mRequestPermissionsResult) {
            PermissionUtils.checkSelfPermissions(this,
                                                 new PermissionUtils.OnCheckSelfPermissionListener() {
                                                     @Override
                                                     public void onPermissionsGranted() {
                                                         loadAppSettings();
                                                     }

                                                     @Override
                                                     public void onRequestPermissions(@NonNull String... permissions) {
                                                         PermissionUtils.requestPermissions(MainFragmentActivity.this,
                                                                                            mLayout,
                                                                                            R.string.snackbar_permission_external_storage_rationale,
                                                                                            REQUEST_EXTERNAL_STORAGE,
                                                                                            permissions);
                                                     }
                                                 },
                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        else {
            mRequestPermissionsResult = true;
        }
    }

    @Override
    protected void onPause() {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onPause");
        }

        LocalBroadcastManager.getInstance(this)
                             .unregisterReceiver(mBroadcastReceiver);

        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (permissions.length > 0) {
                    mRequestPermissionsResult = PermissionUtils.checkPermissions(grantResults);

                    if (mRequestPermissionsResult) {
                        int messageResourceId = R.string.snackbar_permissions_granted;

                        if (Arrays.asList(permissions)
                                  .containsAll(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,
                                                             Manifest.permission.ACCESS_COARSE_LOCATION)) && !Arrays.asList(permissions)
                                                                                                                    .contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            messageResourceId = R.string.message_permission_location_available;
                        }
                        else if (!Arrays.asList(permissions)
                                        .containsAll(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,
                                                                   Manifest.permission.ACCESS_COARSE_LOCATION)) && Arrays.asList(permissions)
                                                                                                                         .contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            messageResourceId = R.string.snackbar_permission_external_storage_available;
                        }

                        Snackbar.make(mLayout,
                                      messageResourceId,
                                      Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    else {
                        Snackbar.make(mLayout,
                                      R.string.snackbar_permissions_not_granted,
                                      Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                                                 permissions,
                                                 grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                               resultCode,
                               data);

        if ((requestCode == REQUEST_SELECT_FEATURE) && (resultCode == RESULT_OK) && (data.getExtras() != null)) {
            final Feature selectedFeature = data.getExtras()
                                                .getParcelable(FeaturesFragmentActivity.KEY_SELECTED_FEATURE);

            if (selectedFeature == null) {
                Log.d(TAG,
                      "onActivityResult, no feature selected");

                return;
            }

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
    public void onFindFeatures(boolean start) {
        if (mProgressBar == null) {
            return;
        }

        mProgressBar.setVisibility(start ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFeaturesFound(List<Feature> features) {

        final Intent intent = new Intent();
        intent.setClass(this,
                        FeaturesFragmentActivity.class);
        intent.putParcelableArrayListExtra(FeaturesFragmentActivity.KEY_FEATURES,
                                           new ArrayList<>(features));

        startActivityForResult(intent,
                               REQUEST_SELECT_FEATURE);
    }

    @Nullable
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
    public void onFeatureSelected(GeoPoint geoPoint,
                                  Feature feature) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onFeatureSelected, geoPoint: " + geoPoint.toString() + ", feature: " + feature.getId());
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

    private void loadAppSettings() {
        AbstractAppSettingsIntentService.readSettings(this,
                                                      AppSettingsIntentService.class,
                                                      getBroadcastActionReadAppSettings(),
                                                      getAppSettingsFilename());
    }

    @NonNull
    private String getBroadcastActionReadAppSettings() {
        return getPackageName() + ".broadcast.settings.read";
    }

    @NonNull
    private String getAppSettingsFilename() {
        final String packageName = getPackageName();

        return "settings_" + packageName.substring(packageName.lastIndexOf('.') + 1) + ".json";
    }

    private void showAppSettingsLoadingFailedAlert() {
        Toast.makeText(this,
                       getString(R.string.message_settings_not_found,
                                 getAppSettingsFilename()),
                       Toast.LENGTH_LONG)
             .show();
    }

    private void showConfirmDialogBeforeQuit() {

        final AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(R.string.alert_dialog_confirm_quit_title,
                                                                                R.string.alert_dialog_confirm_quit_message);
        alertDialog.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialog.show(getSupportFragmentManager(),
                         QUIT_ACTION_DIALOG);
    }
}
