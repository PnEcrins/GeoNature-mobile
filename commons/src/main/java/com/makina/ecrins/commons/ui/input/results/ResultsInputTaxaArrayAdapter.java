package com.makina.ecrins.commons.ui.input.results;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.input.AbstractTaxon;

/**
 * Custom <code>ArrayAdapter</code> used for {@link com.makina.ecrins.commons.ui.input.results.AbstractResultsInputFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ResultsInputTaxaArrayAdapter extends ArrayAdapter<AbstractTaxon> {

    private final LayoutInflater mInflater;
    private final int mTextViewResourceId;
    private OnDisplaySelectedTaxonListener mOnDisplaySelectedTaxonListener = null;

    private long mSelectedTaxonId;

    public ResultsInputTaxaArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        mTextViewResourceId = textViewResourceId;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(mTextViewResourceId, parent, false);

            if (mOnDisplaySelectedTaxonListener != null) {
                mInflater.inflate(mOnDisplaySelectedTaxonListener.getViewResourceId(), (ViewGroup) view.findViewById(R.id.layoutTaxonDetails));
            }
        }
        else {
            view = convertView;
        }

        ((TextView) view.findViewById(R.id.textViewTaxonName)).setText(getItem(position).getNameEntered());

        if (getItem(position).getId() == getSelectedTaxonId()) {
            ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(true);
            displaySelectedTaxonDetails(getItem(position), view.findViewById(R.id.layoutTaxonDetails), true);
        }
        else {
            ((RadioButton) view.findViewById(R.id.radioButton)).setChecked(false);
            displaySelectedTaxonDetails(getItem(position), view.findViewById(R.id.layoutTaxonDetails), false);
        }

        if (getItem(position).getComment().isEmpty()) {
            ((ImageView) view.findViewById(R.id.imageViewTaxonComment)).setImageResource(R.drawable.ic_action_comment_add);
            ((ImageView) view.findViewById(R.id.imageViewTaxonComment)).setContentDescription(getContext().getString(R.string.action_comment_add));
        }
        else {
            ((ImageView) view.findViewById(R.id.imageViewTaxonComment)).setImageResource(R.drawable.ic_action_comment);
            ((ImageView) view.findViewById(R.id.imageViewTaxonComment)).setContentDescription(getContext().getString(R.string.action_comment_edit));
        }

        // sets the background color for the current selected taxon
        if (getSelectedTaxonId() == getItem(position).getId()) {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.holo_blue_light));
        }
        else {
            view.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
        }

        return view;
    }

    public void setOnDisplaySelectedTaxonListener(OnDisplaySelectedTaxonListener pOnDisplaySelectedTaxonListener) {
        this.mOnDisplaySelectedTaxonListener = pOnDisplaySelectedTaxonListener;
    }

    public long getSelectedTaxonId() {
        return mSelectedTaxonId;
    }

    public void setSelectedTaxonId(long pSelectedTaxonId) {
        this.mSelectedTaxonId = pSelectedTaxonId;
    }

    private void displaySelectedTaxonDetails(AbstractTaxon selectedTaxon, View parentView, boolean showDetails) {
        if (mOnDisplaySelectedTaxonListener != null) {
            if (showDetails) {
                parentView.setVisibility(View.VISIBLE);
            }
            else {
                parentView.setVisibility(View.GONE);
            }

            mOnDisplaySelectedTaxonListener.displayDetails(selectedTaxon, parentView);
        }
    }

    public interface OnDisplaySelectedTaxonListener {
        int getViewResourceId();

        void displayDetails(AbstractTaxon selectedTaxon, View parentView);
    }
}
