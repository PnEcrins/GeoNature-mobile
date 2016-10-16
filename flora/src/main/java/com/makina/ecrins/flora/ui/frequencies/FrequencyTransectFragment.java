package com.makina.ecrins.flora.ui.frequencies;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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

import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Frequency;
import com.makina.ecrins.maps.geojson.geometry.LineString;
import com.makina.ecrins.maps.geojson.geometry.Polygon;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Frequency: Transect method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequencyTransectFragment
        extends Fragment
        implements OnClickListener {

    private static final String ARG_AREA = "ARG_AREA";
    private static final String KEY_FREQUENCY = "KEY_FREQUENCY";

    private static final int VIEWS_ENABLED_DELAY = 1000;

    private Area mArea;
    private Frequency mFrequency;

    private TextView mTextViewFrequencyStepAdvice;
    private EditText mEditTextNumberOfTransects;
    private Button mButtonYes;
    private EditText mEditTextYes;
    private Button mButtonNo;
    private EditText mEditTextNo;
    private TextView mTextViewNumberOfSteps;
    private TextView mTextViewComputedFrequency;

    private OnFrequencyListener mOnFrequencyListener;

    private Handler mHandler;

    public FrequencyTransectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link FrequencyTransectFragment}.
     */
    @NonNull
    public static FrequencyTransectFragment newInstance(@NonNull final Area area,
                                                        @NonNull final Frequency frequency) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_AREA,
                           area);
        args.putParcelable(KEY_FREQUENCY,
                           frequency);

        final FrequencyTransectFragment fragment = new FrequencyTransectFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        mArea = getArguments().getParcelable(ARG_AREA);
        mFrequency = (savedInstanceState == null) ? (Frequency) getArguments().getParcelable(KEY_FREQUENCY) : (Frequency) savedInstanceState.getParcelable(KEY_FREQUENCY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frequency_transect,
                                     container,
                                     false);

        mTextViewFrequencyStepAdvice = (TextView) view.findViewById(R.id.textViewFrequencyStepAdvice);
        mTextViewFrequencyStepAdvice.setText(String.format(getString(R.string.frequency_transect_step_advice),
                                                           0));

        mEditTextNumberOfTransects = (EditText) view.findViewById(R.id.editTextNumberOfTransects);
        mEditTextNumberOfTransects.setText(NumberFormat.getInstance()
                                                       .format(mFrequency.getTransects()));
        mEditTextNumberOfTransects.addTextChangedListener(new TextWatcher() {
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
                        mFrequency.setTransects(Integer.valueOf(s.toString()));
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyTransectFragment.class.getName(),
                              nfe.getMessage());
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
                        mFrequency.setTransectYes(Integer.valueOf(s.toString()));
                        updateFrequency(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyTransectFragment.class.getName(),
                              nfe.getMessage());
                    }
                }
            }
        });

        mEditTextNo = (EditText) view.findViewById(R.id.editTextNo);
        mEditTextNo.addTextChangedListener(new TextWatcher() {
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
                        mFrequency.setTransectNo(Integer.valueOf(s.toString()));
                        updateFrequency(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyTransectFragment.class.getName(),
                              nfe.getMessage());
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
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFrequencyListener) {
            mOnFrequencyListener = (OnFrequencyListener) context;
        }
        else {
            throw new RuntimeException(getContext().toString() + " must implement OnFrequencyListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_FREQUENCY,
                               mFrequency);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.buttonYes:
                enableViews(false);

                mFrequency.setTransectYes(mFrequency.getTransectYes() + 1);

                updateFrequency(true);

                mHandler.postDelayed(new ViewsEnableDelay(),
                                     VIEWS_ENABLED_DELAY);

                break;
            case R.id.buttonNo:
                enableViews(false);

                mFrequency.setTransectNo(mFrequency.getTransectNo() + 1);

                updateFrequency(true);

                mHandler.postDelayed(new ViewsEnableDelay(),
                                     VIEWS_ENABLED_DELAY);

                break;
        }
    }

    private void updateFrequency(boolean updateEditText) {
        int numberOfSteps = mFrequency.getTransectYes() + mFrequency.getTransectNo();

        mTextViewNumberOfSteps.setText(String.format(getString(R.string.frequency_transect_number_of_steps),
                                                     numberOfSteps));

        if (numberOfSteps > 0) {
            double computedFrequency = (Integer.valueOf(mFrequency.getTransectYes())
                                               .doubleValue() / Integer.valueOf(numberOfSteps)
                                                                       .doubleValue()) * 100;

            NumberFormat numberFormatPercent = DecimalFormat.getPercentInstance();
            numberFormatPercent.setMaximumFractionDigits(2);

            mTextViewComputedFrequency.setText(String.format(getString(R.string.frequency_transect_frequency),
                                                             numberFormatPercent.format(computedFrequency / 100)));

            mFrequency.setValue(computedFrequency);

            mOnFrequencyListener.OnFrequencyUpdated(mFrequency);
        }
        else {
            mTextViewComputedFrequency.setText(getString(R.string.frequency_transect_frequency_undefined));
        }

        if (updateEditText) {
            mEditTextYes.setText(NumberFormat.getInstance()
                                             .format(mFrequency.getTransectYes()));
            mEditTextNo.setText(NumberFormat.getInstance()
                                            .format(mFrequency.getTransectNo()));
        }
    }

    private void enableViews(boolean enabled) {
        mButtonYes.setEnabled(enabled);
        mButtonNo.setEnabled(enabled);
        mEditTextYes.setEnabled(enabled);
        mEditTextNo.setEnabled(enabled);
    }

    private final class ViewsEnableDelay
            implements Runnable {

        @Override
        public void run() {
            enableViews(true);
        }
    }

    private final class ComputeRecommendedStep
            implements Runnable {

        @Override
        public void run() {
            double computedStep; // meters

            switch (mArea.getFeature()
                         .getGeometry()
                         .getType()) {
                case POINT:
                    computedStep = (Math.PI * Math.sqrt(mArea.getComputedArea() / Math.PI)) / 100;
                    break;
                case LINE_STRING:
                    computedStep = ((LineString) mArea.getFeature()
                                                      .getGeometry()).getGeodesicLength() / 100;
                    break;
                case POLYGON:
                    computedStep = ((Polygon) mArea.getFeature()
                                                   .getGeometry()).getGeodesicLength() / 200;
                    break;
                default:
                    computedStep = 0.6;
                    break;
            }

            mFrequency.setRecommendedStep(computedStep);

            mTextViewFrequencyStepAdvice.setText(String.format(getString(R.string.frequency_transect_step_advice),
                                                               Double.valueOf(computedStep * 100)
                                                                     .intValue()));
        }
    }
}
