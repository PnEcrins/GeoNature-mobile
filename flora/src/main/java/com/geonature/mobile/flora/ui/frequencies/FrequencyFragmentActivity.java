package com.geonature.mobile.flora.ui.frequencies;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.geonature.mobile.commons.ui.dialog.ChooseActionDialogFragment;
import com.geonature.mobile.flora.BuildConfig;
import com.geonature.mobile.flora.R;
import com.geonature.mobile.flora.input.Area;
import com.geonature.mobile.flora.input.Frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all <code>Frequency*Fragment</code> fragments implementations.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequencyFragmentActivity
        extends AppCompatActivity
        implements OnClickListener,
                   OnFrequencyListener {

    private static final String TAG = FrequencyFragmentActivity.class.getName();

    public static final String EXTRA_AREA = "EXTRA_AREA";
    public static final String EXTRA_FREQUENCY = "EXTRA_FREQUENCY";

    private static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";

    private Frequency mFrequency;

    private final ChooseActionDialogFragment.OnChooseActionDialogListener mOnChooseActionDialogListener = new ChooseActionDialogFragment.OnChooseActionDialogListener() {
        @Override
        public void onItemClick(DialogInterface dialog,
                                int position,
                                int actionResourceId) {
            switch (actionResourceId) {
                case R.string.choose_action_yes:
                    dialog.dismiss();
                    FrequencyFragmentActivity.this.finishAndSendResult();
                    break;
                case R.string.choose_action_no:
                    dialog.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_frequency);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.buttonFinish).setOnClickListener(this);

        final Bundle bundle = getIntent().getExtras();
        final Area area = bundle.getParcelable(EXTRA_AREA);
        mFrequency = (savedInstanceState == null) ? (Frequency) bundle.getParcelable(EXTRA_FREQUENCY) : (Frequency) savedInstanceState.getParcelable(EXTRA_FREQUENCY);

        if ((area == null) || (mFrequency == null)) {
            Log.w(TAG,
                  "invalid parameters");

            finish();
        }
        else {
            if (savedInstanceState == null) {
                final FragmentManager fm = getSupportFragmentManager();
                final FragmentTransaction transaction = fm.beginTransaction();

                switch (mFrequency.getType()) {
                    case ESTIMATION:
                        Fragment frequencyEstimationFragment = fm.findFragmentByTag(FrequencyEstimationFragment.class.getSimpleName());

                        if (frequencyEstimationFragment == null) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG,
                                      "create FrequencyEstimationFragment");
                            }

                            transaction.replace(R.id.fragment_frequency_container,
                                                FrequencyEstimationFragment.newInstance(mFrequency),
                                                FrequencyEstimationFragment.class.getSimpleName());
                        }
                        else {
                            transaction.replace(R.id.fragment_frequency_container,
                                                frequencyEstimationFragment);
                        }

                        transaction.commit();
                        break;
                    case TRANSECT:
                        Fragment frequencyTransectFragment = fm.findFragmentByTag(FrequencyTransectFragment.class.getSimpleName());

                        if (frequencyTransectFragment == null) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG,
                                      "create FrequencyTransectFragment");
                            }

                            transaction.replace(R.id.fragment_frequency_container,
                                                FrequencyTransectFragment.newInstance(area,
                                                                                      mFrequency),
                                                FrequencyTransectFragment.class.getSimpleName());
                        }
                        else {
                            transaction.replace(R.id.fragment_frequency_container,
                                                frequencyTransectFragment);
                        }

                        transaction.commit();
                        break;
                    default:
                        Log.w(TAG,
                              "invalid frequency type " + mFrequency.getType()
                                                                    .getValue());

                        // nothing to do at this point, so we finish this activity
                        finish();
                }
            }

            // restore ChooseActionDialogFragment state after resume if needed
            final ChooseActionDialogFragment chooseActionDialogFragment = (ChooseActionDialogFragment) getSupportFragmentManager().findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

            if (chooseActionDialogFragment != null) {
                chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_FREQUENCY,
                               mFrequency);
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

    @Override
    public void OnFrequencyUpdated(@NonNull Frequency frequency) {
        mFrequency = frequency;
    }

    private void showConfirmDialogIfNeeded() {
        switch (mFrequency.getType()) {
            case TRANSECT:
                final List<Integer> actions = new ArrayList<>();
                Collections.addAll(actions,
                                   R.string.choose_action_yes,
                                   R.string.choose_action_no);
                final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment.newInstance(R.string.choose_action_title_quit_step,
                                                                                                                     actions);
                chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
                chooseActionDialogFragment.show(getSupportFragmentManager(),
                                                CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);
                break;
            default:
                finishAndSendResult();
        }
    }

    private void finishAndSendResult() {
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_FREQUENCY,
                        mFrequency);

        setResult(RESULT_OK,
                  intent);

        finish();
    }
}
