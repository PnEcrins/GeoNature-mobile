package com.makina.ecrins.search.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.search.R;
import com.makina.ecrins.search.ui.FeaturesListFragment.OnFeatureSelectedListener;

/**
 * Lists all {@link com.makina.ecrins.maps.geojson.Feature}s from arguments.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FeaturesFragmentActivity
        extends AppCompatActivity
        implements OnFeatureSelectedListener {

    public static final String KEY_FEATURES = "features";
    public static final String KEY_SELECTED_FEATURE = "selected_feature";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_features);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if ((getIntent().getExtras() != null) && (getIntent().getExtras()
                                                             .containsKey(KEY_FEATURES))) {

            if (savedInstanceState == null) {
                final Bundle parameters = new Bundle();
                parameters.putParcelableArrayList(FeaturesListFragment.KEY_FEATURES,
                                                  getIntent().getExtras()
                                                             .getParcelableArrayList(KEY_FEATURES));

                final FeaturesListFragment featuresListFragment = new FeaturesListFragment();
                featuresListFragment.setArguments(parameters);

                getSupportFragmentManager().beginTransaction()
                                           .replace(android.R.id.content,
                                                    featuresListFragment)
                                           .commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public void onFeatureSelected(Feature selectedFeature) {

        final Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_SELECTED_FEATURE,
                              selectedFeature);
        setResult(Activity.RESULT_OK,
                  resultIntent);

        finish();
    }
}
