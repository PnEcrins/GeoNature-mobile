package com.makina.ecrins.mortality.ui.input.counting;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.ThemeUtils;
import com.makina.ecrins.mortality.MainApplication;
import com.makina.ecrins.mortality.R;
import com.makina.ecrins.mortality.input.Taxon;
import com.makina.ecrins.mortality.ui.input.PagerFragmentActivity;

/**
 * Basic counting {@link Taxon} list view.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SimpleCountingListFragment
        extends ListFragment
        implements IValidateFragment {

    private CountingAdapter mAdapter;

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {
        // give some text to display if there is no data
        setEmptyText(getString(R.string.counting_no_data));

        // create an empty adapter we will use to display the counting part according to the counting class of the selected taxon
        mAdapter = new CountingAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(
            ListView l,
            View v,
            int position,
            long id) {

        switch (mAdapter.getItem(position)) {
            case CountingAdapter.COUNTING_ADULT_MALE:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(1);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(0);
                break;
            case CountingAdapter.COUNTING_ADULT_FEMALE:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(1);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(0);
                break;
            case CountingAdapter.COUNTING_ADULT_UNDETERMINED:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(1);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(0);
                break;
            case CountingAdapter.COUNTING_NOT_ADULT:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(1);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(0);
                break;
            case CountingAdapter.COUNTING_YOUNG:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(1);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(0);
                break;
            case CountingAdapter.COUNTING_YEARLING:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(1);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(0);
                break;
            case CountingAdapter.COUNTING_UNDETERMINED:
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultMale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultFemale(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityAdultUndetermined(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityNotAdult(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYoung(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityYearling(0);
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                           .getTaxa()
                                                                           .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                  .getCurrentSelectedTaxonId())).setMortalityUndetermined(1);
                break;
        }

        mAdapter.notifyDataSetChanged();

        ((PagerFragmentActivity) getActivity()).validateCurrentPage();
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

        Taxon selectedTaxon = (Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                        .getTaxa()
                                                                                        .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                               .getCurrentSelectedTaxonId());

        if (selectedTaxon == null) {
            return false;
        }
        else {
            Log.d(getClass().getName(),
                  "validate : " + selectedTaxon.counting());

            return selectedTaxon.counting() > 0;
        }
    }

    @Override
    public void refreshView() {

        Log.d(getClass().getName(),
              "refreshView");

        Taxon selectedTaxon = (Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                        .getTaxa()
                                                                                        .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                               .getCurrentSelectedTaxonId());

        if (selectedTaxon == null) {
            Log.w(getClass().getName(),
                  "no taxon selected !");
        }
        else {
            mAdapter.clear();

            Log.d(getClass().getName(),
                  "selectedTaxon " + selectedTaxon.getTaxonId() + ", class : " + selectedTaxon.getClassId());

            switch (selectedTaxon.getClassCount()) {
                case 6:
                    mAdapter.add(CountingAdapter.COUNTING_ADULT_MALE);
                    mAdapter.add(CountingAdapter.COUNTING_ADULT_FEMALE);
                    mAdapter.add(CountingAdapter.COUNTING_ADULT_UNDETERMINED);
                    mAdapter.add(CountingAdapter.COUNTING_YOUNG);
                    mAdapter.add(CountingAdapter.COUNTING_YEARLING);
                    mAdapter.add(CountingAdapter.COUNTING_UNDETERMINED);
                    break;
                default:
                    mAdapter.add(CountingAdapter.COUNTING_ADULT_MALE);
                    mAdapter.add(CountingAdapter.COUNTING_ADULT_FEMALE);
                    mAdapter.add(CountingAdapter.COUNTING_ADULT_UNDETERMINED);
                    mAdapter.add(CountingAdapter.COUNTING_NOT_ADULT);
                    mAdapter.add(CountingAdapter.COUNTING_UNDETERMINED);
                    break;
            }
        }
    }

    private class CountingAdapter
            extends ArrayAdapter<Integer> {

        public static final int COUNTING_ADULT_MALE = 0;
        public static final int COUNTING_ADULT_FEMALE = 1;
        public static final int COUNTING_ADULT_UNDETERMINED = 2;
        public static final int COUNTING_NOT_ADULT = 3;
        public static final int COUNTING_YOUNG = 4;
        public static final int COUNTING_YEARLING = 5;
        public static final int COUNTING_UNDETERMINED = 6;

        private final LayoutInflater mInflater;

        public CountingAdapter(Context context) {

            super(context,
                  R.layout.list_item_simple_counting);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(
                final int position,
                View convertView,
                ViewGroup parent) {

            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_simple_counting,
                                         parent,
                                         false);
            }
            else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(R.id.textViewTaxonCounting);

            ImageView imageView = (ImageView) view.findViewById(R.id.imageViewTaxonCounting);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                           48,
                                                           getResources().getDisplayMetrics());
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                            48,
                                                            getResources().getDisplayMetrics());
            imageView.setLayoutParams(params);

            Log.d(getClass().getName(),
                  "getView " + getItem(position));

            switch (getItem(position)) {
                case COUNTING_ADULT_MALE:
                    textView.setText(R.string.counting_adult_male);
                    imageView.setImageResource(R.drawable.ic_male_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityAdultMale() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
                case COUNTING_ADULT_FEMALE:
                    textView.setText(R.string.counting_adult_female);
                    imageView.setImageResource(R.drawable.ic_female_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityAdultFemale() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
                case COUNTING_ADULT_UNDETERMINED:
                    textView.setText(R.string.counting_adult_undetermined);
                    imageView.setImageResource(R.drawable.ic_unspecified_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityAdultUndetermined() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
                case COUNTING_NOT_ADULT:
                    textView.setText(R.string.counting_not_adult);

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                   32,
                                                                   getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                    32,
                                                                    getResources().getDisplayMetrics());
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ScaleType.CENTER_INSIDE);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityNotAdult() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
                case COUNTING_YOUNG:
                    textView.setText(R.string.counting_young);

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                   32,
                                                                   getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                    32,
                                                                    getResources().getDisplayMetrics());
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ScaleType.CENTER_INSIDE);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityYoung() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
                case COUNTING_YEARLING:
                    textView.setText(R.string.counting_yearling);

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                   32,
                                                                   getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                    32,
                                                                    getResources().getDisplayMetrics());
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ScaleType.CENTER_INSIDE);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityYearling() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
                case COUNTING_UNDETERMINED:
                    textView.setText(R.string.counting_undetermined);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                                                                   .getTaxa()
                                                                                   .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                          .getCurrentSelectedTaxonId())).getMortalityUndetermined() > 0) {
                        getListView().setSelection(position);
                        view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                    }
                    else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    break;
            }

            return view;
        }
    }
}
