package com.makina.ecrins.flora.ui.frequencies;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Frequency;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Frequency: Estimation method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequencyEstimationFragment
        extends Fragment {

    private static final String TAG = FrequencyEstimationFragment.class.getName();

    private static final String KEY_FREQUENCY = "KEY_FREQUENCY";

    private SeekBar mSeekBarFrequency;
    private EditText mEditTextFrequency;

    private Frequency mFrequency;

    private OnFrequencyListener mOnFrequencyListener;

    public FrequencyEstimationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link FrequencyEstimationFragment}.
     */
    @NonNull
    public static FrequencyEstimationFragment newInstance(@NonNull final Frequency frequency) {
        final Bundle args = new Bundle();
        args.putParcelable(KEY_FREQUENCY,
                           frequency);

        final FrequencyEstimationFragment fragment = new FrequencyEstimationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFrequency = (savedInstanceState == null) ? (Frequency) getArguments().getParcelable(KEY_FREQUENCY) : (Frequency) savedInstanceState.getParcelable(KEY_FREQUENCY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_frequency_estimation,
                                           container,
                                           false);

        final List<Abacus> abacus = new ArrayList<>();
        abacus.add(new Abacus(R.drawable.ic_abacus_01,
                              0.01));
        abacus.add(new Abacus(R.drawable.ic_abacus_02,
                              0.02));
        abacus.add(new Abacus(R.drawable.ic_abacus_05,
                              0.05));
        abacus.add(new Abacus(R.drawable.ic_abacus_08,
                              0.08));
        abacus.add(new Abacus(R.drawable.ic_abacus_15,
                              0.15));
        abacus.add(new Abacus(R.drawable.ic_abacus_25,
                              0.25));
        abacus.add(new Abacus(R.drawable.ic_abacus_50,
                              0.5));
        abacus.add(new Abacus(R.drawable.ic_abacus_70,
                              0.7));
        abacus.add(new Abacus(R.drawable.ic_abacus_90,
                              0.9));

        final GridView gridViewAbacus = (GridView) view.findViewById(R.id.gridViewAbacus);
        final AbacusAdapter abacusAdapter = new AbacusAdapter(getActivity(),
                                                              abacus);
        gridViewAbacus.setAdapter(abacusAdapter);
        gridViewAbacus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final Abacus selectedAbacus = abacusAdapter.getItem(position);

                if (selectedAbacus == null) {
                    Log.w(TAG,
                          "onItemClick: no abacus found");

                    return;
                }

                updateFrequency(selectedAbacus.getValue() * 100,
                                true);
            }
        });

        mSeekBarFrequency = (SeekBar) view.findViewById(R.id.seekBarFrequency);
        mSeekBarFrequency.setMax(100);
        mSeekBarFrequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
                if (fromUser) {
                    updateFrequency(progress,
                                    true);
                }
            }
        });

        mEditTextFrequency = (EditText) view.findViewById(R.id.editTextFrequency);
        mEditTextFrequency.addTextChangedListener(new TextWatcher() {
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
                        updateFrequency(Integer.valueOf(s.toString()),
                                        false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyEstimationFragment.class.getName(),
                              nfe.getMessage());
                    }
                }
            }
        });

        ((TextView) view.findViewById(R.id.textViewSeekBarMaxValue)).setText(NumberFormat.getInstance()
                                                                                         .format(mSeekBarFrequency.getMax()));

        updateFrequency(mFrequency.getValue(),
                        true);

        return view;
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

    private void updateFrequency(double progress,
                                 boolean updateEditText) {
        double progressValue = progress;

        if (progressValue > 100.0) {
            progressValue = 100.0;
        }

        if (progressValue < 0.0) {
            progressValue = 0.0;
        }

        mSeekBarFrequency.setProgress(Double.valueOf(progressValue)
                                            .intValue());

        if ((updateEditText) || (progressValue != progress)) {
            mEditTextFrequency.setText(NumberFormat.getInstance()
                                                   .format(Double.valueOf(progressValue)
                                                                 .intValue()));
        }

        mFrequency.setValue(progressValue);

        mOnFrequencyListener.OnFrequencyUpdated(mFrequency);
    }

    private class AbacusAdapter
            extends ArrayAdapter<Abacus> {

        private final LayoutInflater mInflater;
        private final NumberFormat mNumberFormat;

        AbacusAdapter(Context context,
                      List<Abacus> abacus) {
            super(context,
                  android.R.layout.simple_list_item_1,
                  android.R.id.text1,
                  abacus);

            mNumberFormat = NumberFormat.getPercentInstance();
            mNumberFormat.setMaximumFractionDigits(0);

            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position,
                            View convertView,
                            @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1,
                                         parent,
                                         false);
            }
            else {
                view = convertView;
            }

            final Abacus abacus = getItem(position);

            if (abacus != null) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(mNumberFormat.format(abacus.getValue()));
                textView.setCompoundDrawablesWithIntrinsicBounds(0,
                                                                 abacus.getResourceId(),
                                                                 0,
                                                                 0);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
            }

            return view;
        }
    }

    private class Abacus {

        private final int mResourceId;
        private final double mValue;

        Abacus(int pResourceId,
               double pValue) {
            mResourceId = pResourceId;
            mValue = pValue;
        }

        int getResourceId() {
            return mResourceId;
        }

        public double getValue() {
            return mValue;
        }
    }
}
