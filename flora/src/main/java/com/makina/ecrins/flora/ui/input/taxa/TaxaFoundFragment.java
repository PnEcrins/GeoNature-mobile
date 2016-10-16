package com.makina.ecrins.flora.ui.input.taxa;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.IValidateWithNavigationControlFragment;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;

/**
 * Step 3: The user must choose an action if he found or not a {@link Taxon}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaFoundFragment
        extends Fragment
        implements IValidateWithNavigationControlFragment,
                   OnClickListener {

    private static final String TAG = TaxaFoundFragment.class.getName();

    private static final String ALERT_DIALOG_DELETE_ALL_AREAS_FRAGMENT = "alert_dialog_delete_all_areas";

    private Input mInput;

    private AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "delete all areas");
            }

            if (mInput.getCurrentSelectedTaxon() != null) {
                ((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                          .clear();
            }

            ((PagerFragmentActivity) getActivity()).goToPageByKey(R.string.pager_fragment_webview_pa_title);
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            // nothing to do ...
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore CommentDialogFragment state after resume if needed
        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager()
                                                                                           .findFragmentByTag(ALERT_DIALOG_DELETE_ALL_AREAS_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_taxa_found,
                                           container,
                                           false);

        view.findViewById(R.id.buttonTaxonFound)
            .setOnClickListener(this);
        view.findViewById(R.id.buttonTaxonNotFound)
            .setOnClickListener(this);

        return view;
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
    public int getResourceTitle() {
        return R.string.pager_fragment_taxa_found_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void refreshView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                                           .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public boolean getPagingToForwardEnabled() {
        return false;
    }

    @Override
    public boolean getPagingToPreviousEnabled() {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonTaxonFound:
                if (mInput.getCurrentSelectedTaxon() != null) {
                    ((Taxon) mInput.getCurrentSelectedTaxon()).setProspectingArea(null);
                }

                ((PagerFragmentActivity) getActivity()).goToNextPage();
                break;
            case R.id.buttonTaxonNotFound:
                if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                                                             .isEmpty())) {
                    ((PagerFragmentActivity) getActivity()).goToPageByKey(R.string.pager_fragment_webview_pa_title);
                }
                else {
                    confirmBeforeDeleteAreas();
                }

                break;
        }
    }

    private void confirmBeforeDeleteAreas() {
        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.alert_dialog_confirm_delete_areas_title,
                                                                                        R.string.alert_dialog_confirm_delete_areas_message);
        alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        alertDialogFragment.show(getActivity().getSupportFragmentManager(),
                                 ALERT_DIALOG_DELETE_ALL_AREAS_FRAGMENT);
    }
}
