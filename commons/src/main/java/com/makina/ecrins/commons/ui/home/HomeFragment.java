package com.makina.ecrins.commons.ui.home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractInputIntentService;
import com.makina.ecrins.commons.input.InputHelper;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractAppSettingsIntentService;
import com.makina.ecrins.commons.util.PermissionUtils;

/**
 * Home screen {@code Fragment}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class HomeFragment
        extends Fragment {

    private static final String TAG = HomeFragment.class.getName();

    private View mLayout;
    private HomeAdapter mHomeAdapter;
    private Button mButtonStartInput;
    private OnHomeFragmentListener mOnHomeFragmentListener;

    private AbstractAppSettings mAppSettings;
    private boolean mRequestPermissionsResult;

    private static final int REQUEST_EXTERNAL_STORAGE = 0;

    private InputHelper mInputHelper;
    private InputHelper.OnInputHelperListener mOnInputHelperListener = new InputHelper.OnInputHelperListener() {
        @NonNull
        @Override
        public AbstractInput createInput() {
            return mOnHomeFragmentListener.createInput();
        }

        @NonNull
        @Override
        public Class<? extends AbstractInputIntentService> getInputIntentServiceClass() {
            return mOnHomeFragmentListener.getInputIntentServiceClass();
        }

        @Override
        public void onReadInput(@NonNull AbstractInputIntentService.Status status) {
            switch (status) {
                case FINISHED_WITH_ERRORS:
                case FINISHED_NOT_FOUND:
                case FINISHED:
                    mButtonStartInput.setEnabled(mAppSettings != null);

                    final AbstractInput input = mInputHelper.getInput();

                    if (input != null) {
                        mOnHomeFragmentListener.onContinueInput(input.getInputId());

                        Toast.makeText(getContext(),
                                       getString(R.string.message_input_reloaded),
                                       Toast.LENGTH_LONG)
                             .show();
                    }

                    break;
            }
        }

        @Override
        public void onSaveInput(@NonNull AbstractInputIntentService.Status status) {
            // nothing to do ...
        }

        @Override
        public void onDeleteInput(@NonNull AbstractInputIntentService.Status status) {
            // nothing to do ...
        }

        @Override
        public void onExportInput(@NonNull AbstractInputIntentService.Status status) {
            // nothing to do ...
        }
    };

    private final HomeAdapter.OnHomeAdapterListener mOnHomeAdapterListener = new HomeAdapter.OnHomeAdapterListener() {
        @Override
        public void onStartSync() {
            mOnHomeFragmentListener.onStartSync();
        }
    };

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
                        mAppSettings = appSettings;

                        if (appSettings != null) {
                            mOnHomeFragmentListener.onAppSettingsLoaded(appSettings);
                            mHomeAdapter.setAppSettings(appSettings);

                            if (getActivity() != null) {
                                ActivityCompat.invalidateOptionsMenu(getActivity());
                            }

                            mInputHelper.readInput();
                        }

                        break;
                }
            }
        }
    };

    @NonNull
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestPermissionsResult = true;

        mInputHelper = new InputHelper(getContext(),
                                       mOnInputHelperListener);


        mHomeAdapter = new HomeAdapter(getContext(),
                                       mOnHomeAdapterListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home,
                                container,
                                false);
    }

    @Override
    public void onViewCreated(View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        setHasOptionsMenu(true);

        mLayout = view.findViewById(android.R.id.content);

        final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(false);
        // use a linear layout manager as default layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mHomeAdapter);

        mButtonStartInput = (Button) view.findViewById(R.id.buttonStartInput);
        mButtonStartInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnHomeFragmentListener.onStartInput();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,
                                  inflater);

        inflater.inflate(R.menu.settings,
                         menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem menuItemSettings = menu.findItem(R.id.menu_settings);
        menuItemSettings.setEnabled(mAppSettings != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            mOnHomeFragmentListener.onShowSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        mButtonStartInput.setEnabled(false);

        LocalBroadcastManager.getInstance(getContext())
                             .registerReceiver(mBroadcastReceiver,
                                               new IntentFilter(getBroadcastActionReadAppSettings()));

        mInputHelper.resume();

        if (mRequestPermissionsResult) {
            PermissionUtils.checkSelfPermissions(getContext(),
                                                 new PermissionUtils.OnCheckSelfPermissionListener() {
                                                     @Override
                                                     public void onPermissionsGranted() {
                                                         loadAppSettings();
                                                     }

                                                     @Override
                                                     public void onRequestPermissions(@NonNull String... permissions) {
                                                         PermissionUtils.requestPermissions(HomeFragment.this,
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
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getContext())
                             .unregisterReceiver(mBroadcastReceiver);
        mInputHelper.dispose();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnHomeFragmentListener) {
            mOnHomeFragmentListener = (OnHomeFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnHomeFragmentListener");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                mRequestPermissionsResult = PermissionUtils.checkPermissions(grantResults);

                if (mRequestPermissionsResult) {
                    Snackbar.make(mLayout,
                                  R.string.snackbar_permission_external_storage_available,
                                  Snackbar.LENGTH_SHORT)
                            .show();
                }
                else {
                    Snackbar.make(mLayout,
                                  R.string.snackbar_permissions_not_granted,
                                  Snackbar.LENGTH_SHORT)
                            .show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                                                 permissions,
                                                 grantResults);
        }
    }

    private void loadAppSettings() {
        AbstractAppSettingsIntentService.readSettings(getContext(),
                                                      mOnHomeFragmentListener.getAppSettingsIntentServiceClass(),
                                                      getBroadcastActionReadAppSettings(),
                                                      getAppSettingsFilename());
    }

    @NonNull
    private String getBroadcastActionReadAppSettings() {
        return getContext().getPackageName() + ".broadcast.settings.read";
    }

    @NonNull
    private String getAppSettingsFilename() {
        final String packageName = getContext().getPackageName();

        return "settings_" + packageName.substring(packageName.lastIndexOf('.') + 1) + ".json";
    }

    private void showAppSettingsLoadingFailedAlert() {
        Toast.makeText(getContext(),
                       getString(R.string.message_settings_not_found,
                                 getAppSettingsFilename()),
                       Toast.LENGTH_LONG)
             .show();
    }

    /**
     * Callback used by {@link HomeFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnHomeFragmentListener {

        @NonNull
        AbstractInput createInput();

        @NonNull
        Class<? extends AbstractInputIntentService> getInputIntentServiceClass();

        @NonNull
        Class<? extends AbstractAppSettingsIntentService> getAppSettingsIntentServiceClass();

        void onAppSettingsLoaded(@NonNull final AbstractAppSettings appSettings);

        void onShowSettings();

        void onStartSync();

        void onStartInput();

        void onContinueInput(long inputId);
    }
}
