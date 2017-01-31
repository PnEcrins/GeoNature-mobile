package com.makina.ecrins.flora.ui.input.remarks;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.IInputFragment;
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
        implements IValidateFragment,
                   IInputFragment {

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
            if (mInput == null) {
                return;
            }

            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                currentSelectedTaxon.getCurrentSelectedArea()
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
        return (mInput != null) &&
                (mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null);
    }

    @Override
    public void refreshView() {
        if (mInput == null) {
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if ((mInput != null) && (currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
            mEditTextRemarks.setText(currentSelectedTaxon.getCurrentSelectedArea()
                                                         .getComment());
        }
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }
}
