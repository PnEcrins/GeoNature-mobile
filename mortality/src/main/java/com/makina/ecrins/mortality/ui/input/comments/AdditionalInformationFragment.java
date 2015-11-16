package com.makina.ecrins.mortality.ui.input.comments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.mortality.MainApplication;
import com.makina.ecrins.mortality.R;
import com.makina.ecrins.mortality.input.Taxon;
import com.makina.ecrins.mortality.ui.input.PagerFragmentActivity;

/**
 * Additional information view.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class AdditionalInformationFragment
        extends Fragment
        implements IValidateFragment,
                   OnClickListener {

    private Taxon mSelectedTaxon = null;

    private CheckBox mCheckBoxSampleTaken;
    private EditText mEditTextAdditionalInformation;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        Log.d(getClass().getName(),
              "onCreateView");

        View view = inflater.inflate(R.layout.fragment_additional_information,
                                     container,
                                     false);

        mEditTextAdditionalInformation = (EditText) view.findViewById(R.id.editTextAdditionalInformation);
        mEditTextAdditionalInformation.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
                // nothing to do ...
            }

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
                // nothing to do ...
            }

            @Override
            public void afterTextChanged(Editable s) {

                if ((mSelectedTaxon != null) && (s.length() > 0) && !s.toString()
                                                                      .equals(mSelectedTaxon.getComment())) {
                    mSelectedTaxon.setComment(s.toString());

                    ((MainApplication) getActivity().getApplication()).getInput()
                                                                      .getTaxa()
                                                                      .put(mSelectedTaxon.getId(),
                                                                           mSelectedTaxon);
                }

                ((PagerFragmentActivity) getActivity()).validateCurrentPage();
            }
        });

        // adding OnFocusChangeListener to this input text to display or hide soft keyboard
        mEditTextAdditionalInformation.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(
                    View v,
                    boolean hasFocus) {

                if (hasFocus) {
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mEditTextAdditionalInformation,
                                                                                                                              0);
                }
                else {
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditTextAdditionalInformation.getWindowToken(),
                                                                                                                                        0);
                }
            }
        });

        mCheckBoxSampleTaken = (CheckBox) view.findViewById(R.id.checkBoxSampleTaken);
        mCheckBoxSampleTaken.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();

        if (mSelectedTaxon != null) {
            mEditTextAdditionalInformation.setText(mSelectedTaxon.getComment());
        }
    }

    @Override
    public int getResourceTitle() {

        return R.string.pager_fragment_additional_information_title;
    }

    @Override
    public boolean getPagingEnabled() {

        return true;
    }

    @Override
    public boolean validate() {

        Taxon selectedTaxon = (Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                        .getTaxa()
                                                                                        .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                               .getCurrentSelectedTaxonId());

        if (selectedTaxon == null) {
            return false;
        }
        else {
            Log.d(getClass().getName(),
                  "validate : " + selectedTaxon.getComment());

            return (selectedTaxon.isMortalitySample()) ? !selectedTaxon.getComment()
                                                                       .isEmpty() : true;
        }
    }

    @Override
    public void refreshView() {

        mSelectedTaxon = (Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId());

        if (mSelectedTaxon == null) {
            Log.w(getClass().getName(),
                  "refreshView, no taxon selected !");
        }
        else {
            Log.d(getClass().getName(),
                  "refreshView, selected taxon : " + mSelectedTaxon.getTaxonId() + ", comment : " + mSelectedTaxon.getComment());

            mCheckBoxSampleTaken.setChecked(mSelectedTaxon.isMortalitySample());
            mEditTextAdditionalInformation.setText(mSelectedTaxon.getComment());

            if (mCheckBoxSampleTaken.isChecked()) {
                mEditTextAdditionalInformation.setHint(R.string.additional_information_hint_mandatory);
            }
            else {
                mEditTextAdditionalInformation.setHint(R.string.additional_information_hint);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.checkBoxSampleTaken:
                CheckBox checkBoxSample = (CheckBox) v;
                checkBoxSample.setSelected(!checkBoxSample.isChecked());

                if (checkBoxSample.isChecked()) {
                    mEditTextAdditionalInformation.setHint(R.string.additional_information_hint_mandatory);
                }
                else {
                    mEditTextAdditionalInformation.setHint(R.string.additional_information_hint);
                }

                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalitySample(checkBoxSample.isChecked());

                ((PagerFragmentActivity) getActivity()).validateCurrentPage();

                break;
        }
    }
}
