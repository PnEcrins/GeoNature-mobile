package com.makina.ecrins.flora.ui.input.area;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.maps.jts.geojson.Feature;

/**
 * Compute the area according to the selected {@link Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class AreaInputFragment
        extends Fragment
        implements IValidateFragment,
                   IInputFragment {

    private static final String TAG = AreaInputFragment.class.getName();

    private AreaAdapter mAreaAdapter;

    private Input mInput;

    private final AreaAdapter.OnAreaAdapterListener mOnAreaAdapterListener = new AreaAdapter.OnAreaAdapterListener() {
        @Override
        public void onAreaComputed(double incline,
                                   double area,
                                   double computedArea) {
            if (mInput == null) {
                Log.w(TAG,
                      "onAreaComputed: null input");
                return;
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onAreaComputed, incline: " + incline + ", area: " + area + ", computedArea: " + computedArea);
            }

            final Area currentSelectedArea = mInput.getCurrentSelectedTaxon() != null ? ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() : null;

            if (currentSelectedArea != null) {
                currentSelectedArea.setInclineValue(incline);
                currentSelectedArea.setArea(area);
                currentSelectedArea.setComputedArea(computedArea);
            }

            ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAreaAdapter = new AreaAdapter(getContext(),
                                       getLoaderManager(),
                                       mOnAreaAdapterListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_area,
                                container,
                                false);
    }

    @Override
    public void onViewCreated(View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(false);
        // use a linear layout manager as default layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAreaAdapter);
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_area_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        if (mInput == null) {
            return false;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        return currentSelectedTaxon != null && currentSelectedTaxon.getCurrentSelectedArea() != null && (currentSelectedTaxon.getCurrentSelectedArea()
                                                                                                                             .getComputedArea() > 0.0);
    }

    @Override
    public void refreshView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                                           .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (mInput == null) {
            Log.w(TAG,
                  "refreshView: null input");
            return;
        }

        // refreshes title
        if (mInput.getCurrentSelectedTaxon() == null) {
            getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                 getString(R.string.area_none)));
        }
        else {
            final Area selectedArea = ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea();

            if ((selectedArea == null) || (selectedArea.getFeature() == null)) {
                Log.w(TAG,
                      "getResourceTitle : no feature selected!");

                getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                     getString(R.string.area_none)));
            }
            else {
                switch (selectedArea.getFeature()
                                    .getGeometry()
                                    .getGeometryType()) {
                    case "Point":
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_point)));
                        break;
                    case "LineString":
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_path)));
                        break;
                    case "Polygon":
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_polygon)));
                        break;
                    default:
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_none)));
                        break;
                }

                mAreaAdapter.setArea(selectedArea);
            }
        }
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }
}
