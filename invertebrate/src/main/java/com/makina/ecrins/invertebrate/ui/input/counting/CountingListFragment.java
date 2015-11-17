package com.makina.ecrins.invertebrate.ui.input.counting;

import android.util.Log;

import com.makina.ecrins.commons.ui.input.counting.AbstractCountingListFragment;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.inputs.Taxon;
import com.makina.ecrins.invertebrate.ui.input.PagerFragmentActivity;

/**
 * Counting {@link Taxon} list view with minus and plus buttons.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingListFragment
        extends AbstractCountingListFragment {

    private Taxon mSelectedTaxon;

    @Override
    public void refreshView() {

        mSelectedTaxon = (Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId());

        if (mSelectedTaxon == null) {
            Log.w(getClass().getName(),
                  "no taxon selected !");
        }
        else {
            getAdapter().clear();

            Log.d(getClass().getName(),
                  "selectedTaxon " + mSelectedTaxon.getTaxonId());

            getValues().put(COUNTING_ADULT_MALE,
                            mSelectedTaxon.getCountAdultMale());
            getValues().put(COUNTING_ADULT_FEMALE,
                            mSelectedTaxon.getCountAdultFemale());
            getValues().put(COUNTING_ADULT_UNDETERMINED,
                            mSelectedTaxon.getCountAdultUndetermined());
            getValues().put(COUNTING_NOT_ADULT,
                            mSelectedTaxon.getCountNotAdult());

            getAdapter().add(COUNTING_ADULT_MALE);
            getAdapter().add(COUNTING_ADULT_FEMALE);
            getAdapter().add(COUNTING_ADULT_UNDETERMINED);
            getAdapter().add(COUNTING_NOT_ADULT);
        }
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
                  "validate: " + selectedTaxon.counting());

            return selectedTaxon.counting() > 0;
        }
    }

    @Override
    protected void updateAndNotify(Integer key) {

        if (mSelectedTaxon != null) {
            switch (key) {
                case COUNTING_ADULT_MALE:
                    mSelectedTaxon.setCountAdultMale(getValues().get(key));
                    break;
                case COUNTING_ADULT_FEMALE:
                    mSelectedTaxon.setCountAdultFemale(getValues().get(key));
                    break;
                case COUNTING_ADULT_UNDETERMINED:
                    mSelectedTaxon.setCountAdultUndetermined(getValues().get(key));
                    break;
                case COUNTING_NOT_ADULT:
                    mSelectedTaxon.setCountNotAdult(getValues().get(key));
                    break;
            }

            ((PagerFragmentActivity) getActivity()).validateCurrentPage();
        }
    }
}
