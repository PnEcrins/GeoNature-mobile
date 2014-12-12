package com.makina.ecrins.flora.ui.frequencies;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.maps.geojson.geometry.LineString;
import com.makina.ecrins.maps.geojson.geometry.Polygon;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Frequency: Transect method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequencyTransectFragment extends Fragment implements OnClickListener {

    protected static final String KEY_NUMBER_OF_TRANSECTS = "number_of_transects";
    protected static final String KEY_NUMBER_OF_YES = "number_of_yes";
    protected static final String KEY_NUMBER_OF_NO = "number_of_no";

    private static final int VIEWS_ENABLED_DELAY = 1000;

    protected Bundle mSavedState;

    private TextView mTextViewFrequencyStepAdvice;
    private EditText mEditTextNumberOfTransects;
    protected Button mButtonYes;
    protected EditText mEditTextYes;
    protected Button mButtonNo;
    protected EditText mEditTextNo;
    private TextView mTextViewNumberOfSteps;
    private TextView mTextViewComputedFrequency;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        if (savedInstanceState == null) {
            Log.d(FrequencyTransectFragment.class.getName(), "onCreate, savedInstanceState null");

            mSavedState = new Bundle();

            mSavedState.putInt(KEY_NUMBER_OF_TRANSECTS, 1);
        }
        else {
            Log.d(FrequencyTransectFragment.class.getName(),
                    "onCreate, savedInstanceState initialized");

            mSavedState = savedInstanceState;
        }

        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            mSavedState.putInt(KEY_NUMBER_OF_TRANSECTS, ((Taxon) ((MainApplication) getActivity()
                    .getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getFrequency()
                    .getTransects());
            mSavedState.putInt(KEY_NUMBER_OF_YES, ((Taxon) ((MainApplication) getActivity()
                    .getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getFrequency()
                    .getTransectYes());
            mSavedState.putInt(KEY_NUMBER_OF_NO, ((Taxon) ((MainApplication) getActivity()
                    .getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getFrequency()
                    .getTransectNo());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frequency_transect, container, false);

        mTextViewFrequencyStepAdvice = (TextView) view.findViewById(R.id.textViewFrequencyStepAdvice);
        mTextViewFrequencyStepAdvice.setText(
                String.format(
                        getString(R.string.frequency_transect_step_advice),
                        0
                )
        );

        mEditTextNumberOfTransects = (EditText) view.findViewById(R.id.editTextNumberOfTransects);
        mEditTextNumberOfTransects.setText(
                Integer.toString(mSavedState.getInt(KEY_NUMBER_OF_TRANSECTS, 1)));
        mEditTextNumberOfTransects.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do ...
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do ...
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        mSavedState.putInt(KEY_NUMBER_OF_TRANSECTS, Integer.valueOf(s.toString()));
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyTransectFragment.class.getName(), nfe.getMessage());
                    }
                }
            }
        });

        mButtonYes = (Button) view.findViewById(R.id.buttonYes);
        mButtonNo = (Button) view.findViewById(R.id.buttonNo);
        mButtonYes.setOnClickListener(this);
        mButtonNo.setOnClickListener(this);

        mEditTextYes = (EditText) view.findViewById(R.id.editTextYes);
        mEditTextYes.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do ...
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do ...
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        mSavedState.putInt(KEY_NUMBER_OF_YES, Integer.valueOf(s.toString()));
                        updateFrequency(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyTransectFragment.class.getName(), nfe.getMessage());
                    }
                }
            }
        });

        mEditTextNo = (EditText) view.findViewById(R.id.editTextNo);
        mEditTextNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do ...
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do ...
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        mSavedState.putInt(KEY_NUMBER_OF_NO, Integer.valueOf(s.toString()));
                        updateFrequency(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyTransectFragment.class.getName(), nfe.getMessage());
                    }
                }
            }
        });

        mTextViewNumberOfSteps = (TextView) view.findViewById(R.id.textViewNumberOfSteps);
        mTextViewComputedFrequency = (TextView) view.findViewById(R.id.textViewComputedFrequency);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.post(new ViewsEnableDelay());
        mHandler.post(new ComputeRecommendedStep());

        updateFrequency(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(FrequencyTransectFragment.class.getName(), "onSaveInstanceState");

        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.buttonYes:
                enableViews(false);
                mSavedState.putInt(KEY_NUMBER_OF_YES, mSavedState.getInt(KEY_NUMBER_OF_YES, 0) + 1);
                updateFrequency(true);
                mHandler.postDelayed(new ViewsEnableDelay(), VIEWS_ENABLED_DELAY);

                break;
            case R.id.buttonNo:
                enableViews(false);
                mSavedState.putInt(KEY_NUMBER_OF_NO, mSavedState.getInt(KEY_NUMBER_OF_NO, 0) + 1);
                updateFrequency(true);
                mHandler.postDelayed(new ViewsEnableDelay(), VIEWS_ENABLED_DELAY);

                break;
        }
    }

    private void updateFrequency(boolean updateEditText) {
        int numberOfSteps = mSavedState.getInt(KEY_NUMBER_OF_YES, 0) +
                mSavedState.getInt(KEY_NUMBER_OF_NO, 0);
        mTextViewNumberOfSteps.setText(
                String.format(
                        getString(R.string.frequency_transect_number_of_steps),
                        numberOfSteps)
        );

        if (numberOfSteps > 0) {
            double computedFrequency = (Integer.valueOf(mSavedState.getInt(KEY_NUMBER_OF_YES, 0))
                    .doubleValue() / Integer.valueOf(numberOfSteps)
                    .doubleValue()) * 100;

            Log.d(FrequencyTransectFragment.class.getName(),
                    "updateFrequency " + computedFrequency);

            NumberFormat numberFormatPercent = DecimalFormat.getPercentInstance();
            numberFormatPercent.setMaximumFractionDigits(2);

            mTextViewComputedFrequency.setText(
                    String.format(
                            getString(R.string.frequency_transect_frequency),
                            numberFormatPercent.format(computedFrequency / 100))
            );

            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getFrequency()
                        .setTransects(mSavedState.getInt(KEY_NUMBER_OF_TRANSECTS, 1));
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getFrequency()
                        .setTransectYes(mSavedState.getInt(KEY_NUMBER_OF_YES, 0));
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getFrequency()
                        .setTransectNo(mSavedState.getInt(KEY_NUMBER_OF_NO, 0));
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getFrequency()
                        .setValue(computedFrequency);
            }
        }
        else {
            mTextViewComputedFrequency.setText(getString(R.string.frequency_transect_frequency_undefined));
        }

        if (updateEditText) {
            mEditTextYes.setText(Integer.toString(mSavedState.getInt(KEY_NUMBER_OF_YES, 0)));
            mEditTextNo.setText(Integer.toString(mSavedState.getInt(KEY_NUMBER_OF_NO, 0)));
        }
    }

    private void enableViews(boolean enabled) {
        mButtonYes.setEnabled(enabled);
        mButtonNo.setEnabled(enabled);
        mEditTextYes.setEnabled(enabled);
        mEditTextNo.setEnabled(enabled);
    }

    private final class ViewsEnableDelay implements Runnable {

        @Override
        public void run() {
            enableViews(true);
        }
    }

    private final class ComputeRecommendedStep implements Runnable {

        @Override
        public void run() {
            if (((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) {
                Area area = ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea();

                if (area == null) {
                    Log.w(FrequencyTransectFragment.class.getName(), "no area selected !");
                }
                else {
                    double computedStep; //meters

                    switch (area.getFeature().getGeometry().getType()) {
                        case POINT:
                            computedStep = (Math.PI * Math
                                    .sqrt(area.getComputedArea() / Math.PI)) / 100;
                            break;
                        case LINE_STRING:
                            computedStep = ((LineString) area.getFeature()
                                    .getGeometry()).getGeodesicLength() / 100;
                            break;
                        case POLYGON:
                            computedStep = ((Polygon) area.getFeature()
                                    .getGeometry()).getGeodesicLength() / 200;
                            break;
                        default:
                            computedStep = 0.6;
                            break;
                    }

                    ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                            .getFrequency()
                            .setRecommendedStep(computedStep);

                    mTextViewFrequencyStepAdvice.setText(
                            String.format(
                                    getString(R.string.frequency_transect_step_advice),
                                    Double.valueOf(computedStep * 100).intValue()
                            )
                    );
                }
            }
        }
    }
}
