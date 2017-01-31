package com.makina.ecrins.search.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.jts.geojson.GeometryUtils;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonReader;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom loader to find a list of filtered {@link Feature} from given criteria.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LoadFilteredFeaturesLoader
        extends AsyncTaskLoader<List<Feature>> {

    private final ForceLoadContentObserver mObserver;

    private final String[] mProjection = {
            MainDatabaseHelper.SearchColumns._ID,
            MainDatabaseHelper.SearchColumns.TAXON,
            MainDatabaseHelper.SearchColumns.DATE_OBS,
            MainDatabaseHelper.SearchColumns.OBSERVER,
            MainDatabaseHelper.SearchColumns.GEOMETRY
    };

    private final List<Feature> mFeatures = new ArrayList<>();

    private Cursor mCursor;
    private Uri mUri;
    private GeoPoint mLocation;
    private double mRadius;
    private String mGroupBy;

    public LoadFilteredFeaturesLoader(Context pContext,
                                      Uri pUri,
                                      GeoPoint pGeoPoint,
                                      double pRadius,
                                      String pGroupBy) {
        super(pContext);

        mObserver = new ForceLoadContentObserver();

        mUri = pUri;
        mLocation = pGeoPoint;
        mRadius = pRadius;
        mGroupBy = pGroupBy;
    }

    @Override
    public List<Feature> loadInBackground() {
        Cursor cursor = getContext().getContentResolver()
                                    .query(mUri,
                                           mProjection,
                                           null,
                                           null,
                                           null);

        if ((mCursor != null) && (mCursor != cursor) && !mCursor.isClosed()) {
            mCursor.close();
        }

        mCursor = cursor;

        if (mCursor != null) {
            // ensure the cursor window is filled
            mCursor.getCount();
            mCursor.registerContentObserver(mObserver);

            if (mCursor.moveToFirst()) {
                if ((mGroupBy != null) && (!mGroupBy.isEmpty())) {
                    mFeatures.clear();
                    final Map<String, Feature> features = new HashMap<>();

                    do {
                        String groupByValue = mCursor.getString(mCursor.getColumnIndex(mGroupBy));

                        if (!features.containsKey(groupByValue)) {
                            final String id = Long.toString(mCursor.getLong(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns._ID)));
                            final Geometry geometry = new GeoJsonReader().readGeometry(mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.GEOMETRY)));

                            if (!TextUtils.isEmpty(id) && geometry != null) {
                                final Feature feature = new Feature(id,
                                                                    geometry);

                                if ((GeometryUtils.distanceTo(mLocation.getPoint(),
                                                              feature.getGeometry()) <= mRadius)) {
                                    feature.getProperties()
                                           .putString(MainDatabaseHelper.SearchColumns.TAXON,
                                                      mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.TAXON)));
                                    feature.getProperties()
                                           .putString(MainDatabaseHelper.SearchColumns.DATE_OBS,
                                                      mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.DATE_OBS)));
                                    feature.getProperties()
                                           .putString(MainDatabaseHelper.SearchColumns.OBSERVER,
                                                      mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.OBSERVER)));

                                    features.put(groupByValue,
                                                 feature);
                                }
                            }
                        }
                    }
                    while (mCursor.moveToNext());

                    mFeatures.addAll(features.values());
                }
                else {
                    mFeatures.clear();

                    do {
                        final String id = Long.toString(mCursor.getLong(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns._ID)));
                        final Geometry geometry = new GeoJsonReader().readGeometry(mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.GEOMETRY)));

                        if (!TextUtils.isEmpty(id) && geometry != null) {
                            final Feature feature = new Feature(id,
                                                                geometry);

                            if ((GeometryUtils.distanceTo(mLocation.getPoint(),
                                                          feature.getGeometry()) <= mRadius)) {
                                feature.getProperties()
                                       .putString(MainDatabaseHelper.SearchColumns.TAXON,
                                                  mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.TAXON)));
                                feature.getProperties()
                                       .putString(MainDatabaseHelper.SearchColumns.DATE_OBS,
                                                  mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.DATE_OBS)));
                                feature.getProperties()
                                       .putString(MainDatabaseHelper.SearchColumns.OBSERVER,
                                                  mCursor.getString(mCursor.getColumnIndex(MainDatabaseHelper.SearchColumns.OBSERVER)));

                                mFeatures.add(feature);
                            }
                        }
                    }
                    while (mCursor.moveToNext());
                }
            }
        }

        return mFeatures;
    }

    @Override
    public void onCanceled(List<Feature> data) {
        if ((mCursor != null) && !mCursor.isClosed()) {
            mCursor.close();
        }

        mFeatures.clear();
    }

    @Override
    public void deliverResult(List<Feature> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (mCursor != null) {
                mCursor.close();
            }

            mFeatures.clear();

            return;
        }

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // ensure the loader is stopped
        onStopLoading();

        if ((mCursor != null) && !mCursor.isClosed()) {
            mCursor.close();
        }

        mCursor = null;
        mFeatures.clear();
    }

    @Override
    protected void onStartLoading() {
        if ((mCursor != null) && (!mFeatures.isEmpty())) {
            deliverResult(mFeatures);
        }

        if (takeContentChanged() || (mCursor == null) || (mFeatures.isEmpty())) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // attempt to cancel the current load task if possible.
        cancelLoad();
    }
}
