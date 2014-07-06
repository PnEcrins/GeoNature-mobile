package com.makina.ecrins.flora.ui.counting;

import android.os.Bundle;
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

/**
 * Counting: Sampling method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingSamplingFragment extends Fragment implements OnClickListener {

    protected static final String KEY_PLOT_SURFACE = "plot_surface";
    protected static final String KEY_COUNTING_PLOT = "counting_plot";
    protected static final String KEY_COUNTING_FERTILE = "counting_fertile";
    protected static final String KEY_COUNTING_STERILE = "counting_sterile";

    protected Bundle mSavedState;

    protected EditText mEditTextPlotSurface;
    protected EditText mEditTextCountingPlot;
    protected Button mButtonCountingMinusPlot;
    private Button mButtonCountingPlusPlot;
    protected EditText mEditTextCountingFertile;
    private TextView mTextViewTotalCountingFertile;
    protected EditText mEditTextCountingSterile;
    private TextView mTextViewTotalCountingSterile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Log.d(CountingSamplingFragment.class.getName(), "onCreate, savedInstanceState null");

            mSavedState = new Bundle();
        }
        else {
            Log.d(CountingSamplingFragment.class.getName(), "onCreate, savedInstanceState initialized");

            mSavedState = savedInstanceState;
        }

        // restore all data
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            mSavedState.putDouble(KEY_PLOT_SURFACE, ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getPlotSurface());
            mSavedState.putInt(KEY_COUNTING_PLOT, ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getPlots());
            mSavedState.putInt(KEY_COUNTING_FERTILE, ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getCountFertile());
            mSavedState.putInt(KEY_COUNTING_STERILE, ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getCountSterile());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counting_sampling, container, false);

        mEditTextPlotSurface = (EditText) view.findViewById(R.id.editTextPlotSurface);
        mEditTextCountingPlot = (EditText) view.findViewById(R.id.editTextCountingPlot);
        mButtonCountingMinusPlot = (Button) view.findViewById(R.id.buttonCountingMinusPlot);
        mButtonCountingPlusPlot = (Button) view.findViewById(R.id.buttonCountingPlusPlot);

        mEditTextPlotSurface.addTextChangedListener(new TextWatcher() {
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
                if (s.toString()
                        .isEmpty()) {
                    mSavedState.putDouble(KEY_PLOT_SURFACE, 0);
                    updateCountingSampling(false);
                }
                else {
                    try {
                        mSavedState.putDouble(KEY_PLOT_SURFACE, Double.valueOf(s.toString()));
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(CountingSamplingFragment.class.getName(), nfe.getMessage());

                        mSavedState.putDouble(KEY_PLOT_SURFACE, 0);
                        updateCountingSampling(false);
                    }
                }
            }
        });

        mEditTextCountingPlot.addTextChangedListener(new TextWatcher() {
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
                if (s.toString()
                        .isEmpty()) {
                    mButtonCountingMinusPlot.setEnabled(false);
                    mSavedState.putInt(KEY_COUNTING_PLOT, 0);
                    updateCountingSampling(false);
                }
                else {
                    try {
                        mSavedState.putInt(KEY_COUNTING_PLOT, Integer.valueOf(s.toString()));
                        mButtonCountingMinusPlot.setEnabled(mSavedState.getInt(KEY_COUNTING_PLOT) > 0);
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(CountingSamplingFragment.class.getName(), nfe.getMessage());

                        mButtonCountingMinusPlot.setEnabled(false);
                        mSavedState.putInt(KEY_COUNTING_PLOT, 0);
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do ...
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do ...
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString()
                        .isEmpty()) {
                    try {
                        mSavedState.putInt(KEY_COUNTING_FERTILE, Integer.valueOf(s.toString()));
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(CountingSamplingFragment.class.getName(), nfe.getMessage());

                        mEditTextCountingFertile.setText(Integer.toString(0));
                    }
                }
            }
        });

        mEditTextCountingSterile = (EditText) view.findViewById(R.id.editTextCountingSterile);
        mTextViewTotalCountingSterile = (TextView) view.findViewById(R.id.textViewTotalCountingSterile);

        mEditTextCountingSterile.addTextChangedListener(new TextWatcher() {
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
                if (!s.toString()
                        .isEmpty()) {
                    try {
                        mSavedState.putInt(KEY_COUNTING_STERILE, Integer.valueOf(s.toString()));
                        updateCountingSampling(false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(CountingSamplingFragment.class.getName(), nfe.getMessage());

                        mEditTextCountingSterile.setText(Integer.toString(0));
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateCountingSampling(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(CountingSamplingFragment.class.getName(), "onSaveInstanceState");

        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCountingMinusPlot:
                updateEditText(mEditTextCountingPlot, -1);
                break;
            case R.id.buttonCountingPlusPlot:
                updateEditText(mEditTextCountingPlot, 1);
                break;
        }
    }

    private void updateEditText(final EditText editText, final int value) {
        int newValue = value;

        if ((editText.getText() != null) && !(editText.getText()
                .toString()
                .isEmpty())) {
            try {
                newValue += Integer.valueOf(editText.getText()
                        .toString());
            }
            catch (NumberFormatException nfe) {
                Log.w(CountingSamplingFragment.class.getName(), nfe.getMessage());
            }
        }

        editText.setText(Integer.toString((newValue < 0) ? 0 : newValue));
    }

    private void updateCountingSampling(boolean updateEditText) {
        double computedArea = 0.0;

        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            final Area selectedArea = ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea();

            if (selectedArea != null) {
                computedArea = selectedArea.getComputedArea();
            }

            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .setPlotSurface(mSavedState.getDouble(KEY_PLOT_SURFACE, 0));
            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .setPlots(mSavedState.getInt(KEY_COUNTING_PLOT, 0));

            if (mSavedState.getDouble(KEY_PLOT_SURFACE, 0) * mSavedState.getInt(KEY_COUNTING_PLOT, 0) > 0) {
                ((CountingFragmentActivity) getActivity()).enableFinish(true);

                double countingFertile = (computedArea * mSavedState.getInt(KEY_COUNTING_FERTILE, 0)) / (mSavedState.getDouble(KEY_PLOT_SURFACE, 0) * mSavedState.getInt(KEY_COUNTING_PLOT, 0));
                double countingSterile = (computedArea * mSavedState.getInt(KEY_COUNTING_STERILE, 0)) / (mSavedState.getDouble(KEY_PLOT_SURFACE, 0) * mSavedState.getInt(KEY_COUNTING_PLOT, 0));

                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setCountFertile(mSavedState.getInt(KEY_COUNTING_FERTILE, 0));
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setCountSterile(mSavedState.getInt(KEY_COUNTING_STERILE, 0));
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setTotalFertile(Double.valueOf(countingFertile)
                                .intValue());
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setTotalSterile(Double.valueOf(countingSterile)
                                .intValue());

                mTextViewTotalCountingFertile.setText(getResources().getQuantityString(R.plurals.counting_sampling_fertile_total_count, Double.valueOf(countingFertile)
                        .intValue(), Double.valueOf(countingFertile)
                        .intValue()));
                mTextViewTotalCountingSterile.setText(getResources().getQuantityString(R.plurals.counting_sampling_sterile_total_count, Double.valueOf(countingSterile)
                        .intValue(), Double.valueOf(countingSterile)
                        .intValue()));
            }
            else {
                ((CountingFragmentActivity) getActivity()).enableFinish(false);

                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setCountFertile(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setCountSterile(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setTotalFertile(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getCounting()
                        .setTotalSterile(0);

                mTextViewTotalCountingFertile.setText(getResources().getQuantityString(R.plurals.counting_sampling_fertile_total_count, 0, 0));
                mTextViewTotalCountingSterile.setText(getResources().getQuantityString(R.plurals.counting_sampling_sterile_total_count, 0, 0));
            }

            if (updateEditText) {
                mEditTextPlotSurface.setText(Double.toString(mSavedState.getDouble(KEY_PLOT_SURFACE, 0)));
                mEditTextCountingPlot.setText(Integer.toString(mSavedState.getInt(KEY_COUNTING_PLOT, 0)));
                mEditTextCountingFertile.setText(Integer.toString(mSavedState.getInt(KEY_COUNTING_FERTILE, 0)));
                mEditTextCountingSterile.setText(Integer.toString(mSavedState.getInt(KEY_COUNTING_STERILE, 0)));
            }
        }
    }
}
