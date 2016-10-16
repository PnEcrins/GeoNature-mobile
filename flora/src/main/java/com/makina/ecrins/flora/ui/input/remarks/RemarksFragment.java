package com.makina.ecrins.flora.ui.input.remarks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;

/**
 * Remarks and comments view.
 * <p>
 * This is the last step for the current {@link Area} editing.
 * </p>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class RemarksFragment
        extends Fragment
        implements IValidateFragment {

    private EditText mEditTextRemarks;

    private Input mInput;

    private final TextWatcher mTextWatcher = new TextWatcher() {
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
            if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                          .setComment(s.toString());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remarks,
                                     container,
                                     false);

        mEditTextRemarks = (EditText) view.findViewById(R.id.editTextRemarks);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mEditTextRemarks.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnInputFragmentListener) {
            final OnInputFragmentListener onInputFragmentListener = (OnInputFragmentListener) context;
            mInput = (Input) onInputFragmentListener.getInput();
        }
        else {
            throw new RuntimeException(getContext().toString() + " must implement OnInputFragmentListener");
        }
    }

    @Override
    public void onPause() {
        mEditTextRemarks.removeTextChangedListener(mTextWatcher);

        super.onPause();
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_remarks_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        return (mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null);
    }

    @Override
    public void refreshView() {
        if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            mEditTextRemarks.setText(((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                               .getComment());
        }
    }
}
