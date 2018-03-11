package com.geonature.mobile.commons.ui.observers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.geonature.mobile.commons.input.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Let the user to choose an {@link Observer} from the list.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see ObserverListFragment
 */
public abstract class AbstractObserverListActivity
        extends AppCompatActivity
        implements ObserverListFragment.OnObserverListFragmentListener {

    public static final String EXTRA_CHOICE_MODE = "EXTRA_CHOICE_MODE";
    public static final String EXTRA_INPUT_FILTER = "EXTRA_INPUT_FILTER";
    public static final String EXTRA_SELECTED_OBSERVERS = "EXTRA_SELECTED_OBSERVERS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        List<Observer> selectedObservers = new ArrayList<>();

        final Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            selectedObservers = bundle.getParcelableArrayList(EXTRA_SELECTED_OBSERVERS);

            if (selectedObservers == null) {
                selectedObservers = new ArrayList<>();
            }
        }

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                                   .replace(android.R.id.content,
                                            ObserverListFragment.newInstance(bundle == null ? ListView.CHOICE_MODE_SINGLE : bundle.getInt(EXTRA_CHOICE_MODE,
                                                                                                                                          ListView.CHOICE_MODE_SINGLE),
                                                                             bundle.getInt(EXTRA_INPUT_FILTER),
                                                                             selectedObservers))
                                   .commit();
    }

    @Override
    public void onSelectedObservers(@NonNull Map<Long, Observer> selectedObservers) {
        final Intent intent = new Intent();
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_OBSERVERS,
                                           new ArrayList<>(selectedObservers.values()));

        setResult(RESULT_OK,
                  intent);

        finish();
    }
}
