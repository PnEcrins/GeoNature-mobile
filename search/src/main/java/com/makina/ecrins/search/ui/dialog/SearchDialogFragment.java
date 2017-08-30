package com.makina.ecrins.search.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.R;

/**
 * Custom {@code Dialog} used to perform a query search on the map for a given radius (meters).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SearchDialogFragment
        extends DialogFragment {

    private static final String KEY_MIN_RADIUS = "min_radius";
    private static final String KEY_MAX_RADIUS = "max_radius";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_LOCATION = "location";

    private OnSearchDialogValidateListener mOnSearchDialogValidateListener;

    public static SearchDialogFragment newInstance(int maxRadius,
                                                   int radius,
                                                   GeoPoint location) {

        if (BuildConfig.DEBUG) {
            Log.d(SearchDialogFragment.class.getName(),
                  "newInstance");
        }

        final SearchDialogFragment dialogFragment = new SearchDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_MIN_RADIUS,
                    0);
        args.putInt(KEY_MAX_RADIUS,
                    maxRadius);
        args.putInt(KEY_RADIUS,
                    radius);
        args.putParcelable(KEY_LOCATION,
                           location);

        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    public void setOnSearchDialogValidateListener(OnSearchDialogValidateListener pOnSearchDialogValidateListener) {

        this.mOnSearchDialogValidateListener = pOnSearchDialogValidateListener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = View.inflate(getActivity(),
                                       R.layout.dialog_search,
                                       null);

        final TextView textViewSearchRadius = (TextView) view.findViewById(R.id.textViewSearchRadius);
        final TextView textViewSeekBarMinValue = (TextView) view.findViewById(R.id.textViewSeekBarMinValue);
        final TextView textViewSeekBarMaxValue = (TextView) view.findViewById(R.id.textViewSeekBarMaxValue);
        final SeekBar seekBarSearchRadius = (SeekBar) view.findViewById(R.id.seekBarSearchRadius);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),
                                                                R.style.CommonsDialogStyle).setTitle(R.string.alert_dialog_search_title)
                                                                                           .setView(view)
                                                                                           .setPositiveButton(R.string.alert_dialog_ok,
                                                                                                              new OnClickListener() {
                                                                                                                  @Override
                                                                                                                  public void onClick(DialogInterface dialog,
                                                                                                                                      int which) {

                                                                                                                      mOnSearchDialogValidateListener.onSearchCriteria(dialog,
                                                                                                                                                                       seekBarSearchRadius.getProgress(),
                                                                                                                                                                       (GeoPoint) getArguments().getParcelable(KEY_LOCATION));
                                                                                                                  }
                                                                                                              })
                                                                                           .setNegativeButton(R.string.alert_dialog_cancel,
                                                                                                              null)
                                                                                           .create();

        textViewSearchRadius.setText(String.format(getString(R.string.alert_dialog_search_radius),
                                                   getArguments().getInt(KEY_MIN_RADIUS)));
        textViewSeekBarMinValue.setText(String.format(getString(R.string.alert_dialog_search_radius_meters),
                                                      getArguments().getInt(KEY_MIN_RADIUS)));
        textViewSeekBarMaxValue.setText(String.format(getString(R.string.alert_dialog_search_radius_meters),
                                                      getArguments().getInt(KEY_MAX_RADIUS)));

        seekBarSearchRadius.setMax(getArguments().getInt(KEY_MAX_RADIUS));
        seekBarSearchRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // nothing to do ...
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // nothing to do ...
            }

            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {

                textViewSearchRadius.setText(String.format(getString(R.string.alert_dialog_search_radius),
                                                           progress));

                if (alertDialog.getButton(Dialog.BUTTON_POSITIVE) != null) {
                    alertDialog.getButton(Dialog.BUTTON_POSITIVE)
                               .setEnabled(progress > 0);
                }
            }
        });

        seekBarSearchRadius.setProgress(getArguments().getInt(KEY_RADIUS));

        alertDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                alertDialog.getButton(Dialog.BUTTON_POSITIVE)
                           .setEnabled(getArguments().getInt(KEY_RADIUS) > 0);
            }
        });

        return alertDialog;
    }

    /**
     * The callback used to apply search criteria.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public interface OnSearchDialogValidateListener {

        /**
         * Invoked when the positive button of the dialog is pressed.
         *
         * @param dialog   the dialog that received the click
         * @param radius   the selected radius (meters)
         * @param location the current location
         */
        void onSearchCriteria(DialogInterface dialog,
                              int radius,
                              GeoPoint location);
    }
}
