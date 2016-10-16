package com.makina.ecrins.flora.ui.input.counting;

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
import android.widget.Toast;

import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Counting;
import com.makina.ecrins.flora.input.Counting.CountingType;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.counting.CountingFragmentActivity;

/**
 * Chooses a counting input method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingListFragment
        extends ListFragment
        implements IValidateFragment {

    private static final String TAG = CountingListFragment.class.getName();

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
            final Counting counting = data.getParcelableExtra(CountingFragmentActivity.EXTRA_COUNTING);

            if (counting == null) {
                Log.w(TAG,
                      "onActivityResult: no counting found!");

                return;
            }

            if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
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

        // sets the selected counting for the current taxon
        if ((mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (!selectedCounting.getType()
                                  .equals(((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                    .getCounting()
                                                                                    .getType()))) {
            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                      .setCounting(new Counting(selectedCounting.getType()));
        }

        ((CountingArrayAdapter) l.getAdapter()).notifyDataSetChanged();

        // starts CountingFragmentActivity only if exhaustive or sampling counting method was selected
        if ((mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (!((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                            .getCounting()
                                                            .getType()
                                                            .equals(CountingType.NONE))) {
            final Intent intent = new Intent(getActivity(),
                                             CountingFragmentActivity.class);
            intent.putExtra(CountingFragmentActivity.EXTRA_AREA,
                            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea());
            intent.putExtra(CountingFragmentActivity.EXTRA_COUNTING,
                            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
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
        if ((mInput.getCurrentSelectedTaxon() == null) || ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() == null))) {
            Log.w(TAG,
                  "validate: no taxon selected !");

            return false;
        }
        else {
            // always true
            return (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                               .getCounting() != null);
        }
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

        // checks the validity of the selected counting method and sets the default one (i.e. NONE) if needed
        if ((mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (!((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                            .getCounting()
                                                            .isValid())) {
            ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                      .setCounting(new Counting(CountingType.NONE));

            Toast.makeText(getActivity(),
                           R.string.message_counting_invalid,
                           Toast.LENGTH_LONG)
                 .show();
        }

        ((CountingArrayAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private class CountingArrayAdapter
            extends ArrayAdapter<Counting> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        public CountingArrayAdapter(Context context,
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

                if ((mInput.getCurrentSelectedTaxon() != null) &&
                        (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                        (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                   .getCounting()
                                                                   .getType()
                                                                   .equals(counting.getType()))) {
                    if (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                  .getCounting()
                                                                  .getType()
                                                                  .equals(CountingType.NONE)) {
                        ((TextView) view.findViewById(R.id.textViewCountingValue)).setText("");
                    }
                    else {
                        ((TextView) view.findViewById(R.id.textViewCountingValue)).setText(String.format(getString(R.string.counting_selected_counting),
                                                                                                         ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                                   .getCounting()
                                                                                                                                                   .getTotalFertile(),
                                                                                                         ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                                                                                                                                   .getCounting()
                                                                                                                                                   .getTotalSterile()));
                    }

                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(true);
                    view.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
                }
                else {
                    ((TextView) view.findViewById(R.id.textViewCountingValue)).setText("");
                    ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(false);
                    view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }

            return view;
        }
    }
}
