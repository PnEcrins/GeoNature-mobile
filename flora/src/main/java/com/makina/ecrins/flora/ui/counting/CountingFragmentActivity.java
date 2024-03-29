package com.makina.ecrins.flora.ui.counting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Counting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all <code>Counting*Fragment</code> fragments implementations.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingFragmentActivity
        extends AppCompatActivity
        implements OnClickListener,
                   OnCountingListener {

    private static final String TAG = CountingFragmentActivity.class.getName();

    public static final String EXTRA_AREA = "EXTRA_AREA";
    public static final String EXTRA_COUNTING = "EXTRA_COUNTING";

    private static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";

    private Button mButtonFinish;

    private Counting mCounting;

    private final ChooseActionDialogFragment.OnChooseActionDialogListener mOnChooseActionDialogListener = new ChooseActionDialogFragment.OnChooseActionDialogListener() {
        @Override
        public void onItemClick(DialogInterface dialog,
                                int position,
                                int actionResourceId) {
            switch (actionResourceId) {
                case R.string.choose_action_yes:
                    dialog.dismiss();
                    CountingFragmentActivity.this.finishAndSendResult();
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

        setContentView(R.layout.activity_counting);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mButtonFinish = (Button) findViewById(R.id.buttonFinish);
        mButtonFinish.setOnClickListener(this);

        final Bundle bundle = getIntent().getExtras();
        final Area area = bundle.getParcelable(EXTRA_AREA);
        mCounting = (savedInstanceState == null) ? (Counting) bundle.getParcelable(EXTRA_COUNTING) : (Counting) savedInstanceState.getParcelable(EXTRA_COUNTING);

        if ((area == null) || (mCounting == null)) {
            Log.w(TAG,
                  "invalid parameters");

            finish();
        }
        else {
            if (savedInstanceState == null) {
                final FragmentManager fm = getSupportFragmentManager();

                switch (mCounting.getType()) {
                    case EXHAUSTIVE:
                        Fragment countingExhaustiveFragment = fm.findFragmentByTag(CountingExhaustiveFragment.class.getSimpleName());

                        if (countingExhaustiveFragment == null) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG,
                                      "create CountingExhaustiveFragment");
                            }

                            fm.beginTransaction()
                              .replace(R.id.fragment_counting_container,
                                       CountingExhaustiveFragment.newInstance(mCounting),
                                       CountingExhaustiveFragment.class.getSimpleName())
                              .commit();
                        }
                        else {
                            fm.beginTransaction()
                              .replace(R.id.fragment_counting_container,
                                       countingExhaustiveFragment)
                              .commit();
                        }

                        break;
                    case SAMPLING:
                        Fragment countingSamplingFragment = fm.findFragmentByTag(CountingSamplingFragment.class.getSimpleName());

                        if (countingSamplingFragment == null) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG,
                                      "create CountingSamplingFragment");
                            }

                            fm.beginTransaction()
                              .replace(R.id.fragment_counting_container,
                                       CountingSamplingFragment.newInstance(area,
                                                                            mCounting),
                                       CountingSamplingFragment.class.getSimpleName())
                              .commit();
                        }
                        else {
                            fm.beginTransaction()
                              .replace(R.id.fragment_counting_container,
                                       countingSamplingFragment)
                              .commit();
                        }

                        break;
                    default:
                        Log.w(TAG,
                              "invalid counting type " + mCounting.getType()
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

        outState.putParcelable(EXTRA_COUNTING,
                               mCounting);
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
    public void OnCountingUpdated(@NonNull Counting counting,
                                  boolean enableFinish) {
        mCounting = counting;

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enableFinish);
        }

        mButtonFinish.setEnabled(enableFinish);
    }

    private void showConfirmDialogIfNeeded() {
        switch (mCounting.getType()) {
            case SAMPLING:
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
        intent.putExtra(EXTRA_COUNTING,
                        mCounting);

        setResult(RESULT_OK,
                  intent);

        finish();
    }
}
