package com.makina.ecrins.flora.ui.frequencies;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Taxon;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all <code>Frequency*Fragment</code> fragments implementations.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequencyFragmentActivity extends ActionBarActivity implements OnClickListener {

    private static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";

    private FrequencyFragmentHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_frequency);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.buttonFinish).setOnClickListener(this);

        if ((((MainApplication) getApplication()).getInput().getCurrentSelectedTaxon() == null) ||
                ((((MainApplication) getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null)) ||
                ((((MainApplication) getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                        (((Taxon) ((MainApplication) getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                .getFrequency() == null))) {
            finish();
        }
        else {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();

            switch (((Taxon) ((MainApplication) getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea().getFrequency().getType()) {
                case ESTIMATION:
                    Fragment frequencyEstimationFragment = fm
                            .findFragmentByTag(FrequencyEstimationFragment.class.getSimpleName());

                    if (frequencyEstimationFragment == null) {
                        Log.d(FrequencyFragmentActivity.class
                                .getName(), "create FrequencyEstimationFragment");

                        transaction
                                .replace(R.id.fragment_frequency_container, new FrequencyEstimationFragment(), FrequencyEstimationFragment.class
                                        .getSimpleName());
                    }
                    else {
                        transaction
                                .replace(R.id.fragment_frequency_container, frequencyEstimationFragment);
                    }

                    transaction.commit();
                    break;
                case TRANSECT:
                    Fragment frequencyTransectFragment = fm
                            .findFragmentByTag(FrequencyTransectFragment.class.getSimpleName());

                    if (frequencyTransectFragment == null) {
                        Log.d(FrequencyFragmentActivity.class
                                .getName(), "create FrequencyTransectFragment");

                        transaction
                                .replace(R.id.fragment_frequency_container, new FrequencyTransectFragment(), FrequencyTransectFragment.class
                                        .getSimpleName());
                    }
                    else {
                        transaction
                                .replace(R.id.fragment_frequency_container, frequencyTransectFragment);
                    }

                    transaction.commit();
                    break;
            }

            mHandler = new FrequencyFragmentHandler(this);

            // restore ChooseActionDialogFragment state after resume if needed
            ChooseActionDialogFragment chooseActionDialogFragment = (ChooseActionDialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

            if (chooseActionDialogFragment != null) {
                chooseActionDialogFragment.setHandler(mHandler);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showConfirmDialogIfNeeded();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        showConfirmDialogIfNeeded();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonFinish:
                showConfirmDialogIfNeeded();
                break;
        }
    }

    private void showConfirmDialogIfNeeded() {
        switch (((Taxon) ((MainApplication) getApplication()).getInput().getCurrentSelectedTaxon())
                .getCurrentSelectedArea().getFrequency().getType()) {
            case TRANSECT:
                List<Integer> actions = new ArrayList<>();
                Collections.addAll(actions, R.string.choose_action_yes, R.string.choose_action_no);
                final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment
                        .newInstance(R.string.choose_action_title_quit_step, actions);
                chooseActionDialogFragment.setHandler(mHandler);
                chooseActionDialogFragment
                        .show(getSupportFragmentManager(), CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);
                break;
            default:
                finish();
        }
    }

    private static class FrequencyFragmentHandler extends Handler {

        private final WeakReference<FrequencyFragmentActivity> mFrequencyFragmentActivity;

        public FrequencyFragmentHandler(FrequencyFragmentActivity pFrequencyFragmentActivity) {
            super();

            mFrequencyFragmentActivity = new WeakReference<>(pFrequencyFragmentActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            FrequencyFragmentActivity frequencyFragmentActivity = mFrequencyFragmentActivity.get();
            ChooseActionDialogFragment dialogFragment = (ChooseActionDialogFragment) frequencyFragmentActivity
                    .getSupportFragmentManager()
                    .findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

            switch (msg.what) {
                case R.string.choose_action_yes:
                    dialogFragment.dismiss();
                    frequencyFragmentActivity.finish();
                    break;
                case R.string.choose_action_no:
                    dialogFragment.dismiss();
                    break;
            }
        }
    }
}
