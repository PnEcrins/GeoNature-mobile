package com.makina.ecrins.flora.ui.counting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Taxon;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all <code>Counting*Fragment</code> fragments implementations.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingFragmentActivity extends ActionBarActivity implements OnClickListener {

    private static final String CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT = "choose_quit_action_dialog";

    private CountingFragmentHandler mHandler;
    private Button mButtonFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_counting);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mButtonFinish = (Button) findViewById(R.id.buttonFinish);
        mButtonFinish.setOnClickListener(this);

        if ((((MainApplication) getApplication()).getInput()
                .getCurrentSelectedTaxon() == null) ||
                ((((MainApplication) getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null))) {
            finish();
        }
        else {
            FragmentManager fm = getSupportFragmentManager();

            switch (((Taxon) ((MainApplication) getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getType()) {
                case EXHAUSTIVE:
                    Fragment countingExhaustiveFragment = fm.findFragmentByTag(CountingExhaustiveFragment.class.getSimpleName());

                    if (countingExhaustiveFragment == null) {
                        Log.d(CountingFragmentActivity.class.getName(), "create CountingExhaustiveFragment");

                        fm.beginTransaction()
                                .replace(R.id.fragment_counting_container, new CountingExhaustiveFragment(), CountingExhaustiveFragment.class.getSimpleName())
                                .commit();
                    }
                    else {
                        fm.beginTransaction()
                                .replace(R.id.fragment_counting_container, countingExhaustiveFragment)
                                .commit();
                    }

                    break;
                case SAMPLING:
                    Fragment countingSamplingFragment = fm.findFragmentByTag(CountingSamplingFragment.class.getSimpleName());

                    if (countingSamplingFragment == null) {
                        Log.d(CountingFragmentActivity.class.getName(), "create CountingSamplingFragment");

                        fm.beginTransaction()
                                .replace(R.id.fragment_counting_container, new CountingSamplingFragment(), CountingSamplingFragment.class.getSimpleName())
                                .commit();
                    }
                    else {
                        fm.beginTransaction()
                                .replace(R.id.fragment_counting_container, countingSamplingFragment)
                                .commit();
                    }

                    break;
                default:
                    // nothing to do at this point, so we finish this activity
                    finish();
            }

            mHandler = new CountingFragmentHandler(this);

            // restore ChooseActionDialogFragment state after resume if needed
            ChooseActionDialogFragment chooseActionDialogFragment = (ChooseActionDialogFragment) getSupportFragmentManager().findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

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

    protected void enableFinish(boolean enabled) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
        mButtonFinish.setEnabled(enabled);
    }

    private void showConfirmDialogIfNeeded() {
        switch (((Taxon) ((MainApplication) getApplication()).getInput()
                .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                .getCounting()
                .getType()) {
            case SAMPLING:
                List<Integer> actions = new ArrayList<Integer>();
                Collections.addAll(actions, R.string.choose_action_yes, R.string.choose_action_no);
                final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment.newInstance(R.string.choose_action_title_quit_step, actions);
                chooseActionDialogFragment.setHandler(mHandler);
                chooseActionDialogFragment.show(getSupportFragmentManager(), CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);
                break;
            default:
                finish();
        }
    }

    private static class CountingFragmentHandler extends Handler {

        private final WeakReference<CountingFragmentActivity> mCountingFragmentActivity;

        public CountingFragmentHandler(CountingFragmentActivity pCountingFragmentActivity) {
            super();

            mCountingFragmentActivity = new WeakReference<CountingFragmentActivity>(pCountingFragmentActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            CountingFragmentActivity countingFragmentActivity = mCountingFragmentActivity.get();
            ChooseActionDialogFragment dialogFragment = (ChooseActionDialogFragment) countingFragmentActivity.getSupportFragmentManager()
                    .findFragmentByTag(CHOOSE_QUIT_ACTION_DIALOG_FRAGMENT);

            switch (msg.what) {
                case R.string.choose_action_yes:
                    dialogFragment.dismiss();
                    countingFragmentActivity.finish();
                    break;
                case R.string.choose_action_no:
                    dialogFragment.dismiss();
                    break;
            }
        }
    }
}
