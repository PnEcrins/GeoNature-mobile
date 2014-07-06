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

import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Taxon;

/**
 * Counting: Exhaustive method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingExhaustiveFragment extends Fragment implements OnClickListener {

    private EditText mEditTextCountingFertile;
    protected Button mButtonCountingMinusFertile;
    private Button mButtonCountingPlusFertile;
    private EditText mEditTextCountingSterile;
    protected Button mButtonCountingMinusSterile;
    private Button mButtonCountingPlusSterile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counting_exhaustive, container, false);

        mEditTextCountingFertile = (EditText) view.findViewById(R.id.editTextCountingFertile);
        mButtonCountingMinusFertile = (Button) view.findViewById(R.id.buttonCountingMinusFertile);
        mButtonCountingPlusFertile = (Button) view.findViewById(R.id.buttonCountingPlusFertile);

        mEditTextCountingSterile = (EditText) view.findViewById(R.id.editTextCountingSterile);
        mButtonCountingMinusSterile = (Button) view.findViewById(R.id.buttonCountingMinusSterile);
        mButtonCountingPlusSterile = (Button) view.findViewById(R.id.buttonCountingPlusSterile);

        mButtonCountingMinusFertile.setOnClickListener(this);
        mButtonCountingPlusFertile.setOnClickListener(this);
        mButtonCountingMinusSterile.setOnClickListener(this);
        mButtonCountingPlusSterile.setOnClickListener(this);

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
                if (s.toString()
                        .isEmpty()) {
                    mButtonCountingMinusFertile.setEnabled(false);
                }
                else {
                    try {
                        int value = Integer.valueOf(s.toString());

                        if ((((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon() != null) &&
                                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                            mButtonCountingMinusFertile.setEnabled(value > 0);
                            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                    .getCounting()
                                    .setCountFertile(value);
                            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                    .getCounting()
                                    .setTotalFertile(value);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(CountingExhaustiveFragment.class.getName(), nfe.getMessage());

                        mButtonCountingMinusFertile.setEnabled(false);
                    }
                }
            }
        });

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
                if (s.toString()
                        .isEmpty()) {
                    mButtonCountingMinusSterile.setEnabled(false);
                }
                else {
                    try {
                        int value = Integer.valueOf(s.toString());

                        if ((((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon() != null) &&
                                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                            mButtonCountingMinusSterile.setEnabled(value > 0);
                            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                    .getCounting()
                                    .setCountSterile(value);
                            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                    .getCounting()
                                    .setTotalSterile(value);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(CountingExhaustiveFragment.class.getName(), nfe.getMessage());

                        mButtonCountingMinusSterile.setEnabled(false);
                    }
                }
            }
        });

        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            mEditTextCountingFertile.setText(Integer.toString(((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getCountFertile()));
            mEditTextCountingSterile.setText(Integer.toString(((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getCounting()
                    .getCountSterile()));
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCountingMinusFertile:
                updateEditText(mEditTextCountingFertile, -1);
                break;
            case R.id.buttonCountingPlusFertile:
                updateEditText(mEditTextCountingFertile, 1);
                break;
            case R.id.buttonCountingMinusSterile:
                updateEditText(mEditTextCountingSterile, -1);
                break;
            case R.id.buttonCountingPlusSterile:
                updateEditText(mEditTextCountingSterile, 1);
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
                Log.w(CountingExhaustiveFragment.class.getName(), nfe.getMessage());
            }
        }

        editText.setText(Integer.toString((newValue < 0) ? 0 : newValue));
    }
}
