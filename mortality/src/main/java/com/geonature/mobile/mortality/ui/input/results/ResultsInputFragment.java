package com.geonature.mobile.mortality.ui.input.results;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.input.AbstractTaxon;
import com.geonature.mobile.commons.ui.input.results.AbstractResultsInputFragment;
import com.geonature.mobile.commons.ui.input.results.ResultsInputTaxaArrayAdapter;
import com.geonature.mobile.mortality.MainApplication;
import com.geonature.mobile.mortality.R;
import com.geonature.mobile.mortality.input.Taxon;
import com.geonature.mobile.mortality.ui.input.PagerFragmentActivity;

/**
 * Results view for the current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ResultsInputFragment
        extends AbstractResultsInputFragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,
                                       container,
                                       savedInstanceState);

        mTaxaAdapter.setOnDisplaySelectedTaxonListener(new ResultsInputTaxaArrayAdapter.OnDisplaySelectedTaxonListener() {
            @Override
            public int getViewResourceId() {

                return R.layout.taxon_results_details;
            }

            @Override
            public void displayDetails(
                    AbstractTaxon selectedTaxon,
                    View parentView) {

                Taxon taxon = (Taxon) selectedTaxon;

                if (taxon.isMortalitySample()) {
                    ((TextView) parentView.findViewById(R.id.textViewTaxonInformation)).setText(R.string.results_sample_taken);
                }
                else {
                    ((TextView) parentView.findViewById(R.id.textViewTaxonInformation)).setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.buttonAddTaxon) {
            // clear the previous selected taxon
            getInput().setCurrentSelectedTaxonId(-1);
            // returns to the taxa view
            ((PagerFragmentActivity) getActivity()).goToPageByKey(R.string.pager_fragment_taxa_title);
        }
    }

    @Override
    public AbstractInput getInput() {

        return ((MainApplication) getActivity().getApplication()).getInput();
    }
}
