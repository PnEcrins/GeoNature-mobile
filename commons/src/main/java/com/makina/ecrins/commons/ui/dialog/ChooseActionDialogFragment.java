package com.makina.ecrins.commons.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.makina.ecrins.commons.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom <code>Dialog</code> used to select a given action from a <code>ListView</code>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ChooseActionDialogFragment extends DialogFragment implements OnItemClickListener {

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ACTIONS = "actions";

    private IntResourcesArrayAdapter mAdapter;
    private Handler mHandler;

    public static ChooseActionDialogFragment newInstance(int title, List<Integer> actions) {
        Log.d(ChooseActionDialogFragment.class.getName(), "newInstance");

        ChooseActionDialogFragment dialogFragment = new ChooseActionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putIntegerArrayList(KEY_ACTIONS, (ArrayList<Integer>) actions);
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(true);

        return dialogFragment;
    }

    public static ChooseActionDialogFragment newInstance(int title, int message, List<Integer> actions) {
        Log.d(ChooseActionDialogFragment.class.getName(), "newInstance");

        ChooseActionDialogFragment dialogFragment = new ChooseActionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE, message);
        args.putIntegerArrayList(KEY_ACTIONS, (ArrayList<Integer>) actions);
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(true);

        return dialogFragment;
    }

    public void setHandler(Handler pHandler) {
        this.mHandler = pHandler;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_list_items, null);
        TextView textView = (TextView) view.findViewById(R.id.textViewMessageDialog);
        int message = getArguments().getInt(KEY_MESSAGE, -1);

        if (message == -1) {
            textView.setVisibility(View.GONE);
        }
        else {
            textView.setText(message);
        }

        ListView listView = (ListView) view.findViewById(android.R.id.list);

        mAdapter = new IntResourcesArrayAdapter(getActivity());

        for (Integer action : getArguments().getIntegerArrayList(KEY_ACTIONS)) {
            mAdapter.add(action);
        }

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_action_choose)
                .setTitle(getArguments().getInt(KEY_TITLE))
                .setView(view)
                .setNegativeButton(R.string.alert_dialog_cancel, null)
                .create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Message message = mHandler.obtainMessage(mAdapter.getItem(position));
        message.sendToTarget();
        dismiss();
    }

    private class IntResourcesArrayAdapter extends ArrayAdapter<Integer> {
        private int mTextViewResourceId;
        private final LayoutInflater mInflater;

        public IntResourcesArrayAdapter(Context context) {
            this(context, android.R.layout.simple_list_item_1);
        }

        public IntResourcesArrayAdapter(Context context, int textViewResourceId) {
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
}
