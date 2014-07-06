package com.makina.ecrins.flora.ui.input.counting;

import android.content.Context;
import android.content.Intent;
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

import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Counting;
import com.makina.ecrins.flora.input.Counting.CountingType;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.counting.CountingFragmentActivity;

/**
 * Chooses a counting input method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CountingListFragment extends ListFragment implements IValidateFragment {

    private static final String TAG = CountingListFragment.class.getName();

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Counting selectedCounting = ((CountingArrayAdapter) l.getAdapter()).getItem(position);

        // sets the selected counting for the current taxon
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (!selectedCounting.getType()
                        .equals(((Taxon) ((MainApplication) getActivity().getApplication())
                                .getInput().getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                .getCounting().getType()))) {
            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .setCounting(new Counting(selectedCounting.getType()));
        }

        ((CountingArrayAdapter) l.getAdapter()).notifyDataSetChanged();

        // starts CountingFragmentActivity only if exhaustive or sampling counting method was selected
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (!((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea().getCounting().getType()
                        .equals(CountingType.NONE))) {
            startActivity(new Intent(getActivity(), CountingFragmentActivity.class));
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
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() == null) ||
                ((((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null))) {
            Log.w(TAG, "validate: no taxon selected !");

            return false;
        }
        else {
            // always true
            return (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                            .getCounting() != null);
        }
    }

    @Override
    public void refreshView() {
        Log.d(TAG, "refreshView");

        if (getListAdapter() == null) {
            CountingArrayAdapter adapter = new CountingArrayAdapter(getActivity(), R.layout.list_item_selected_counting);
            adapter.add(new Counting(CountingType.NONE));
            adapter.add(new Counting(CountingType.EXHAUSTIVE));
            adapter.add(new Counting(CountingType.SAMPLING));

            setListAdapter(adapter);
            setListShown(true);
        }

        // checks the validity of the selected counting method and sets the default one (i.e. NONE) if needed
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                (!((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea().getCounting()
                        .isValid())) {
            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .setCounting(new Counting(CountingType.NONE));

            Toast.makeText(
                    getActivity(),
                    R.string.message_counting_invalid,
                    Toast.LENGTH_LONG).show();
        }

        ((CountingArrayAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private class CountingArrayAdapter extends ArrayAdapter<Counting> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        public CountingArrayAdapter(Context context, int resource) {
            super(context, resource);

            mResourceId = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(mResourceId, parent, false);
            }
            else {
                view = convertView;
            }

            ((TextView) view.findViewById(R.id.textViewCountingName)).setText(
                    getString(getItem(position).getType().getResourceNameId()));

            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea().getCounting()
                            .getType().equals(getItem(position).getType()))) {
                if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea().getCounting().getType()
                        .equals(CountingType.NONE)) {
                    ((TextView) view.findViewById(R.id.textViewCountingValue)).setText("");
                }
                else {
                    ((TextView) view.findViewById(R.id.textViewCountingValue)).setText(
                            String.format(
                                    getString(R.string.counting_selected_counting),
                                    ((Taxon) ((MainApplication) getActivity().getApplication())
                                            .getInput().getCurrentSelectedTaxon())
                                            .getCurrentSelectedArea().getCounting()
                                            .getTotalFertile(),
                                    ((Taxon) ((MainApplication) getActivity().getApplication())
                                            .getInput().getCurrentSelectedTaxon())
                                            .getCurrentSelectedArea().getCounting()
                                            .getTotalSterile())
                    );
                }

                ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(true);
                view.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
            }
            else {
                ((TextView) view.findViewById(R.id.textViewCountingValue)).setText("");
                ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(false);
                view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            return view;
        }
    }
}
