package com.geonature.mobile.flora.ui.input.counting;

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
import android.widget.Toast;

import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.ui.input.IInputFragment;
import com.geonature.mobile.commons.ui.pager.IValidateFragment;
import com.geonature.mobile.commons.util.ThemeUtils;
import com.geonature.mobile.flora.R;
import com.geonature.mobile.flora.input.Counting;
import com.geonature.mobile.flora.input.Counting.CountingType;
import com.geonature.mobile.flora.input.Input;
import com.geonature.mobile.flora.input.Taxon;
import com.geonature.mobile.flora.ui.counting.CountingFragmentActivity;

/**
 * Chooses a counting input method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingListFragment
        extends ListFragment
        implements IValidateFragment,
                   IInputFragment {

    private static final String TAG = CountingListFragment.class.getName();

    private Input mInput;

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            final Counting counting = data.getParcelableExtra(CountingFragmentActivity.EXTRA_COUNTING);

            if (counting == null) {
                Log.w(TAG,
                      "onActivityResult: no counting found!");

                return;
            }

            if (mInput == null) {
                return;
            }

            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                currentSelectedTaxon.getCurrentSelectedArea()
                                    .setCounting(counting);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l,
                                View v,
                                int position,
                                long id) {
        final Counting selectedCounting = ((CountingArrayAdapter) l.getAdapter()).getItem(position);

        if (selectedCounting == null) {
            return;
        }

        if (mInput == null) {
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        // sets the selected counting for the current taxon
        if ((currentSelectedTaxon != null) &&
                (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                (!selectedCounting.getType()
                                  .equals(currentSelectedTaxon.getCurrentSelectedArea()
                                                              .getCounting()
                                                              .getType()))) {
            currentSelectedTaxon.getCurrentSelectedArea()
                                .setCounting(new Counting(selectedCounting.getType()));
        }

        ((CountingArrayAdapter) l.getAdapter()).notifyDataSetChanged();

        // starts CountingFragmentActivity only if exhaustive or sampling counting method was selected
        if ((currentSelectedTaxon != null) &&
                (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                (!currentSelectedTaxon.getCurrentSelectedArea()
                                      .getCounting()
                                      .getType()
                                      .equals(CountingType.NONE))) {
            final Intent intent = new Intent(getActivity(),
                                             CountingFragmentActivity.class);
            intent.putExtra(CountingFragmentActivity.EXTRA_AREA,
                            currentSelectedTaxon.getCurrentSelectedArea());
            intent.putExtra(CountingFragmentActivity.EXTRA_COUNTING,
                            currentSelectedTaxon.getCurrentSelectedArea()
                                                .getCounting());

            startActivityForResult(intent,
                                   0);
        }
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_counting_title;
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

        return !(currentSelectedTaxon == null || currentSelectedTaxon.getCurrentSelectedArea() == null) &&
                currentSelectedTaxon.getCurrentSelectedArea() != null && currentSelectedTaxon.getCurrentSelectedArea()
                                                                                             .getCounting()
                                                                                             .isValid();
    }

    @Override
    public void refreshView() {
        if (getListAdapter() == null) {
            CountingArrayAdapter adapter = new CountingArrayAdapter(getActivity(),
                                                                    R.layout.list_item_selected_counting);
            adapter.add(new Counting(CountingType.NONE));
            adapter.add(new Counting(CountingType.EXHAUSTIVE));
            adapter.add(new Counting(CountingType.SAMPLING));

            setListAdapter(adapter);
            setListShown(true);
        }

        if (mInput == null) {
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        // checks the validity of the selected counting method and sets the default one (i.e. NONE) if needed
        if (currentSelectedTaxon != null &&
                currentSelectedTaxon.getCurrentSelectedArea() != null &&
                !currentSelectedTaxon.getCurrentSelectedArea()
                                     .getCounting()
                                     .isValid()) {
            currentSelectedTaxon.getCurrentSelectedArea()
                                .setCounting(new Counting(CountingType.NONE));

            Toast.makeText(getActivity(),
                           R.string.message_counting_invalid,
                           Toast.LENGTH_LONG)
                 .show();
        }

        ((CountingArrayAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }

    private class CountingArrayAdapter
            extends ArrayAdapter<Counting> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        CountingArrayAdapter(Context context,
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

            final Counting counting = getItem(position);

            if (counting != null) {
                ((TextView) view.findViewById(R.id.textViewCountingName)).setText(getString(counting.getType()
                                                                                                    .getResourceNameId()));

                if (mInput == null) {
                    return view;
                }

                final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                if ((currentSelectedTaxon != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea()
                                             .getCounting()
                                             .getType()
                                             .equals(counting.getType()))) {
                    if (currentSelectedTaxon.getCurrentSelectedArea()
                                            .getCounting()
                                            .getType()
                                            .equals(CountingType.NONE)) {
                        ((TextView) view.findViewById(R.id.textViewCountingValue)).setText("");
                    }
                    else {
                        ((TextView) view.findViewById(R.id.textViewCountingValue)).setText(String.format(getString(R.string.counting_selected_counting),
                                                                                                         currentSelectedTaxon.getCurrentSelectedArea()
                                                                                                                             .getCounting()
                                                                                                                             .getTotalFertile(),
                                                                                                         currentSelectedTaxon.getCurrentSelectedArea()
                                                                                                                             .getCounting()
                                                                                                                             .getTotalSterile()));
                    }

                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(true);
                    view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                }
                else {
                    ((TextView) view.findViewById(R.id.textViewCountingValue)).setText("");
                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(false);
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            return view;
        }
    }
}
