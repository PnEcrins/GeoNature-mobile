package com.makina.ecrins.flora.ui.input.frequencies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.ThemeUtils;
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
        implements IValidateFragment,
                   IInputFragment {

    private static final String TAG = FrequenciesListFragment.class.getName();

    private Input mInput;

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

            if (mInput == null) {
                return;
            }

            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                currentSelectedTaxon.getCurrentSelectedArea()
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

        if (mInput == null) {
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if ((currentSelectedTaxon != null) &&
                (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                ((currentSelectedTaxon.getCurrentSelectedArea()
                                      .getFrequency() == null) || ((currentSelectedTaxon.getCurrentSelectedArea()
                                                                                        .getFrequency() != null) && (!selectedFrequency.getType()
                                                                                                                                       .equals(currentSelectedTaxon.getCurrentSelectedArea()
                                                                                                                                                                   .getFrequency()
                                                                                                                                                                   .getType()))))) {
            currentSelectedTaxon.getCurrentSelectedArea()
                                .setFrequency(new Frequency(selectedFrequency.getType()));
        }

        ((FrequenciesArrayAdapter) l.getAdapter()).notifyDataSetChanged();

        if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
            final Intent intent = new Intent(getActivity(),
                                             FrequencyFragmentActivity.class);
            intent.putExtra(FrequencyFragmentActivity.EXTRA_AREA,
                            currentSelectedTaxon.getCurrentSelectedArea());
            intent.putExtra(FrequencyFragmentActivity.EXTRA_FREQUENCY,
                            currentSelectedTaxon.getCurrentSelectedArea()
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
        if (mInput == null) {
            return false;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        return !(mInput == null || currentSelectedTaxon == null || currentSelectedTaxon.getCurrentSelectedArea() == null) &&
                currentSelectedTaxon.getCurrentSelectedArea() != null &&
                currentSelectedTaxon.getCurrentSelectedArea()
                                    .getFrequency() != null &&
                currentSelectedTaxon.getCurrentSelectedArea()
                                    .getFrequency()
                                    .getValue() > 0;
    }

    @Override
    public void refreshView() {
        if (mInput == null) {
            Log.w(TAG,
                  "refreshView: null input");
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if ((currentSelectedTaxon != null) &&
                (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                (currentSelectedTaxon.getCurrentSelectedArea()
                                     .getFeature() != null)) {
            Log.d(TAG,
                  "refreshView, current area " + currentSelectedTaxon.getCurrentSelectedArea()
                                                                     .getFeature()
                                                                     .getId());
        }
        else {
            Log.w(TAG,
                  "refreshView, no selected area!");
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

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }

    private class FrequenciesArrayAdapter
            extends ArrayAdapter<Frequency> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        FrequenciesArrayAdapter(Context context,
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

                if (mInput == null) {
                    return view;
                }

                final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                if ((currentSelectedTaxon != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea()
                                             .getFrequency() != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea()
                                             .getFrequency()
                                             .getType()
                                             .equals(frequency.getType()))) {
                    NumberFormat numberFormatPercent = DecimalFormat.getPercentInstance();
                    numberFormatPercent.setMaximumFractionDigits(2);

                    ((TextView) view.findViewById(R.id.textViewFrequencyValue)).setText(String.format(getString(R.string.frequencies_selected_frequency_computed),
                                                                                                      numberFormatPercent.format(currentSelectedTaxon.getCurrentSelectedArea()
                                                                                                                                                     .getFrequency()
                                                                                                                                                     .getValue() / 100)));
                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(true);
                    view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));

                    ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
                }
                else {
                    ((TextView) view.findViewById(R.id.textViewFrequencyValue)).setText("");
                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(false);
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            return view;
        }
    }
}
