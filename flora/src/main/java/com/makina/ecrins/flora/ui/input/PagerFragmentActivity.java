package com.makina.ecrins.flora.ui.input;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractInputIntentService;
import com.makina.ecrins.commons.input.InputHelper;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.AbstractNavigationHistoryPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.InputIntentService;
import com.makina.ecrins.flora.ui.input.area.AreaInputFragment;
import com.makina.ecrins.flora.ui.input.counting.CountingListFragment;
import com.makina.ecrins.flora.ui.input.disturbances.DisturbancesFragment;
import com.makina.ecrins.flora.ui.input.frequencies.FrequenciesListFragment;
import com.makina.ecrins.flora.ui.input.map.WebViewFragment;
import com.makina.ecrins.flora.ui.input.observers.ObserversAndDateInputFragment;
import com.makina.ecrins.flora.ui.input.phenology.PhenologyListFragment;
import com.makina.ecrins.flora.ui.input.physiognomy.PhysiognomyFragment;
import com.makina.ecrins.flora.ui.input.remarks.RemarksFragment;
import com.makina.ecrins.flora.ui.input.taxa.TaxaFoundFragment;
import com.makina.ecrins.flora.ui.input.taxa.TaxaInputListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code ViewPager} implementation as {@code AppCompatActivity} with navigation history support.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PagerFragmentActivity
        extends AbstractNavigationHistoryPagerFragmentActivity
        implements OnInputFragmentListener {

    private static final String TAG = PagerFragmentActivity.class.getName();

    protected static final String ALERT_CANCEL_DIALOG_FRAGMENT = "alert_cancel_dialog_fragment";
    protected static final String PROGRESS_DIALOG_FRAGMENT = "progress_dialog";
    protected static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";

    public static final String EXTRA_NEW_INPUT = "extra_new_input";

    private long mInputIdSaved;

    private final AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {
            mInputHelper.saveInput();
            PagerFragmentActivity.this.finish();
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            // nothing to do ...
        }
    };

    private final ChooseActionDialogFragment.OnChooseActionDialogListener mOnChooseActionDialogListener = new ChooseActionDialogFragment.OnChooseActionDialogListener() {
        @Override
        public void onItemClick(DialogInterface dialog,
                                int position,
                                int actionResourceId) {
            switch (actionResourceId) {
                case R.string.alert_dialog_action_start_new_input:
                    dialog.dismiss();
                    mInputHelper.deleteInput();

                    // restart the current activity
                    finish();

                    final Intent intent = getIntent();
                    intent.putExtra(EXTRA_NEW_INPUT,
                                    true);
                    intent.removeExtra(EXTRA_PAGER_ID);

                    startActivity(intent);

                    break;
                case R.string.alert_dialog_action_close_app:
                    dialog.dismiss();
                    mInputHelper.deleteInput();
                    ((MainApplication) getApplication()).setCloseApplication(true);
                    finish();

                    break;
            }
        }
    };

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
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onReadInput, " + status.name());
            }

            switch (status) {
                case FINISHED_WITH_ERRORS:
                case FINISHED_NOT_FOUND:
                case FINISHED:
                    AbstractInput input = mInputHelper.getInput();

                    if (input == null) {
                        Log.w(TAG,
                              "onReadInput, status: " + status + ", no input found");

                        input = mInputHelper.startInput();
                    }
                    else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG,
                                  "onReadInput, continue input: " + input.getInputId());
                        }
                    }

                    mPager.setId(input.getInputId());

                    final IValidateFragment pageFragment = getCurrentPageFragment();

                    if (pageFragment instanceof IInputFragment) {
                        ((IInputFragment) pageFragment).setInput(input);
                        pageFragment.refreshView();
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onExportInput, " + status.name());
            }

            final AbstractInput input = mInputHelper.getInput();
            ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_FRAGMENT);

            switch (status) {
                case STARTING:
                    showProgressDialog(R.string.progress_title,
                                       R.string.progress_message_saving_current_input,
                                       ProgressDialog.STYLE_SPINNER,
                                       0);
                    break;
                case FINISHED:
                    if (dialogFragment != null) {
                        dialogFragment.dismiss();
                    }

                    if (input == null) {
                        Log.w(TAG,
                              "onExportInput, status: " + status + ", no input found");

                        return;
                    }

                    mInputIdSaved = input.getInputId();

                    showChooseActionDialog();

                    break;
                case FINISHED_WITH_ERRORS:
                    if (dialogFragment != null) {
                        dialogFragment.dismiss();
                    }

                    Toast.makeText(PagerFragmentActivity.this,
                                   R.string.message_saving_current_input_failed,
                                   Toast.LENGTH_LONG)
                         .show();

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onCreate");
        }

        mInputHelper = new InputHelper(this,
                                       mOnInputHelperListener);

        if (getIntent().getBooleanExtra(EXTRA_NEW_INPUT,
                                        true)) {
            // instantiates a new Input
            mPager.setId(mInputHelper.startInput()
                                     .getInputId());
        }
        else {
            mInputHelper.readInput();
        }

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

    @Override
    protected void onResume() {
        super.onResume();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onResume");
        }

        mInputHelper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onPause");
        }

        mInputHelper.dispose();
    }

    @NonNull
    @Override
    protected Map<Integer, IValidateFragment> getPagerFragments() {
        final Map<Integer, IValidateFragment> fragments = new LinkedHashMap<>();

        WebViewFragment mapAPFragment = new WebViewFragment();

        WebViewFragment mapPAFragment = new WebViewFragment();
        mapPAFragment.getArguments()
                     .putBoolean(WebViewFragment.KEY_AP,
                                 false);
        mapPAFragment.getArguments()
                     .putBoolean(WebViewFragment.KEY_ADD_MARKER,
                                 false);
        mapPAFragment.getArguments()
                     .putBoolean(WebViewFragment.KEY_ADD_PATH,
                                 false);

        fragments.put(R.string.pager_fragment_observers_and_date_title,
                      new ObserversAndDateInputFragment());
        fragments.put(R.string.pager_fragment_taxa_title,
                      new TaxaInputListFragment());
        fragments.put(R.string.pager_fragment_taxa_found_title,
                      new TaxaFoundFragment());
        fragments.put(R.string.pager_fragment_webview_ap_title,
                      mapAPFragment);
        fragments.put(R.string.pager_fragment_area_title,
                      new AreaInputFragment());
        fragments.put(R.string.pager_fragment_frequencies_title,
                      new FrequenciesListFragment());
        fragments.put(R.string.pager_fragment_phenology_title,
                      new PhenologyListFragment());
        fragments.put(R.string.pager_fragment_counting_title,
                      new CountingListFragment());
        fragments.put(R.string.pager_fragment_physiognomy_title,
                      new PhysiognomyFragment());
        fragments.put(R.string.pager_fragment_disturbances_title,
                      new DisturbancesFragment());
        fragments.put(R.string.pager_fragment_remarks_title,
                      new RemarksFragment());
        fragments.put(R.string.pager_fragment_choose_action_title,
                      new ChooseActionListFragment());
        fragments.put(R.string.pager_fragment_webview_pa_title,
                      mapPAFragment);

        return fragments;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        final AbstractInput input = mInputHelper.getInput();

        if (input == null) {
            mInputHelper.readInput();
        }
        else {
            final IValidateFragment pageFragment = getCurrentPageFragment();

            if (pageFragment instanceof IInputFragment) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "onPageSelected: " + position + ", " + pageFragment.getClass()
                                                                             .getName());
                }

                ((IInputFragment) pageFragment).setInput(input);
                pageFragment.refreshView();
                validateCurrentPage();
            }
        }
    }

    @Override
    protected void performFinishAction() {
        final Input input = (Input) mInputHelper.getInput();

        if (input == null) {
            Log.w(TAG,
                  "performFinishAction: no input found");

            // TODO: reload the input by calling readInput()
            Toast.makeText(PagerFragmentActivity.this,
                           R.string.message_saving_current_input_failed,
                           Toast.LENGTH_LONG)
                 .show();

            return;
        }

        if (mInputIdSaved == input.getInputId()) {
            showChooseActionDialog();
        }
        else {
            mInputHelper.exportInput();
        }
    }

    @Override
    public void onSaveInput() {
        mInputHelper.saveInput();
    }

    @Override
    public void onCloseApplication() {
        ((MainApplication) getApplication()).setCloseApplication(true);
        finish();
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(R.string.alert_dialog_confirm_cancel_title,
                        R.string.alert_dialog_confirm_cancel_input_all_message);
    }

    protected void showAlertDialog(int titleResourceId,
                                   int messageResourceId) {
        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(titleResourceId,
                                                                                        messageResourceId);
        alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialogFragment.show(getSupportFragmentManager(),
                                 ALERT_CANCEL_DIALOG_FRAGMENT);
    }

    protected void showProgressDialog(int titleResourceId,
                                      int messageResourceId,
                                      int progressStyle,
                                      int max) {
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(titleResourceId,
                                                                                           messageResourceId,
                                                                                           progressStyle,
                                                                                           max);
        progressDialogFragment.show(getSupportFragmentManager(),
                                    PROGRESS_DIALOG_FRAGMENT);
    }

    protected void showChooseActionDialog() {
        // display a confirmation dialog to choose an action
        final List<Integer> actions = new ArrayList<>();
        Collections.addAll(actions,
                           R.string.alert_dialog_action_start_new_input,
                           R.string.alert_dialog_action_close_app);
        final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment.newInstance(R.string.alert_dialog_confirm_quit_finish_title,
                                                                                                             R.string.alert_dialog_confirm_quit_finish_message,
                                                                                                             actions);
        chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        chooseActionDialogFragment.show(getSupportFragmentManager(),
                                        CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);
    }
}
