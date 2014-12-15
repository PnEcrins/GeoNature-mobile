package com.makina.ecrins.flora.ui.input;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.makina.ecrins.commons.input.SaveInputAsyncTask;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;
import com.makina.ecrins.commons.ui.pager.AbstractNavigationHistoryPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.ui.input.area.AreaFragment;
import com.makina.ecrins.flora.ui.input.counting.CountingListFragment;
import com.makina.ecrins.flora.ui.input.disturbances.DisturbancesFragment;
import com.makina.ecrins.flora.ui.input.frequencies.FrequenciesListFragment;
import com.makina.ecrins.flora.ui.input.map.WebViewFragment;
import com.makina.ecrins.flora.ui.input.observers.ObserversAndDateFragment;
import com.makina.ecrins.flora.ui.input.phenology.PhenologyListFragment;
import com.makina.ecrins.flora.ui.input.physiognomy.PhysiognomyFragment;
import com.makina.ecrins.flora.ui.input.remarks.RemarksFragment;
import com.makina.ecrins.flora.ui.input.taxa.TaxaFoundFragment;
import com.makina.ecrins.flora.ui.input.taxa.TaxaFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic <code>ViewPager</code> implementation as <code>FragmentActivity</code>.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PagerFragmentActivity extends AbstractNavigationHistoryPagerFragmentActivity {

    private static final String TAG = PagerFragmentActivity.class.getName();

    protected static final String ALERT_CANCEL_DIALOG_FRAGMENT = "alert_cancel_dialog_fragment";
    protected static final String PROGRESS_DIALOG_FRAGMENT = "progress_dialog";
    protected static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";

    protected static final String KEY_INPUT_SAVED = "input_saved";

    private final AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonListener(DialogInterface dialog) {
            PagerFragmentActivity.this.finish();
        }

        @Override
        public void onNegativeButtonListener(DialogInterface dialog) {
            // nothing to do ...
        }
    };

    private final ChooseActionDialogFragment.OnChooseActionDialogListener mOnChooseActionDialogListener = new ChooseActionDialogFragment.OnChooseActionDialogListener() {
        @Override
        public void onItemClick(
                DialogInterface dialog,
                int position,
                int actionResourceId) {
            switch (actionResourceId) {
                case R.string.alert_dialog_action_start_new_input:
                    // instantiates a new Input
                    ((MainApplication) getApplication()).setInput(new Input());

                    if (BuildConfig.DEBUG) {
                        Log.d(
                                TAG,
                                "start new input: " + ((MainApplication) getApplication()).getInput()
                                        .getInputId()
                        );
                    }

                    goBackInHistory(R.string.pager_fragment_observers_and_date_title);

                    break;
                case R.string.alert_dialog_action_close_app:
                    ((MainApplication) getApplication()).setCloseApplication(true);
                    finish();

                    break;
                case R.string.alert_dialog_action_cancel_alert_dialog:
                    final ChooseActionDialogFragment chooseActionDialogFragmentToDismiss = (ChooseActionDialogFragment) getSupportFragmentManager().findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

                    if (chooseActionDialogFragmentToDismiss != null) {
                        chooseActionDialogFragmentToDismiss.dismiss();
                    }

                    break;
            }
        }
    };

    private static class PagerFragmentHandler extends Handler {

        private final WeakReference<PagerFragmentActivity> mPagerFragmentActivity;

        public PagerFragmentHandler(PagerFragmentActivity pPagerFragmentActivity) {
            super();

            mPagerFragmentActivity = new WeakReference<>(pPagerFragmentActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            PagerFragmentActivity pagerFragmentActivity = mPagerFragmentActivity.get();
            ProgressDialogFragment dialogFragment = (ProgressDialogFragment) pagerFragmentActivity
                    .getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_FRAGMENT);

            switch (msg.what) {
                case SaveInputAsyncTask.HANDLER_INPUT_SAVE_START:
                    pagerFragmentActivity.showProgressDialog(
                            R.string.progress_title,
                            R.string.progress_message_saving_current_input,
                            ProgressDialog.STYLE_SPINNER,
                            0);

                    break;
                case SaveInputAsyncTask.HANDLER_INPUT_SAVED:

                    if (dialogFragment != null) {
                        dialogFragment.dismiss();
                    }

                    pagerFragmentActivity.mSavedState.putLong(
                            KEY_INPUT_SAVED,
                            ((MainApplication) pagerFragmentActivity.getApplication())
                                    .getInput().getInputId());

                    pagerFragmentActivity.showChooseActionDialog();

                    break;
                case SaveInputAsyncTask.HANDLER_INPUT_SAVE_FAILED:

                    if (dialogFragment != null) {
                        dialogFragment.dismiss();
                    }

                    Toast.makeText(
                            pagerFragmentActivity,
                            R.string.message_saving_current_input_failed,
                            Toast.LENGTH_LONG)
                            .show();

                    break;
            }
        }
    }

    private PagerFragmentHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        if (savedInstanceState == null) {
            // instantiates a new Input
            ((MainApplication) getApplication()).setInput(new Input());

            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "onCreate, input: " + ((MainApplication) getApplication()).getInput()
                                .getInputId()
                );
            }
        }

        mHandler = new PagerFragmentHandler(this);

        // restore AlertDialogFragment state after resume if needed
        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(ALERT_CANCEL_DIALOG_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }

        // restore ChooseActionDialogFragment state after resume if needed
        final ChooseActionDialogFragment chooseActionDialogFragment = (ChooseActionDialogFragment) getSupportFragmentManager().findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

        if (chooseActionDialogFragment != null) {
            chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        }
    }
/*
    @Override
    protected void onPause() {
        // FIXME: Careful we dismiss dialog, cause of error after screen rotate, we lost the information of fragment (Activity, tag)
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager()
                .findFragmentByTag(ALERT_CANCEL_DIALOG_FRAGMENT);

        if (fragment != null) {
            fragment.dismiss();
        }

        super.onPause();
    }
*/
    @Override
    protected Map<Integer, IValidateFragment> getPagerFragments() {
        final Map<Integer, IValidateFragment> fragments = new LinkedHashMap<>();

        WebViewFragment mapAPFragment = new WebViewFragment();

        WebViewFragment mapPAFragment = new WebViewFragment();
        mapPAFragment.getArguments().putBoolean(WebViewFragment.KEY_AP, false);
        mapPAFragment.getArguments().putBoolean(WebViewFragment.KEY_ADD_MARKER, false);
        mapPAFragment.getArguments().putBoolean(WebViewFragment.KEY_ADD_PATH, false);

        fragments.put(R.string.pager_fragment_observers_and_date_title, new ObserversAndDateFragment());
        fragments.put(R.string.pager_fragment_taxa_title, new TaxaFragment());
        fragments.put(R.string.pager_fragment_taxa_found_title, new TaxaFoundFragment());
        fragments.put(R.string.pager_fragment_webview_ap_title, mapAPFragment);
        fragments.put(R.string.pager_fragment_area_title, new AreaFragment());
        fragments.put(R.string.pager_fragment_frequencies_title, new FrequenciesListFragment());
        fragments.put(R.string.pager_fragment_phenology_title, new PhenologyListFragment());
        fragments.put(R.string.pager_fragment_counting_title, new CountingListFragment());
        fragments.put(R.string.pager_fragment_physiognomy_title, new PhysiognomyFragment());
        fragments.put(R.string.pager_fragment_disturbances_title, new DisturbancesFragment());
        fragments.put(R.string.pager_fragment_remarks_title, new RemarksFragment());
        fragments.put(R.string.pager_fragment_choose_action_title, new ChooseActionListFragment());
        fragments.put(R.string.pager_fragment_webview_pa_title, mapPAFragment);

        return fragments;
    }

    @Override
    protected void performFinishAction() {
        if (mSavedState.getLong(KEY_INPUT_SAVED, 0) == ((MainApplication) getApplication()).getInput().getInputId()) {
            showChooseActionDialog();
        }
        else {
            saveCurrentInput();
        }
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(
                R.string.alert_dialog_confirm_cancel_title,
                R.string.alert_dialog_confirm_cancel_input_all_message
        );
    }

    protected void showAlertDialog(
            int titleResourceId,
            int messageResourceId) {
        final DialogFragment dialogFragment = AlertDialogFragment.newInstance(
                titleResourceId,
                messageResourceId,
                mOnAlertDialogListener
        );
        dialogFragment.show(
                getSupportFragmentManager(),
                ALERT_CANCEL_DIALOG_FRAGMENT
        );
    }

    protected void showProgressDialog(
            int titleResourceId,
            int messageResourceId,
            int progressStyle,
            int max) {
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(
                titleResourceId,
                messageResourceId,
                progressStyle,
                max
        );
        progressDialogFragment.show(
                getSupportFragmentManager(),
                PROGRESS_DIALOG_FRAGMENT
        );
    }

    protected void showChooseActionDialog() {
        // display a confirmation dialog to choose an action
        final List<Integer> actions = new ArrayList<>();
        Collections.addAll(
                actions,
                R.string.alert_dialog_action_start_new_input,
                R.string.alert_dialog_action_close_app,
                R.string.alert_dialog_action_cancel_alert_dialog
        );
        final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment.newInstance(
                R.string.alert_dialog_confirm_quit_finish_title,
                R.string.alert_dialog_confirm_quit_finish_message,
                actions
        );
        chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        chooseActionDialogFragment.show(
                getSupportFragmentManager(),
                CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT
        );
    }

    protected void saveCurrentInput() {
        (new SaveInputAsyncTask(this, mHandler)).execute(((MainApplication) getApplication()).getInput());
    }
}
