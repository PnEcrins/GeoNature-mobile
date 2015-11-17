package com.makina.ecrins.invertebrate.ui.input.results;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.ui.input.results.AbstractResultsInputFragment;
import com.makina.ecrins.commons.ui.input.results.ResultsInputTaxaArrayAdapter;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.R;
import com.makina.ecrins.invertebrate.inputs.Taxon;
import com.makina.ecrins.invertebrate.ui.input.PagerFragmentActivity;

/**
 * Results view for the current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ResultsInputFragment
        extends AbstractResultsInputFragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
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
                TextView textViewTaxonCounting = (TextView) parentView.findViewById(R.id.textViewTaxonCounting);
                textViewTaxonCounting.setText(getResources().getQuantityString(R.plurals.results_input_selected_taxon_counting,
                                                                               taxon.counting(),
                                                                               taxon.counting()));

                TextView textViewTaxonCriterion = (TextView) parentView.findViewById(R.id.textViewTaxonCriterion);
                textViewTaxonCriterion.setText(getString(R.string.results_input_selected_taxon_criterion,
                                                         taxon.getCriterionLabel()));
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
