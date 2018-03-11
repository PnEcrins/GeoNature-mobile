package com.geonature.mobile.flora.ui.counting;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.geonature.mobile.flora.R;
import com.geonature.mobile.flora.input.Counting;

import java.text.NumberFormat;

/**
 * Counting: Exhaustive method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingExhaustiveFragment
        extends Fragment
        implements OnClickListener {

    private static final String TAG = CountingExhaustiveFragment.class.getName();

    private static final String KEY_COUNTING = "KEY_COUNTING";

    private EditText mEditTextCountingFertile;
    private Button mButtonCountingMinusFertile;
    private Button mButtonCountingPlusFertile;
    private EditText mEditTextCountingSterile;
    private Button mButtonCountingMinusSterile;
    private Button mButtonCountingPlusSterile;

    private Counting mCounting;

    private OnCountingListener mOnCountingListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link CountingExhaustiveFragment}.
     */
    @NonNull
    public static CountingExhaustiveFragment newInstance(@NonNull final Counting counting) {
        final Bundle args = new Bundle();
        args.putParcelable(KEY_COUNTING,
                           counting);

        final CountingExhaustiveFragment fragment = new CountingExhaustiveFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCounting = (savedInstanceState == null) ? (Counting) getArguments().getParcelable(KEY_COUNTING) : (Counting) savedInstanceState.getParcelable(KEY_COUNTING);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_counting_exhaustive,
                                           container,
                                           false);

        mEditTextCountingFertile = view.findViewById(R.id.editTextCountingFertile);
        mButtonCountingMinusFertile = view.findViewById(R.id.buttonCountingMinusFertile);
        mButtonCountingPlusFertile = view.findViewById(R.id.buttonCountingPlusFertile);

        mEditTextCountingSterile = view.findViewById(R.id.editTextCountingSterile);
        mButtonCountingMinusSterile = view.findViewById(R.id.buttonCountingMinusSterile);
        mButtonCountingPlusSterile = view.findViewById(R.id.buttonCountingPlusSterile);

        mButtonCountingMinusFertile.setOnClickListener(this);
        mButtonCountingPlusFertile.setOnClickListener(this);
        mButtonCountingMinusSterile.setOnClickListener(this);
        mButtonCountingPlusSterile.setOnClickListener(this);

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
                if (s.toString()
                     .isEmpty()) {
                    mButtonCountingMinusFertile.setEnabled(false);
                }
                else {
                    try {
                        int value = Integer.valueOf(s.toString());

                        mButtonCountingMinusFertile.setEnabled(value > 0);
                        mCounting.setCountFertile(value);
                        mCounting.setTotalFertile(value);

                        mOnCountingListener.OnCountingUpdated(mCounting,
                                                              true);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(TAG,
                              nfe.getMessage());

                        mButtonCountingMinusFertile.setEnabled(false);
                    }
                }
            }
        });

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
                if (s.toString()
                     .isEmpty()) {
                    mButtonCountingMinusSterile.setEnabled(false);
                }
                else {
                    try {
                        int value = Integer.valueOf(s.toString());

                        mButtonCountingMinusSterile.setEnabled(value > 0);
                        mCounting.setCountSterile(value);
                        mCounting.setTotalSterile(value);

                        mOnCountingListener.OnCountingUpdated(mCounting,
                                                              true);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(TAG,
                              nfe.getMessage());

                        mButtonCountingMinusSterile.setEnabled(false);
                    }
                }
            }
        });

        mEditTextCountingFertile.setText(NumberFormat.getInstance()
                                                     .format(mCounting.getCountFertile()));
        mEditTextCountingSterile.setText(NumberFormat.getInstance()
                                                     .format(mCounting.getCountSterile()));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnCountingListener) {
            mOnCountingListener = (OnCountingListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnCountingListener");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_COUNTING,
                               mCounting);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCountingMinusFertile:
                updateEditText(mEditTextCountingFertile,
                               -1);
                break;
            case R.id.buttonCountingPlusFertile:
                updateEditText(mEditTextCountingFertile,
                               1);
                break;
            case R.id.buttonCountingMinusSterile:
                updateEditText(mEditTextCountingSterile,
                               -1);
                break;
            case R.id.buttonCountingPlusSterile:
                updateEditText(mEditTextCountingSterile,
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
                Log.w(CountingExhaustiveFragment.class.getName(),
                      nfe.getMessage());
            }
        }

        editText.setText(NumberFormat.getInstance()
                                     .format((newValue < 0) ? 0 : newValue));
    }
}
