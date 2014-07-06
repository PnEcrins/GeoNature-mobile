package com.makina.ecrins.flora.ui.input.remarks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Taxon;

/**
 * Remarks and comments view.
 * <p>
 * This is the last step for the current {@link Area} editing.
 * </p>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class RemarksFragment extends Fragment implements IValidateFragment {

    private static final String TAG = RemarksFragment.class.getName();

    private EditText mEditTextRemarks;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // nothing to do ...
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // nothing to do ...
        }

        @Override
        public void afterTextChanged(Editable s) {
            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .setComment(s.toString());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_remarks, container, false);

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
        return (((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null);
    }

    @Override
    public void refreshView() {
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            mEditTextRemarks.setText(((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea().getComment());
        }
    }
}
