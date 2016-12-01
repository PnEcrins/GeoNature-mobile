package com.makina.ecrins.flora.ui.input.area;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.GeometryUtils;
import com.vividsolutions.jts.geom.Polygon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

/**
 * Default {@code Adapter} of {@link Area}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class AreaAdapter
        extends RecyclerView.Adapter<AreaAdapter.AbstractViewHolder> {

    private static final String TAG = AreaAdapter.class.getName();

    private static final String KEY_SELECTED_INCLINE = "KEY_SELECTED_INCLINE";
    private static final int TYPE_UNDEFINED = 0;
    private static final int TYPE_POINT = 1;
    private static final int TYPE_LINE_STRING = 2;
    private static final int TYPE_POLYGON = 3;

    private final Context mContext;
    private LoaderManager mLoaderManager;
    private final OnAreaAdapterListener mOnAreaAdapterListener;
    private final DecimalFormat mDecimalFormat;

    private Area mArea;

    public AreaAdapter(@NonNull final Context context,
                       @NonNull final LoaderManager loaderManager,
                       @NonNull final OnAreaAdapterListener onAreaAdapterListener) {
        this.mContext = context;
        this.mLoaderManager = loaderManager;
        this.mOnAreaAdapterListener = onAreaAdapterListener;

        mDecimalFormat = new DecimalFormat();
        mDecimalFormat.setMaximumFractionDigits(2);
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        mDecimalFormat.setDecimalFormatSymbols(symbols);
    }

    @Override
    public AbstractViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        switch (viewType) {
            case TYPE_POINT:
                return new PointViewHolder(LayoutInflater.from(parent.getContext())
                                                         .inflate(R.layout.view_area_point,
                                                                  parent,
                                                                  false));
            case TYPE_LINE_STRING:
                return new LineStringViewHolder(LayoutInflater.from(parent.getContext())
                                                              .inflate(R.layout.view_area_path,
                                                                       parent,
                                                                       false));
            case TYPE_POLYGON:
                return new PolygonViewHolder(LayoutInflater.from(parent.getContext())
                                                           .inflate(R.layout.view_area_polygon,
                                                                    parent,
                                                                    false));
            default:
                return new AbstractViewHolder(LayoutInflater.from(parent.getContext())
                                                            .inflate(R.layout.view_area_none,
                                                                     parent,
                                                                     false)) {
                    @Override
                    public void bind(@NonNull Area area,
                                     int position) {

                    }

                    @Override
                    public void computeArea() {

                    }
                };
        }
    }

    @Override
    public void onBindViewHolder(AbstractViewHolder holder,
                                 int position) {
        holder.bind(mArea,
                    position);
    }

    @Override
    public int getItemCount() {
        return mArea == null ? 0 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mArea.getFeature() == null) {
            return TYPE_UNDEFINED;
        }

        switch (mArea.getFeature()
                     .getGeometry()
                     .getGeometryType()) {
            case "Point":
                return TYPE_POINT;
            case "LineString":
                return TYPE_LINE_STRING;
            case "Polygon":
                return TYPE_POLYGON;
            default:
                return TYPE_UNDEFINED;
        }
    }

    public void setArea(@NonNull final Area area) {
        this.mArea = area;

        notifyDataSetChanged();
    }

    /**
     * Default {@code ViewHolder} used by {@link AreaAdapter} for {@link Area} as "Point".
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    private class PointViewHolder
            extends AbstractViewHolder {

        private final EditText mEditTextPointArea;

        private double mPointArea;

        PointViewHolder(View itemView) {
            super(itemView);

            mEditTextPointArea = (EditText) itemView.findViewById(R.id.editTextPointArea);
            mEditTextPointArea.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s,
                                          int start,
                                          int before,
                                          int count) {
                    // nothing to do ...
                }

                @Override
                public void beforeTextChanged(CharSequence s,
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
                            mPointArea = mDecimalFormat.parse(s.toString())
                                                       .doubleValue();
                            computeArea();
                        }
                        catch (ParseException pe) {
                            Log.w(TAG,
                                  pe.getMessage());
                        }
                    }
                }
            });
        }

        @Override
        public void bind(@NonNull Area area,
                         int position) {
            mPointArea = area.getComputedArea();
            mEditTextPointArea.setText(mPointArea == 0 ? null : mDecimalFormat.format(mPointArea));
        }

        @Override
        public void computeArea() {
            mOnAreaAdapterListener.onAreaComputed(mInclineValue,
                                                  mPointArea,
                                                  mPointArea);
        }
    }

    /**
     * Default {@code ViewHolder} used by {@link AreaAdapter} for {@link Area} as "LineString".
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    private class LineStringViewHolder
            extends AbstractViewHolder {

        private final TextView mTextViewPathLengthComputed;
        private final EditText mEditTextPathWidth;
        private final Spinner mSpinnerIncline;
        private final TextView mTextViewAreaComputed;

        private double mPathWidth;
        private double mPathLength;

        LineStringViewHolder(View itemView) {
            super(itemView);

            mTextViewPathLengthComputed = (TextView) itemView.findViewById(R.id.textViewPathLengthComputed);
            mTextViewPathLengthComputed.setText(String.format(mContext.getString(R.string.area_path_length_computed),
                                                              0.0));

            mEditTextPathWidth = (EditText) itemView.findViewById(R.id.editTextPathWidth);
            mEditTextPathWidth.setEnabled(false);
            mEditTextPathWidth.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s,
                                          int start,
                                          int before,
                                          int count) {
                    // nothing to do ...
                }

                @Override
                public void beforeTextChanged(CharSequence s,
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
                            mPathWidth = mDecimalFormat.parse(s.toString())
                                                       .doubleValue();
                            computeArea();
                        }
                        catch (ParseException pe) {
                            Log.w(TAG,
                                  pe.getMessage());
                        }
                    }
                }
            });

            mSpinnerIncline = (Spinner) itemView.findViewById(R.id.spinnerIncline);
            mSpinnerIncline.setOnItemSelectedListener(mOnItemSelectedListener);
            mSpinnerIncline.setEnabled(false);

            mTextViewAreaComputed = (TextView) itemView.findViewById(R.id.textViewAreaComputed);
            mTextViewAreaComputed.setText(String.format(mContext.getString(R.string.area_computed),
                                                        0.0));

            // prepare the loader, either re-connect with an existing one, or start a new one
            mLoaderManager.initLoader(AbstractMainContentProvider.INCLINES,
                                      new Bundle(),
                                      mLoaderCallbacks);
        }

        @Override
        public void bind(@NonNull Area area,
                         int position) {
            final Feature feature = area.getFeature();

            if (feature != null) {
                mPathLength = GeometryUtils.getGeodesicLength(feature.getGeometry());

                if (mPathLength != 0) {
                    mPathWidth = area.getArea() / mPathLength;
                }
            }

            mTextViewPathLengthComputed.setText(String.format(mContext.getString(R.string.area_path_length_computed),
                                                              mPathLength));

            if (mSpinnerIncline.getAdapter() != null) {
                for (int i = 0; i < mSpinnerIncline.getAdapter()
                                                   .getCount(); i++) {
                    final Cursor cursorAtPosition = (Cursor) mSpinnerIncline.getAdapter()
                                                                            .getItem(i);

                    if (area.getInclineValue() == cursorAtPosition.getDouble(cursorAtPosition.getColumnIndex(MainDatabaseHelper.InclinesColumns.VALUE))) {
                        mSpinnerIncline.setSelection(i);
                        break;
                    }
                }
            }

            mSpinnerIncline.setEnabled(true);

            mEditTextPathWidth.setEnabled(true);
            mEditTextPathWidth.setText(mPathWidth == 0 ? null : mDecimalFormat.format(mPathWidth));
        }

        @Override
        public void computeArea() {
            final double area = mPathLength * mPathWidth;
            final double computedArea = area / Math.cos(Math.PI * mInclineValue / 180);
            mTextViewAreaComputed.setText(String.format(mContext.getString(R.string.area_computed),
                                                        computedArea));

            mOnAreaAdapterListener.onAreaComputed(mInclineValue,
                                                  area,
                                                  computedArea);
        }

        @Override
        void initializeAdapter(Cursor cursor) {
            super.initializeAdapter(cursor);

            if (mAdapter != null) {
                mSpinnerIncline.setAdapter(mAdapter);
            }
        }
    }

    /**
     * Default {@code ViewHolder} used by {@link AreaAdapter} for {@link Area} as "Polygon".
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    private class PolygonViewHolder
            extends AbstractViewHolder {

        private final TextView mTextViewPolygonPlanimetricAreaComputed;
        private final Spinner mSpinnerIncline;
        private final TextView mTextViewAreaComputed;

        private double mPolygonArea;

        PolygonViewHolder(View itemView) {
            super(itemView);

            mTextViewPolygonPlanimetricAreaComputed = (TextView) itemView.findViewById(R.id.textViewPolygonPlanimetricAreaComputed);
            mTextViewPolygonPlanimetricAreaComputed.setText(String.format(mContext.getString(R.string.area_polygon_planimetric_area_computed),
                                                                          0.0));

            mSpinnerIncline = (Spinner) itemView.findViewById(R.id.spinnerIncline);
            mSpinnerIncline.setOnItemSelectedListener(mOnItemSelectedListener);
            mSpinnerIncline.setEnabled(false);

            mTextViewAreaComputed = (TextView) itemView.findViewById(R.id.textViewAreaComputed);
            mTextViewAreaComputed.setText(String.format(mContext.getString(R.string.area_computed),
                                                        0.0));

            // prepare the loader, either re-connect with an existing one, or start a new one
            mLoaderManager.initLoader(AbstractMainContentProvider.INCLINES,
                                      new Bundle(),
                                      mLoaderCallbacks);
        }

        @Override
        public void bind(@NonNull Area area,
                         int position) {
            final Feature feature = area.getFeature();

            if (feature != null) {
                mPolygonArea = GeometryUtils.getGeodesicArea((Polygon) feature.getGeometry(),
                                                             false);
            }

            mTextViewPolygonPlanimetricAreaComputed.setText(String.format(mContext.getString(R.string.area_polygon_planimetric_area_computed),
                                                                          mPolygonArea));

            if (mSpinnerIncline.getAdapter() != null) {
                for (int i = 0; i < mSpinnerIncline.getAdapter()
                                                   .getCount(); i++) {
                    final Cursor cursorAtPosition = (Cursor) mSpinnerIncline.getAdapter()
                                                                            .getItem(i);

                    if (area.getInclineValue() == cursorAtPosition.getDouble(cursorAtPosition.getColumnIndex(MainDatabaseHelper.InclinesColumns.VALUE))) {
                        mSpinnerIncline.setSelection(i);
                        break;
                    }
                }
            }

            mSpinnerIncline.setEnabled(true);
        }

        @Override
        public void computeArea() {
            final double computedArea = mPolygonArea / Math.cos(Math.PI * mInclineValue / 180);
            mTextViewAreaComputed.setText(String.format(mContext.getString(R.string.area_computed),
                                                        computedArea));

            mOnAreaAdapterListener.onAreaComputed(mInclineValue,
                                                  mPolygonArea,
                                                  computedArea);
        }

        @Override
        void initializeAdapter(Cursor cursor) {
            super.initializeAdapter(cursor);

            if (mAdapter != null) {
                mSpinnerIncline.setAdapter(mAdapter);
            }
        }
    }

    abstract class AbstractViewHolder
            extends RecyclerView.ViewHolder {

        SimpleCursorAdapter mAdapter;
        double mInclineValue;

        LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id,
                                                 Bundle args) {
                final String[] projection = {
                        MainDatabaseHelper.InclinesColumns._ID,
                        MainDatabaseHelper.InclinesColumns.VALUE,
                        MainDatabaseHelper.InclinesColumns.NAME
                };

                switch (id) {
                    case AbstractMainContentProvider.INCLINES:
                        return new CursorLoader(mContext,
                                                MainContentProvider.CONTENT_INCLINES_URI,
                                                projection,
                                                null,
                                                null,
                                                null);
                    case AbstractMainContentProvider.INCLINE_ID:
                        return new CursorLoader(mContext,
                                                Uri.withAppendedPath(MainContentProvider.CONTENT_INCLINES_URI,
                                                                     Long.toString(args.getLong(KEY_SELECTED_INCLINE))),
                                                projection,
                                                null,
                                                null,
                                                null);
                    default:
                        throw new IllegalArgumentException("Unknown loader: " + id);
                }
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader,
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

            }
        };

        AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view,
                                       int position,
                                       long id) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "onItemSelected " + id);
                }

                final Bundle bundle = new Bundle();
                bundle.putLong(KEY_SELECTED_INCLINE,
                               id);
                mLoaderManager.restartLoader(AbstractMainContentProvider.INCLINE_ID,
                                             bundle,
                                             mLoaderCallbacks);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do ...
            }
        };

        AbstractViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(@NonNull final Area area,
                                  int position);

        public abstract void computeArea();

        void initializeAdapter(Cursor cursor) {
            if (mAdapter == null) {
                mAdapter = new SimpleCursorAdapter(mContext,
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
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when an {@link Area} has been computed.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnAreaAdapterListener {
        void onAreaComputed(double incline,
                            double area,
                            double computedArea);
    }
}
