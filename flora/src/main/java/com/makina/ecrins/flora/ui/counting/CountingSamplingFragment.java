package com.makina.ecrins.flora.ui.counting;

import android.content.Context;
import android.os.Bundle;
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
import com.makina.ecrins.flora.input.Counting;

import java.text.NumberFormat;

/**
 * Counting: Sampling method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingSamplingFragment
        extends Fragment
        implements OnClickListener {

    private static final String TAG = CountingSamplingFragment.class.getName();

    private static final String ARG_AREA = "ARG_AREA";
    private static final String KEY_COUNTING = "KEY_COUNTING";

    protected EditText mEditTextPlotSurface;
    protected EditText mEditTextCountingPlot;
    protected Button mButtonCountingMinusPlot;
    private Button mButtonCountingPlusPlot;
    protected EditText mEditTextCountingFertile;
    private TextView mTextViewTotalCountingFertile;
    protected EditText mEditTextCountingSterile;
    private TextView mTextViewTotalCountingSterile;

    private Area mArea;
    private Counting mCounting;

    private OnCountingListener mOnCountingListener;

    public CountingSamplingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link CountingSamplingFragment}.
     */
    @NonNull
    public static CountingSamplingFragment newInstance(@NonNull final Area area,
                                                       @NonNull final Counting counting) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_AREA,
                           area);
        args.putParcelable(KEY_COUNTING,
                           counting);

        final CountingSamplingFragment fragment = new CountingSamplingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArea = getArguments().getParcelable(ARG_AREA);
        mCounting = (savedInstanceState == null) ? (Counting) getArguments().getParcelable(KEY_COUNTING) : (Counting) savedInstanceState.getParcelable(KEY_COUNTING);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_counting_sampling,
                                           container,
                                           false);

        mEditTextPlotSurface = (EditText) view.findViewById(R.id.editTextPlotSurface);
        mEditTextCountingPlot = (EditText) view.findViewById(R.id.editTextCountingPlot);
        mButtonCountingMinusPlot = (Button) view.findViewById(R.id.buttonCountingMinusPlot);
        mButtonCountingPlusPlot = (Button) view.findViewById(R.id.buttonCountingPlusPlot);

        mEditTextPlotSurface.addTextChangedListener(new TextWatcher() {
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
                if (s.toString()
                     .isEmpty()) {
                    mCounting.setPlotSurface(0);
                    updateCountingSampling(false);
                }
                else {
                    try {
                        mCounting.setPlotSurface(Double.valueOf(s.toString()));
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(TAG,
                              nfe.getMessage());

                        mCounting.setPlotSurface(0);
                        updateCountingSampling(false);
                    }
                }
            }
        });

        mEditTextCountingPlot.addTextChangedListener(new TextWatcher() {
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
                if (s.toString()
                     .isEmpty()) {
                    mButtonCountingMinusPlot.setEnabled(false);
                    mCounting.setPlots(0);
                    updateCountingSampling(false);
                }
                else {
                    try {
                        mCounting.setPlots(Integer.valueOf(s.toString()));
                        mButtonCountingMinusPlot.setEnabled(mCounting.getPlots() > 0);
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(TAG,
                              nfe.getMessage());

                        mButtonCountingMinusPlot.setEnabled(false);
                        mCounting.setPlots(0);
                        updateCountingSampling(false);
                    }
                }
            }
        });

        mButtonCountingMinusPlot.setOnClickListener(this);
        mButtonCountingPlusPlot.setOnClickListener(this);

        mEditTextCountingFertile = (EditText) view.findViewById(R.id.editTextCountingFertile);
        mTextViewTotalCountingFertile = (TextView) view.findViewById(R.id.textViewTotalCountingFertile);

        mEditTextCountingFertile.addTextChangedListener(new TextWatcher() {
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
                        mCounting.setCountFertile(Integer.valueOf(s.toString()));
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(TAG,
                              nfe.getMessage());

                        mEditTextCountingFertile.setText(NumberFormat.getInstance()
                                                                     .format(0));
                    }
                }
            }
        });

        mEditTextCountingSterile = (EditText) view.findViewById(R.id.editTextCountingSterile);
        mTextViewTotalCountingSterile = (TextView) view.findViewById(R.id.textViewTotalCountingSterile);

        mEditTextCountingSterile.addTextChangedListener(new TextWatcher() {
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
                        mCounting.setCountSterile(Integer.valueOf(s.toString()));
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(TAG,
                              nfe.getMessage());

                        mEditTextCountingSterile.setText(NumberFormat.getInstance()
                                                                     .format(0));
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnCountingListener) {
            mOnCountingListener = (OnCountingListener) context;
        }
        else {
            throw new RuntimeException(getContext().toString() + " must implement OnCountingListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateCountingSampling(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_COUNTING,
                               mCounting);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCountingMinusPlot:
                updateEditText(mEditTextCountingPlot,
                               -1);
                break;
            case R.id.buttonCountingPlusPlot:
                updateEditText(mEditTextCountingPlot,
                               1);
                break;
        }
    }

    private void updateEditText(final EditText editText,
                                final int value) {
        int newValue = value;

        if ((editText.getText() != null) && !(editText.getText()
                                                      .toString()
                                                      .isEmpty())) {
            try {
                newValue += Integer.valueOf(editText.getText()
                                                    .toString());
            }
            catch (NumberFormatException nfe) {
                Log.w(TAG,
                      nfe.getMessage());
            }
        }

        editText.setText(NumberFormat.getInstance()
                                     .format((newValue < 0) ? 0 : newValue));
    }

    private void updateCountingSampling(boolean updateEditText) {
        final double computedArea = mArea.getComputedArea();

        if (mCounting.getPlotSurface() * mCounting.getPlots() > 0) {
            final double countingFertile = (computedArea * mCounting.getCountFertile()) / (mCounting.getPlotSurface() * mCounting.getPlots());
            final double countingSterile = (computedArea * mCounting.getCountSterile()) / (mCounting.getPlotSurface() * mCounting.getPlots());

            mCounting.setTotalFertile(Double.valueOf(countingFertile)
                                            .intValue());
            mCounting.setTotalSterile(Double.valueOf(countingSterile)
                                            .intValue());

            mTextViewTotalCountingFertile.setText(getResources().getQuantityString(R.plurals.counting_sampling_fertile_total_count,
                                                                                   Double.valueOf(countingFertile)
                                                                                         .intValue(),
                                                                                   Double.valueOf(countingFertile)
                                                                                         .intValue()));
            mTextViewTotalCountingSterile.setText(getResources().getQuantityString(R.plurals.counting_sampling_sterile_total_count,
                                                                                   Double.valueOf(countingSterile)
                                                                                         .intValue(),
                                                                                   Double.valueOf(countingSterile)
                                                                                         .intValue()));

            mOnCountingListener.OnCountingUpdated(mCounting,
                                                  true);
        }
        else {
            mCounting.setCountFertile(0);
            mCounting.setCountSterile(0);
            mCounting.setTotalFertile(0);
            mCounting.setTotalSterile(0);

            mTextViewTotalCountingFertile.setText(getResources().getQuantityString(R.plurals.counting_sampling_fertile_total_count,
                                                                                   0,
                                                                                   0));
            mTextViewTotalCountingSterile.setText(getResources().getQuantityString(R.plurals.counting_sampling_sterile_total_count,
                                                                                   0,
                                                                                   0));

            mOnCountingListener.OnCountingUpdated(mCounting,
                                                  false);
        }

        if (updateEditText) {
            mEditTextPlotSurface.setText(NumberFormat.getInstance()
                                                     .format(mCounting.getPlotSurface()));
            mEditTextCountingPlot.setText(NumberFormat.getInstance()
                                                      .format(mCounting.getPlots()));
            mEditTextCountingFertile.setText(NumberFormat.getInstance()
                                                         .format(mCounting.getCountFertile()));
            mEditTextCountingSterile.setText(NumberFormat.getInstance()
                                                         .format(mCounting.getCountSterile()));
        }
    }
}
