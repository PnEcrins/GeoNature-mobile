package com.makina.ecrins.flora.ui.input.frequencies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Frequency;
import com.makina.ecrins.flora.input.Frequency.FrequencyType;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.frequencies.FrequencyFragmentActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Chooses a frequency input method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequenciesListFragment
        extends ListFragment
        implements IValidateFragment {

    private static final String TAG = FrequenciesListFragment.class.getName();

    private Input mInput;

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
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            final Frequency frequency = data.getParcelableExtra(FrequencyFragmentActivity.EXTRA_FREQUENCY);

            if (frequency == null) {
                Log.w(TAG,
                      "onActivityResult: no frequency found!");

                return;
            }

            if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                          .setFrequency(frequency);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l,
                                View v,
                                int position,
                                long id) {
        final Frequency selectedFrequency = ((FrequenciesArrayAdapter) l.getAdapter()).getItem(position);

        if (selectedFrequency == null) {
            return;
        }

        if ((mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                ((((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                            .getFrequency() == null) || ((((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                    .getFrequency() != null) && (!selectedFrequency.getType()
                                                                                                                                                                                   .equals(((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                                                                                                                     .getFrequency()
                                                                                                                                                                                                                                     .getType()))))) {
            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                      .setFrequency(new Frequency(selectedFrequency.getType()));
        }

        ((FrequenciesArrayAdapter) l.getAdapter()).notifyDataSetChanged();

        if (mInput.getCurrentSelectedTaxon() != null) {
            final Intent intent = new Intent(getActivity(),
                                             FrequencyFragmentActivity.class);
            intent.putExtra(FrequencyFragmentActivity.EXTRA_AREA,
                            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea());
            intent.putExtra(FrequencyFragmentActivity.EXTRA_FREQUENCY,
                            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                      .getFrequency());

            startActivityForResult(intent,
                                   0);
        }
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_frequencies_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        if ((mInput.getCurrentSelectedTaxon() == null) || ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() == null))) {
            Log.w(TAG,
                  "validate: no taxon selected !");

            return false;
        }
        else {
            return (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                               .getFrequency() != null) &&
                    (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                               .getFrequency()
                                                               .getValue() > 0);
        }
    }

    @Override
    public void refreshView() {
        if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            Log.d(TAG,
                  "refreshView, current area " + ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                           .getFeature()
                                                                                           .getId());
        }
        else {
            Log.w(TAG,
                  "refreshView, no selected area !");
        }

        if (getListAdapter() == null) {
            FrequenciesArrayAdapter adapter = new FrequenciesArrayAdapter(getActivity(),
                                                                          R.layout.list_item_selected_frequency);
            adapter.add(new Frequency(FrequencyType.ESTIMATION));
            adapter.add(new Frequency(FrequencyType.TRANSECT));

            setListAdapter(adapter);
            setListShown(true);
        }
        else {
            ((FrequenciesArrayAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    private class FrequenciesArrayAdapter
            extends ArrayAdapter<Frequency> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        public FrequenciesArrayAdapter(Context context,
                                       int resource) {
            super(context,
                  resource);

            mResourceId = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position,
                            View convertView,
                            @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(mResourceId,
                                         parent,
                                         false);
            }
            else {
                view = convertView;
            }

            final Frequency frequency = getItem(position);

            if (frequency != null) {
                ((TextView) view.findViewById(R.id.textViewFrequencyName)).setText(getString(frequency.getType()
                                                                                                      .getResourceNameId()));

                if ((mInput.getCurrentSelectedTaxon() != null) &&
                        (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                        (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                   .getFrequency() != null) &&
                        (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                   .getFrequency()
                                                                   .getType()
                                                                   .equals(frequency.getType()))) {
                    NumberFormat numberFormatPercent = DecimalFormat.getPercentInstance();
                    numberFormatPercent.setMaximumFractionDigits(2);

                    ((TextView) view.findViewById(R.id.textViewFrequencyValue)).setText(String.format(getString(R.string.frequencies_selected_frequency_computed),
                                                                                                      numberFormatPercent.format(((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                                                           .getFrequency()
                                                                                                                                                                           .getValue() / 100)));
                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(true);
                    view.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));

                    ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
                }
                else {
                    ((TextView) view.findViewById(R.id.textViewFrequencyValue)).setText("");
                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(false);
                    view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }

            return view;
        }
    }
}
