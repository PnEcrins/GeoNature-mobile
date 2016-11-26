package com.makina.ecrins.flora.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractInputIntentService;
import com.makina.ecrins.commons.input.InputHelper;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.settings.AbstractSettingsService;
import com.makina.ecrins.commons.ui.AbstractMainFragmentActivity;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.InputIntentService;
import com.makina.ecrins.flora.settings.AppSettings;
import com.makina.ecrins.flora.settings.SettingsService;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;
import com.makina.ecrins.flora.ui.settings.MainPreferencesActivity;
import com.makina.ecrins.flora.ui.sync.SynchronizationActivity;

/**
 * This is the main <code>Activity</code> of this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainFragmentActivity
        extends AbstractMainFragmentActivity {

    protected static final String ALERT_CONFIRM_CONTINUE_INPUT_DIALOG_FRAGMENT = "ALERT_CONFIRM_CONTINUE_INPUT_DIALOG_FRAGMENT";

    private InputHelper mInputHelper;
    private InputHelper.OnInputHelperListener mOnInputHelperListener = new InputHelper.OnInputHelperListener() {
        @NonNull
        @Override
        public AbstractInput createInput() {
            return new Input();
        }

        @NonNull
        @Override
        public Class<? extends AbstractInputIntentService> getInputIntentServiceClass() {
            return InputIntentService.class;
        }

        @Override
        public void onReadInput(@NonNull AbstractInputIntentService.Status status) {
            switch (status) {
                case FINISHED_WITH_ERRORS:
                case FINISHED_NOT_FOUND:
                case FINISHED:
                    mButtonStartInput.setEnabled(true);
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

    private final AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {

            final Intent intent = new Intent(MainFragmentActivity.this,
                                             PagerFragmentActivity.class);
            intent.putExtra(PagerFragmentActivity.EXTRA_NEW_INPUT,
                            false);
            startActivity(intent);
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            startActivity(new Intent(MainFragmentActivity.this,
                                     PagerFragmentActivity.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInputHelper = new InputHelper(this,
                                       mOnInputHelperListener);

        // restore AlertDialogFragment state after resume if needed
        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(ALERT_CONFIRM_CONTINUE_INPUT_DIALOG_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mButtonStartInput.setEnabled(false);
        mInputHelper.resume();
        mInputHelper.readInput();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mInputHelper.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this,
                                         MainPreferencesActivity.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartSynchronization:
                startActivity(new Intent(this,
                                         SynchronizationActivity.class));
                break;
            case R.id.buttonStartInput:
                if (mInputHelper.getInput() == null) {
                    startActivity(new Intent(this,
                                             PagerFragmentActivity.class));
                }
                else {
                    showConfirmContinueInputDialog();
                }

                break;
        }
    }

    @Override
    protected boolean isCloseApplication() {
        return ((MainApplication) getApplication()).isCloseApplication();
    }

    @Override
    protected void setCloseApplication(boolean closeApplication) {
        ((MainApplication) getApplication()).setCloseApplication(closeApplication);
    }

    @Override
    protected AbstractAppSettings getAppSettings() {
        return ((MainApplication) getApplication()).getAppSettings();
    }

    @Override
    protected void setAppSettings(AbstractAppSettings appSettings) {
        ((MainApplication) getApplication()).setAppSettings((AppSettings) appSettings);
    }

    @Override
    protected Class<? extends AbstractSettingsService> getSettingsServiceClass() {

        return SettingsService.class;
    }

    @Override
    protected boolean checkServiceMessageStatusTask() {
        return true;
    }

    @Override
    protected void performMessageStatusTaskHandler(AbstractMainFragmentActivity mainFragmentActivity,
                                                   Message msg) {
        // nothing to do ...
    }

    @Override
    protected Uri getObserverLoaderUri(long ObserverId) {
        return Uri.withAppendedPath(MainContentProvider.CONTENT_OBSERVERS_URI,
                                    Long.toString(ObserverId));
    }

    @Override
    protected Observer getDefaultObserver() {
        return ((MainApplication) getApplication()).getDefaultObserver();
    }

    @Override
    protected void setDefaultObserver(Observer observer) {
        ((MainApplication) getApplication()).setDefaultObserver(observer);
    }

    @Override
    protected int whatSettingsLoadingStart() {
        return MainApplication.HANDLER_SETTINGS_LOADING_START;
    }

    @Override
    protected int whatSettingsLoading() {
        return MainApplication.HANDLER_SETTINGS_LOADING;
    }

    @Override
    protected int whatSettingsLoadingFailed() {
        return MainApplication.HANDLER_SETTINGS_LOADED_FAILED;
    }

    @Override
    protected int whatSettingsLoadingLoaded() {
        return MainApplication.HANDLER_SETTINGS_LOADED;
    }

    private void showConfirmContinueInputDialog() {
        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.alert_dialog_confirm_input_continue_title,
                                                                                        R.string.alert_dialog_confirm_input_continue_message,
                                                                                        R.string.alert_dialog_confirm_input_continue_action_yes,
                                                                                        R.string.alert_dialog_confirm_input_continue_action_no);
        alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialogFragment.show(getSupportFragmentManager(),
                                 ALERT_CONFIRM_CONTINUE_INPUT_DIALOG_FRAGMENT);
    }
}
