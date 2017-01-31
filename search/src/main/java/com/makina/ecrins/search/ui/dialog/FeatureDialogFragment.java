package com.makina.ecrins.search.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.jts.geojson.GeometryUtils;
import com.makina.ecrins.search.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Custom {@code Dialog} used to display some data from a given {@link Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FeatureDialogFragment
        extends DialogFragment {

    private static final String KEY_FEATURE = "feature";
    private static final String KEY_LOCATION = "location";

    public static FeatureDialogFragment newInstance(Feature pFeature,
                                                    GeoPoint pGeoPoint) {

        final FeatureDialogFragment dialogFragment = new FeatureDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(KEY_FEATURE,
                           pFeature);
        args.putParcelable(KEY_LOCATION,
                           pGeoPoint);

        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(true);

        return dialogFragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = View.inflate(getActivity(),
                                       R.layout.dialog_feature,
                                       null);

        final Feature feature = getArguments().getParcelable(KEY_FEATURE);

        final TextView textViewFeatureDate = (TextView) view.findViewById(R.id.textViewFeatureDate);
        final TextView textViewFeatureObserver = (TextView) view.findViewById(R.id.textViewFeatureObserver);
        final TextView textViewFeatureDistance = (TextView) view.findViewById(R.id.textViewFeatureDistance);

        if ((feature != null) && feature.getProperties()
                                        .containsKey(MainDatabaseHelper.SearchColumns.DATE_OBS) &&
                (!TextUtils.isEmpty(feature.getProperties()
                                           .getString(MainDatabaseHelper.SearchColumns.DATE_OBS)))) {
            try {
                textViewFeatureDate.setVisibility(View.VISIBLE);
                textViewFeatureDate.setText(getString(R.string.alert_dialog_feature_date,
                                                      DateFormat.getLongDateFormat(getActivity())
                                                                .format((new SimpleDateFormat("yyyy/MM/dd",
                                                                                              Locale.getDefault())).parse(feature.getProperties()
                                                                                                                                 .getString(MainDatabaseHelper.SearchColumns.DATE_OBS)))));
            }
            catch (ParseException pe) {
                textViewFeatureDate.setVisibility(View.GONE);
                Log.w(getClass().getName(),
                      pe.getMessage(),
                      pe);
            }
        }
        else {
            textViewFeatureDate.setVisibility(View.GONE);
        }

        textViewFeatureObserver.setText(getString(R.string.alert_dialog_feature_observer,
                                                  feature.getProperties()
                                                         .getString(MainDatabaseHelper.SearchColumns.OBSERVER)));
        textViewFeatureDistance.setText(R.string.alert_dialog_feature_distance_undetermined);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),
                                                                R.style.CommonsDialogStyle).setTitle(feature.getProperties()
                                                                                                            .getString(MainDatabaseHelper.SearchColumns.TAXON))
                                                                                           .setView(view)
                                                                                           .setNegativeButton(R.string.alert_dialog_button_close,
                                                                                                              null)
                                                                                           .create();

        alertDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        // this fragment may be not attached to the activity
                        if (isAdded()) {
                            final GeoPoint location = getArguments().getParcelable(KEY_LOCATION);
                            final Feature feature = getArguments().getParcelable(KEY_FEATURE);

                            if ((location != null) && (feature != null)) {
                                textViewFeatureDistance.setText(getString(R.string.alert_dialog_feature_distance,
                                                                          GeometryUtils.distanceTo(location.getPoint(),
                                                                                                   feature.getGeometry())));
                            }
                        }
                    }
                });
            }
        });

        return alertDialog;
    }
}
