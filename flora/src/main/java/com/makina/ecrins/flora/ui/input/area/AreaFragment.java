package com.makina.ecrins.flora.ui.input.area;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.maps.geojson.geometry.LineString;
import com.makina.ecrins.maps.geojson.geometry.Polygon;

/**
 * Compute the area according to the selected {@link com.makina.ecrins.maps.geojson.Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class AreaFragment
        extends Fragment
        implements IValidateFragment,
                   LoaderManager.LoaderCallbacks<Cursor>,
                   OnItemSelectedListener {

    private static final String TAG = AreaFragment.class.getName();

    private static final String KEY_SELECTED_INCLINE = "selected_incline";

    private Bundle mSavedState;

    protected TextView mTextViewPathLengthComputed = null;
    protected TextView mTextViewPolygonPlanimetricAreaComputed;
    protected EditText mEditTextPathWidth = null;
    protected Spinner mSpinnerIncline = null;

    private EditText mEditTextPointArea = null;
    private TextView mTextViewAreaComputed = null;

    private SimpleCursorAdapter mAdapter = null;

    private double mPointArea;
    private double mPathLength;
    private double mPathWidth;
    private double mPolygonArea;
    private double mInclineValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSavedState = new Bundle();
        }
        else {
            mSavedState = savedInstanceState;
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        Log.d(TAG,
              "onCreateView");

        View view = super.onCreateView(inflater,
                                       container,
                                       savedInstanceState);

        if (((MainApplication) getActivity().getApplication()).getInput()
                                                              .getCurrentSelectedTaxon() == null) {
            Log.w(TAG,
                  "onCreateView : no taxon selected !");
        }
        else {
            final Area selectedArea = ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                                 .getCurrentSelectedTaxon()).getCurrentSelectedArea();

            if (selectedArea == null) {
                Log.w(TAG,
                      "onCreateView : no feature selected !");
            }
            else {
                switch (selectedArea.getFeature()
                                    .getGeometry()
                                    .getType()) {
                    case POINT:
                        view = inflater.inflate(R.layout.fragment_area_point,
                                                container,
                                                false);
                        mEditTextPointArea = (EditText) view.findViewById(R.id.editTextPointArea);
                        mEditTextPointArea.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(
                                    CharSequence s,
                                    int start,
                                    int before,
                                    int count) {
                                // nothing to do ...
                            }

                            @Override
                            public void beforeTextChanged(
                                    CharSequence s,
                                    int start,
                                    int count,
                                    int after) {
                                // nothing to do ...
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                                if (!s.toString()
                                      .isEmpty()) {
                                    try {
                                        mPointArea = Double.valueOf(s.toString());
                                        computeArea();
                                    }
                                    catch (NumberFormatException nfe) {
                                        Log.w(TAG,
                                              nfe.getMessage());
                                    }
                                }
                            }
                        });

                        break;
                    case LINE_STRING:
                        view = inflater.inflate(R.layout.fragment_area_path,
                                                container,
                                                false);

                        mTextViewPathLengthComputed = (TextView) view.findViewById(R.id.textViewPathLengthComputed);
                        mTextViewPathLengthComputed.setText(String.format(getString(R.string.area_path_length_computed),
                                                                          0.0));

                        mEditTextPathWidth = (EditText) view.findViewById(R.id.editTextPathWidth);
                        mEditTextPathWidth.setEnabled(false);
                        mEditTextPathWidth.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(
                                    CharSequence s,
                                    int start,
                                    int before,
                                    int count) {
                                // nothing to do ...
                            }

                            @Override
                            public void beforeTextChanged(
                                    CharSequence s,
                                    int start,
                                    int count,
                                    int after) {
                                // nothing to do ...
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                                if (!s.toString()
                                      .isEmpty()) {
                                    try {
                                        mPathWidth = Double.valueOf(s.toString());
                                        computeArea();
                                    }
                                    catch (NumberFormatException nfe) {
                                        Log.w(TAG,
                                              nfe.getMessage());
                                    }
                                }
                            }
                        });

                        mSpinnerIncline = (Spinner) view.findViewById(R.id.spinnerIncline);
                        mSpinnerIncline.setOnItemSelectedListener(this);
                        mSpinnerIncline.setEnabled(false);

                        mTextViewAreaComputed = (TextView) view.findViewById(R.id.textViewAreaComputed);
                        mTextViewAreaComputed.setText(String.format(getString(R.string.area_computed),
                                                                    0.0));

                        if (mAdapter != null) {
                            mSpinnerIncline.setAdapter(mAdapter);
                        }

                        // prepare the loader, either re-connect with an existing one, or start a new one
                        getLoaderManager().initLoader(AbstractMainContentProvider.INCLINES,
                                                      mSavedState,
                                                      this);

                        new GetFeatureLengthOrAreaAsyncTask().execute();
                        break;
                    case POLYGON:
                        view = inflater.inflate(R.layout.fragment_area_polygon,
                                                container,
                                                false);
                        mTextViewPolygonPlanimetricAreaComputed = (TextView) view.findViewById(R.id.textViewPolygonPlanimetricAreaComputed);
                        mTextViewPolygonPlanimetricAreaComputed.setText(String.format(getString(R.string.area_polygon_planimetric_area_computed),
                                                                                      0.0));

                        mSpinnerIncline = (Spinner) view.findViewById(R.id.spinnerIncline);
                        mSpinnerIncline.setOnItemSelectedListener(this);
                        mSpinnerIncline.setEnabled(false);

                        mTextViewAreaComputed = (TextView) view.findViewById(R.id.textViewAreaComputed);
                        mTextViewAreaComputed.setText(String.format(getString(R.string.area_computed),
                                                                    0.0));

                        if (mAdapter != null) {
                            mSpinnerIncline.setAdapter(mAdapter);
                        }

                        // prepare the loader, either re-connect with an existing one, or start a new one
                        getLoaderManager().initLoader(AbstractMainContentProvider.INCLINES,
                                                      mSavedState,
                                                      this);

                        new GetFeatureLengthOrAreaAsyncTask().execute();
                        break;
                    default:
                        break;
                }
            }
        }

        return view;
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

        return ((((MainApplication) getActivity().getApplication()).getInput()
                                                                   .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                            .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                       .getComputedArea() > 0.0));
    }

    @Override
    public void refreshView() {

        ((AppCompatActivity) getActivity()).getSupportActionBar()
                                           .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // refreshes title
        if (((MainApplication) getActivity().getApplication()).getInput()
                                                              .getCurrentSelectedTaxon() == null) {
            getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                 getString(R.string.area_none)));
        }
        else {
            final Area selectedArea = ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                                 .getCurrentSelectedTaxon()).getCurrentSelectedArea();

            if (selectedArea == null) {
                Log.w(TAG,
                      "getResourceTitle : no feature selected !");

                getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                     getString(R.string.area_none)));
            }
            else {
                switch (selectedArea.getFeature()
                                    .getGeometry()
                                    .getType()) {
                    case POINT:
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_point)));
                        break;
                    case LINE_STRING:
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_path)));
                        break;
                    case POLYGON:
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_polygon)));
                        break;
                    default:
                        getActivity().setTitle(String.format(getString(getResourceTitle()),
                                                             getString(R.string.area_none)));
                        break;
                }
            }
        }

        // refreshes entirely the view
        getFragmentManager().beginTransaction()
                            .detach(this)
                            .attach(this)
                            .commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
            Bundle args) {

        final String[] projection = {
                MainDatabaseHelper.InclinesColumns._ID,
                MainDatabaseHelper.InclinesColumns.VALUE,
                MainDatabaseHelper.InclinesColumns.NAME
        };

        switch (id) {
            case AbstractMainContentProvider.INCLINES:
                return new CursorLoader(getActivity(),
                                        MainContentProvider.CONTENT_INCLINES_URI,
                                        projection,
                                        null,
                                        null,
                                        null);
            case AbstractMainContentProvider.INCLINE_ID:
                return new CursorLoader(getActivity(),
                                        Uri.withAppendedPath(MainContentProvider.CONTENT_INCLINES_URI,
                                                             Long.toString(args.getLong(KEY_SELECTED_INCLINE))),
                                        projection,
                                        null,
                                        null,
                                        null);
            default:
                throw new IllegalArgumentException("Unknown loader : " + id);
        }
    }

    @Override
    public void onLoadFinished(
            Loader<Cursor> loader,
            Cursor data) {

        switch (loader.getId()) {
            case AbstractMainContentProvider.INCLINES:
                if (mAdapter == null) {
                    initializeAdapter(data);
                }
                else {
                    mAdapter.swapCursor(data);
                }

                break;
            case AbstractMainContentProvider.INCLINE_ID:
                if (data.moveToFirst()) {
                    mInclineValue = data.getDouble(data.getColumnIndex(MainDatabaseHelper.InclinesColumns.VALUE));
                    computeArea();
                }

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()) {
            case AbstractMainContentProvider.INCLINES:
                // data is not available anymore, delete reference
                if (mAdapter != null) {
                    mAdapter.swapCursor(null);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(
            AdapterView<?> parent,
            View view,
            int position,
            long id) {

        Log.d(TAG,
              "onItemSelected " + id);

        mSavedState.putLong(KEY_SELECTED_INCLINE,
                            id);
        getLoaderManager().restartLoader(AbstractMainContentProvider.INCLINE_ID,
                                         mSavedState,
                                         this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // nothing to do ...
    }

    private void initializeAdapter(Cursor cursor) {

        if (mAdapter == null) {
            mAdapter = new SimpleCursorAdapter(getActivity(),
                                               android.R.layout.simple_spinner_item,
                                               cursor,
                                               new String[] {
                                                       MainDatabaseHelper.InclinesColumns.NAME
                                               },
                                               new int[] {
                                                       android.R.id.text1
                                               },
                                               0);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerIncline.setAdapter(mAdapter);
        }
    }

    private void computeArea() {

        if ((((MainApplication) getActivity().getApplication()).getInput()
                                                               .getCurrentSelectedTaxon() != null) && (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                                                  .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            switch (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                               .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                          .getFeature()
                                                                                                          .getGeometry()
                                                                                                          .getType()) {
                case POINT:
                    ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                               .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                          .setComputedArea(mPointArea);
                    ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
                    break;
                case LINE_STRING:
                    ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                               .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                          .setComputedArea((mPathLength * mPathWidth) / Math.cos(Math.PI * mInclineValue / 180));
                    mTextViewAreaComputed.setText(String.format(getString(R.string.area_computed),
                                                                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                           .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                                      .getComputedArea()));

                    ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
                    break;
                case POLYGON:
                    ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                               .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                          .setComputedArea(mPolygonArea / Math.cos(Math.PI * mInclineValue / 180));
                    mTextViewAreaComputed.setText(String.format(getString(R.string.area_computed),
                                                                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                           .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                                      .getComputedArea()));

                    ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
                    break;
                default:
                    break;
            }
        }
    }

    private class GetFeatureLengthOrAreaAsyncTask
            extends AsyncTask<Void, Void, Double> {

        private Area mSelectedArea = null;

        @Override
        protected void onPreExecute() {

            if (((MainApplication) getActivity().getApplication()).getInput()
                                                                  .getCurrentSelectedTaxon() != null) {
                mSelectedArea = ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                           .getCurrentSelectedTaxon()).getCurrentSelectedArea();
            }
        }

        @Override
        protected Double doInBackground(Void... params) {

            if (mSelectedArea == null) {
                return 0.0;
            }
            else {
                switch (mSelectedArea.getFeature()
                                     .getGeometry()
                                     .getType()) {
                    case LINE_STRING:
                        return ((LineString) mSelectedArea.getFeature()
                                                          .getGeometry()).getGeodesicLength();
                    case POLYGON:
                        return ((Polygon) mSelectedArea.getFeature()
                                                       .getGeometry()).getGeodesicArea(false);
                    default:
                        return 0.0;
                }
            }
        }

        @Override
        protected void onPostExecute(Double result) {

            if (mSelectedArea != null) {
                switch (mSelectedArea.getFeature()
                                     .getGeometry()
                                     .getType()) {
                    case LINE_STRING:
                        mPathLength = result;
                        mTextViewPathLengthComputed.setText(String.format(getString(R.string.area_path_length_computed),
                                                                          result));
                        mEditTextPathWidth.setEnabled(true);
                        mSpinnerIncline.setEnabled(true);
                        break;
                    case POLYGON:
                        mPolygonArea = result;
                        mTextViewPolygonPlanimetricAreaComputed.setText(String.format(getString(R.string.area_polygon_planimetric_area_computed),
                                                                                      result));
                        mSpinnerIncline.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
