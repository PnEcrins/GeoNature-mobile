package com.makina.ecrins.commons.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom {@code Dialog} as {@code DialogFragment} used to select a given action from a
 * {@code ListView}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ChooseActionDialogFragment extends DialogFragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ACTIONS = "actions";

    private StringResourcesArrayAdapter mAdapter;
    private OnChooseActionDialogListener mOnChooseActionDialogListener;

    public static ChooseActionDialogFragment newInstance(
            int titleResourceId,
            final List<Integer> actions) {
        return newInstance(
                titleResourceId,
                0,
                actions
        );
    }

    public static ChooseActionDialogFragment newInstance(
            int titleResourceId,
            int messageResourceId,
            final List<Integer> actions) {

        if (BuildConfig.DEBUG) {
            Log.d(
                    ChooseActionDialogFragment.class.getName(),
                    "newInstance"
            );
        }

        final ChooseActionDialogFragment dialogFragment = new ChooseActionDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(
                KEY_TITLE,
                titleResourceId
        );
        args.putInt(
                KEY_MESSAGE,
                messageResourceId
        );
        args.putIntegerArrayList(
                KEY_ACTIONS,
                new ArrayList<>(actions)
        );
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(true);

        return dialogFragment;
    }

    public void setOnChooseActionDialogListener(OnChooseActionDialogListener pOnChooseActionDialogListener) {
        this.mOnChooseActionDialogListener = pOnChooseActionDialogListener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = View.inflate(
                getActivity(),
                R.layout.dialog_list_items,
                null
        );

        TextView textView = (TextView) view.findViewById(R.id.textViewMessageDialog);
        int message = getArguments().getInt(KEY_MESSAGE);

        if (message == 0) {
            textView.setVisibility(View.GONE);
        }
        else {
            textView.setText(message);
        }

        final ListView listView = (ListView) view.findViewById(android.R.id.list);

        mAdapter = new StringResourcesArrayAdapter(getActivity());

        final List<Integer> actions = getArguments().getIntegerArrayList(KEY_ACTIONS);

        if (actions != null) {
            for (Integer action : actions) {
                mAdapter.add(action);
            }
        }

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        if (mOnChooseActionDialogListener != null) {
                            mOnChooseActionDialogListener.onItemClick(
                                    getDialog(),
                                    position,
                                    mAdapter.getItem(position)
                            );
                        }
                    }
                }
        );

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_action_choose)
                .setTitle(getArguments().getInt(KEY_TITLE))
                .setView(view)
                .setNegativeButton(R.string.alert_dialog_cancel, null)
                .create();
    }

    /**
     * Simple {@code Adapter} using String resources.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private class StringResourcesArrayAdapter
            extends ArrayAdapter<Integer> {

        private int mTextViewResourceId;
        private final LayoutInflater mInflater;

        public StringResourcesArrayAdapter(Context context) {
            this(
                    context,
                    android.R.layout.simple_list_item_1
            );
        }

        public StringResourcesArrayAdapter(
                Context context,
                int textViewResourceId) {
            super(context, textViewResourceId);

            mTextViewResourceId = textViewResourceId;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(mTextViewResourceId, parent, false);
            }
            else {
                view = convertView;
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(position));

            return view;
        }
    }

    /**
     * The callback used by {@link com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static interface OnChooseActionDialogListener {

        /**
         * Invoked when an item in this {@code ListView} has been clicked.
         */
        void onItemClick(
                DialogInterface dialog,
                int position,
                int actionResourceId);
    }
}
