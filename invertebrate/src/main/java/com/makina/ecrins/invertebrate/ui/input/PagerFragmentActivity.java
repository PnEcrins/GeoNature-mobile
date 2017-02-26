package com.makina.ecrins.invertebrate.ui.input;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.makina.ecrins.commons.input.SaveInputAsyncTask;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.R;
import com.makina.ecrins.invertebrate.inputs.Input;
import com.makina.ecrins.invertebrate.ui.input.counting.CountingListFragment;
import com.makina.ecrins.invertebrate.ui.input.criteria.CriteriaFragment;
import com.makina.ecrins.invertebrate.ui.input.environments.EnvironmentsFragment;
import com.makina.ecrins.invertebrate.ui.input.map.WebViewFragment;
import com.makina.ecrins.invertebrate.ui.input.observers.ObserversAndDateFragment;
import com.makina.ecrins.invertebrate.ui.input.results.ResultsInputFragment;
import com.makina.ecrins.invertebrate.ui.input.taxa.TaxaFragment;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic <code>ViewPager</code> implementation as <code>FragmentActivity</code>.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PagerFragmentActivity
        extends AbstractPagerFragmentActivity {

    protected static final String ALERT_CANCEL_DIALOG_FRAGMENT = "alert_cancel_dialog_fragment";
    protected static final String PROGRESS_DIALOG_FRAGMENT = "progress_dialog";
    protected static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";
    protected static final String CHOOSE_CANCEL_ACTION_DIALOG_FRAGMENT = "choose_cancel_action_dialog";

    protected int mFinishMode;

    private final AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {

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
                case R.string.alert_dialog_confirm_quit_go_home_message:
                case R.string.alert_dialog_confirm_quit_finish_message:
                    mFinishMode = actionResourceId;
                    saveCurrentInput();
                    break;
                case R.string.alert_dialog_confirm_cancel_input_current_taxon_message:
                    // clears the current selected taxon and set the last inserted taxon as the current selected taxon
                    if (((MainApplication) getApplication()).getInput()
                                                            .getTaxa()
                                                            .size() > 1) {
                        ((MainApplication) getApplication()).getInput()
                                                            .getTaxa()
                                                            .remove(((MainApplication) getApplication()).getInput()
                                                                                                        .getCurrentSelectedTaxonId());
                        ((MainApplication) getApplication()).getInput()
                                                            .setCurrentSelectedTaxonId(((MainApplication) getApplication()).getInput()
                                                                                                                           .getLastInsertedTaxonId());

                        goToLastPage();
                    }

                    break;
                case R.string.alert_dialog_confirm_cancel_input_all_message:
                    finish();
                    break;
            }
        }
    };

    private static class PagerFragmentHandler
            extends Handler {

        private final WeakReference<PagerFragmentActivity> mPagerFragmentActivity;

        public PagerFragmentHandler(PagerFragmentActivity pPagerFragmentActivity) {

            super();
            mPagerFragmentActivity = new WeakReference<>(pPagerFragmentActivity);
        }

        @Override
        public void handleMessage(Message msg) {

            PagerFragmentActivity pagerFragmentActivity = mPagerFragmentActivity.get();
            ProgressDialogFragment dialogFragment = (ProgressDialogFragment) pagerFragmentActivity.getSupportFragmentManager()
                                                                                                  .findFragmentByTag(PROGRESS_DIALOG_FRAGMENT);

            switch (msg.what) {
                case SaveInputAsyncTask.HANDLER_INPUT_SAVE_START:
                    pagerFragmentActivity.showProgressDialog(R.string.progress_title,
                                                             R.string.progress_message_saving_current_input,
                                                             ProgressDialog.STYLE_SPINNER,
                                                             0);
                    break;
                case SaveInputAsyncTask.HANDLER_INPUT_SAVED:

                    if (dialogFragment != null) {
                        dialogFragment.dismiss();
                    }

                    switch (pagerFragmentActivity.mFinishMode) {
                        case R.string.alert_dialog_confirm_quit_go_home_message:
                            pagerFragmentActivity.finish();
                            break;
                        case R.string.alert_dialog_confirm_quit_finish_message:
                            ((MainApplication) pagerFragmentActivity.getApplication()).setCloseApplication(true);
                            pagerFragmentActivity.finish();
                            break;
                    }

                    break;
                case SaveInputAsyncTask.HANDLER_INPUT_SAVE_FAILED:

                    if (dialogFragment != null) {
                        dialogFragment.dismiss();
                    }

                    Toast.makeText(pagerFragmentActivity,
                                   R.string.message_saving_current_input_failed,
                                   Toast.LENGTH_LONG)
                         .show();

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(getClass().getName(),
              "onCreate");

        if (savedInstanceState == null) {
            // instantiates a new Input
            ((MainApplication) getApplication()).setInput(new Input());

            Log.d(getClass().getName(),
                  "onCreate, input: " + ((MainApplication) getApplication()).getInput()
                                                                            .getInputId());
        }

        // restore ChooseActionDialogFragment state after resume if needed
        ChooseActionDialogFragment chooseActionDialogFragment = (ChooseActionDialogFragment) getSupportFragmentManager().findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

        if (chooseActionDialogFragment != null) {
            chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        }

        // restore ChooseActionDialogFragment state after resume if needed
        chooseActionDialogFragment = (ChooseActionDialogFragment) getSupportFragmentManager().findFragmentByTag(CHOOSE_CANCEL_ACTION_DIALOG_FRAGMENT);

        if (chooseActionDialogFragment != null) {
            chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        }
    }

    @Override
    protected void onPause() {
        // FIXME: Careful we dismiss dialog, cause of error after screen rotate, we lost the information of fragment (Activity, tag)
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(ALERT_CANCEL_DIALOG_FRAGMENT);

        if (fragment != null) {
            fragment.dismiss();
        }

        super.onPause();
    }

    @NonNull
    @Override
    protected Map<Integer, IValidateFragment> getPagerFragments() {
        final Map<Integer, IValidateFragment> fragments = new LinkedHashMap<>();

        fragments.put(R.string.pager_fragment_observers_and_date_title,
                      new ObserversAndDateFragment());
        fragments.put(R.string.pager_fragment_webview_title,
                      new WebViewFragment());
        fragments.put(R.string.pager_fragment_environments_title,
                      new EnvironmentsFragment());
        fragments.put(R.string.pager_fragment_taxa_title,
                      new TaxaFragment());
        fragments.put(R.string.pager_fragment_counting_title,
                      new CountingListFragment());
        fragments.put(R.string.pager_fragment_criteria_title,
                      new CriteriaFragment());
        fragments.put(R.string.pager_fragment_results_input_title,
                      new ResultsInputFragment());

        return fragments;
    }

    @Override
    protected void performFinishAction() {
        // display a confirmation dialog before quitting the current input
        showChooseActionDialog(R.string.alert_dialog_confirm_quit_title,
                               CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT,
                               R.string.alert_dialog_confirm_quit_go_home_message,
                               R.string.alert_dialog_confirm_quit_finish_message);
    }

    @Override
    public void onBackPressed() {
        // only if at least two taxa ware added to this input
        if (((MainApplication) getApplication()).getInput()
                                                .getTaxa()
                                                .size() > 1) {
            showChooseActionDialog(R.string.alert_dialog_confirm_cancel_title,
                                   CHOOSE_CANCEL_ACTION_DIALOG_FRAGMENT,
                                   R.string.alert_dialog_confirm_cancel_input_current_taxon_message,
                                   R.string.alert_dialog_confirm_cancel_input_all_message);
        }
        else {
            showAlertDialog(R.string.alert_dialog_confirm_cancel_title,
                            R.string.alert_dialog_confirm_cancel_input_all_message);
        }
    }

    protected void showAlertDialog(int titleResourceId,
                                   int messageResourceId) {

        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(titleResourceId,
                                                                                        messageResourceId);
        alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialogFragment.show(getSupportFragmentManager(),
                                 ALERT_CANCEL_DIALOG_FRAGMENT);
    }

    protected void showProgressDialog(int title,
                                      int message,
                                      int progressStyle,
                                      int max) {

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(title,
                                                                                           message,
                                                                                           progressStyle,
                                                                                           max);
        progressDialogFragment.show(getSupportFragmentManager(),
                                    PROGRESS_DIALOG_FRAGMENT);
    }

    protected void showChooseActionDialog(int title,
                                          @NonNull final String tag,
                                          @NonNull final Integer... actions) {

        final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment.newInstance(title,
                                                                                                             Arrays.asList(actions));
        chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        chooseActionDialogFragment.show(getSupportFragmentManager(),
                                        tag);
    }

    protected void saveCurrentInput() {

        (new SaveInputAsyncTask(this,
                                ((MainApplication) getApplication()).getAppSettings()
                                                                    .getProtocolSettings(),
                                new PagerFragmentHandler(this))).execute(((MainApplication) getApplication()).getInput());
    }
}
