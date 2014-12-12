package com.makina.ecrins.commons.ui.input.counting;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;

/**
 * Counting {@link com.makina.ecrins.commons.input.AbstractTaxon} list view with minus and plus buttons.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractCountingListFragment extends ListFragment implements IValidateFragment {

    public static final int COUNTING_ADULT_MALE = 0;
    public static final int COUNTING_ADULT_FEMALE = 1;
    public static final int COUNTING_ADULT_UNDETERMINED = 2;
    public static final int COUNTING_NOT_ADULT = 3;
    public static final int COUNTING_YOUNG = 4;
    public static final int COUNTING_YEARLING = 5;
    public static final int COUNTING_UNDETERMINED = 6;

    private CountingAdapter mAdapter;
    private final SparseIntArray mValues = new SparseIntArray();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // give some text to display if there is no data
        setEmptyText(getString(R.string.counting_no_data));

        getListView().setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
        getListView().setItemsCanFocus(true);
        getListView().setClickable(false);

        // create an empty adapter we will use to display the counting part according to the counting class of the selected taxon
        mAdapter = new CountingAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_counting_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    public CountingAdapter getAdapter() {
        return this.mAdapter;
    }

    public SparseIntArray getValues() {
        return this.mValues;
    }

    protected abstract void updateAndNotify(Integer key);

    protected final class CountingAdapter extends ArrayAdapter<Integer> implements OnClickListener {
        private final int mTextViewResourceId;
        private final LayoutInflater mInflater;
        private final SparseArray<EditTextWatcher> mTextWatchers;

        public CountingAdapter(Context context) {
            super(context, R.layout.list_item_counting);
            mTextViewResourceId = R.layout.list_item_counting;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mTextWatchers = new SparseArray<>();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;

            final Integer item = getItem(position);

            if (convertView == null) {
                view = mInflater.inflate(mTextViewResourceId, parent, false);

                final EditText editText = (EditText) view.findViewById(R.id.editTextCounting);

                editText.setTag(item);
                editText.setOnClickListener(this);
                view.findViewById(R.id.buttonCountingMinus).setOnClickListener(this);
                view.findViewById(R.id.buttonCountingPlus).setOnClickListener(this);

                editText.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (v == editText) {

                            // noinspection StatementWithEmptyBody
                            if (hasFocus) {
                                // nothing to do ...
                            }
                            else {
                                editText.setCursorVisible(false);
                                editText.setFocusable(false);
                                editText.setFocusableInTouchMode(false);

                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            }
                        }
                    }
                });
            }
            else {
                view = convertView;
            }

            final EditText editText = (EditText) view.findViewById(R.id.editTextCounting);
            final TextView textView = (TextView) view.findViewById(R.id.textViewTaxonCounting);

            final ImageView imageView = (ImageView) view.findViewById(R.id.imageViewTaxonCounting);
            final LayoutParams params = imageView.getLayoutParams();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
            imageView.setLayoutParams(params);

            // register / unregister a EditTextWatcher instance for the given EditText
            if (mTextWatchers.get(item) == null) {
                if (mTextWatchers.get((Integer) editText.getTag()) != null) {
                    mTextWatchers.get((Integer) editText.getTag()).unregisterEditText(editText);
                }

                editText.setTag(item);
                mTextWatchers.put(item, new EditTextWatcher(editText));
            }
            else {
                if (mTextWatchers.get((Integer) editText.getTag()) != null) {
                    mTextWatchers.get((Integer) editText.getTag()).unregisterEditText(editText);
                }

                editText.setTag(item);
                mTextWatchers.get(item).registerEditText(editText);
            }

            switch (item) {
                case COUNTING_ADULT_MALE:
                    textView.setText(R.string.counting_adult_male);
                    imageView.setImageResource(R.drawable.ic_male_symbol);
                    editText.setText(Integer.toString(getValues().get(COUNTING_ADULT_MALE)));
                    break;
                case COUNTING_ADULT_FEMALE:
                    textView.setText(R.string.counting_adult_female);
                    imageView.setImageResource(R.drawable.ic_female_symbol);
                    editText.setText(Integer.toString(getValues().get(COUNTING_ADULT_FEMALE)));
                    break;
                case COUNTING_ADULT_UNDETERMINED:
                    textView.setText(R.string.counting_adult_undetermined);
                    imageView.setImageResource(R.drawable.ic_unspecified_symbol);
                    editText.setText(Integer.toString(getValues().get(COUNTING_ADULT_UNDETERMINED)));
                    break;
                case COUNTING_NOT_ADULT:
                    textView.setText(R.string.counting_not_adult);

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    imageView.setLayoutParams(params);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    editText.setText(Integer.toString(getValues().get(COUNTING_NOT_ADULT)));

                    break;
                case COUNTING_YOUNG:
                    textView.setText(R.string.counting_young);

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    imageView.setLayoutParams(params);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    editText.setText(Integer.toString(getValues().get(COUNTING_YOUNG)));

                    break;
                case COUNTING_YEARLING:
                    textView.setText(R.string.counting_yearling);

                    params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                    imageView.setLayoutParams(params);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);

                    editText.setText(Integer.toString(getValues().get(COUNTING_YEARLING)));

                    break;
                case COUNTING_UNDETERMINED:
                    textView.setText(R.string.counting_undetermined);
                    imageView.setImageResource(R.drawable.ic_male_female_symbol);
                    editText.setText(Integer.toString(getValues().get(COUNTING_UNDETERMINED)));
                    break;
            }

            return view;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public void onClick(View v) {
            int position = getListView().getPositionForView((View) v.getParent());
            EditText editText = (EditText) ((View) v.getParent()).findViewById(R.id.editTextCounting);

            if (v.getId() == R.id.buttonCountingMinus) {
                updateEditText(editText, position, -1);
            }
            else if (v.getId() == R.id.buttonCountingPlus) {
                updateEditText(editText, position, 1);
            }
            else if (v.getId() == R.id.editTextCounting) {
                editText.setCursorVisible(true);
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
            }
        }

        private void updateEditText(final EditText editText, final int position, final int value) {
            editText.setText(Integer.toString(((getValues().get(getItem(position)) + value) < 0) ? 0 : (getValues().get(getItem(position)) + value)));
        }
    }

    private class EditTextWatcher implements TextWatcher {
        private EditText mEditText = null;
        private Button mButtonCountingMinus;

        public EditTextWatcher(EditText pEditText) {
            registerEditText(pEditText);
        }

        public void registerEditText(EditText pEditText) {
            unregisterEditText(mEditText);

            mEditText = pEditText;
            mButtonCountingMinus = (Button) ((View) mEditText.getParent()).findViewById(R.id.buttonCountingMinus);
            mEditText.addTextChangedListener(this);
        }

        public void unregisterEditText(EditText pEditText) {
            if (pEditText != null) {
                pEditText.removeTextChangedListener(this);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Integer oldValue = getValues().get((Integer) mEditText.getTag());

            Log.d(AbstractCountingListFragment.class.getName(), "afterTextChanged " + mEditText.getTag() + " " + oldValue);

            if (mEditText.getEditableText().length() > 0) {
                int value = Integer.valueOf(mEditText.getEditableText().toString());

                mButtonCountingMinus.setEnabled(value > 0);

                if (oldValue != value) {
                    getValues().put((Integer) mEditText.getTag(), value);
                    updateAndNotify((Integer) mEditText.getTag());
                }
            }
            else {
                mButtonCountingMinus.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // nothing to do ...
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // nothing to do ...
        }
    }
}
