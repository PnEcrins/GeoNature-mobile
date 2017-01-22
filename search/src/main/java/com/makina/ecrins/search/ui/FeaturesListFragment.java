package com.makina.ecrins.search.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.R;

import java.util.List;

/**
 * Lists all {@link Feature}s from arguments.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FeaturesListFragment
        extends ListFragment {

    public static final String KEY_FEATURES = "features";

    private ArrayAdapter<Feature> mAdapter;

    private OnFeatureSelectedListener mOnFeatureSelectedListener;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create an empty adapter we will use to display the loaded data
        mAdapter = new ArrayAdapter<Feature>(getActivity(),
                                             android.R.layout.simple_list_item_1,
                                             android.R.id.text1) {
            @Override
            public View getView(int position,
                                View convertView,
                                ViewGroup parent) {
                View view = super.getView(position,
                                          convertView,
                                          parent);

                ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(position).getProperties()
                                                                                            .getString(MainDatabaseHelper.SearchColumns.TAXON));

                return view;
            }
        };
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        // give some text to display if there is no data
        setEmptyText(getString(R.string.taxa_no_data));

        setListAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mOnFeatureSelectedListener = (OnFeatureSelectedListener) activity;
        }
        catch (ClassCastException cce) {
            throw new ClassCastException(activity.toString() + " must implement OnFeatureSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mOnFeatureSelectedListener = null;
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(getClass().getName(),
                  "onResume");
        }

        super.onResume();

        mAdapter.clear();

        if (getArguments().containsKey(KEY_FEATURES) && (!getArguments().getParcelableArrayList(KEY_FEATURES)
                                                                        .isEmpty())) {
            List<Feature> features = getArguments().getParcelableArrayList(KEY_FEATURES);

            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(),
                      "onResume : features found : " + features.size());
            }

            for (Feature feature : features) {
                if (BuildConfig.DEBUG) {
                    Log.d(getClass().getName(),
                          "onResume : add feature " + feature.getId());
                }

                mAdapter.add(feature);
            }
        }
        else {
            mHandler.postDelayed(new Runnable() {
                                     @Override
                                     public void run() {
                                         getActivity().finish();
                                     }
                                 },
                                 1500);
        }
    }

    @Override
    public void onListItemClick(ListView l,
                                View v,
                                int position,
                                long id) {
        mOnFeatureSelectedListener.onFeatureSelected(mAdapter.getItem(position));
    }

    public interface OnFeatureSelectedListener {

        void onFeatureSelected(Feature selectedFeature);
    }
}
