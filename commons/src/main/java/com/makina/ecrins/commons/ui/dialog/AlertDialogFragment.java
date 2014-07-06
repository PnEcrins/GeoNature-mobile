package com.makina.ecrins.commons.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.makina.ecrins.commons.R;

/**
 * Basic implementation of a <code>AlertDialog</code> as <code>DialogFragment</code>.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class AlertDialogFragment extends DialogFragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_MSG_POSITIVE = "message_positive";
    private static final String KEY_MSG_NEGATIVE = "message_negative";

    private static OnClickListener sOnPositiveClickListener;
    private static OnClickListener sOnNegativeClickListener;

    public static AlertDialogFragment newInstance(int title, int message, OnClickListener pOnPositiveClickListener, OnClickListener pOnNegativeClickListener) {
        Log.d(AlertDialogFragment.class.getName(), "newInstance");

        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE, message);
        args.putInt(KEY_MSG_POSITIVE, R.string.alert_dialog_ok);
        args.putInt(KEY_MSG_NEGATIVE, R.string.alert_dialog_cancel);
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(false);

        sOnPositiveClickListener = pOnPositiveClickListener;
        sOnNegativeClickListener = pOnNegativeClickListener;

        return dialogFragment;
    }

    public static AlertDialogFragment newInstance(int title, int message, int msgpositive, int msgnegative, OnClickListener pOnPositiveClickListener, OnClickListener pOnNegativeClickListener) {
        Log.d(AlertDialogFragment.class.getName(), "newInstance");

        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE, message);
        args.putInt(KEY_MSG_POSITIVE, msgpositive);
        args.putInt(KEY_MSG_NEGATIVE, msgnegative);
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(false);

        sOnPositiveClickListener = pOnPositiveClickListener;
        sOnNegativeClickListener = pOnNegativeClickListener;

        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_action_alert)
                .setTitle(getArguments().getInt(KEY_TITLE))
                .setMessage(getArguments().getInt(KEY_MESSAGE))
                .setPositiveButton(getArguments().getInt(KEY_MSG_POSITIVE), sOnPositiveClickListener)
                .setNegativeButton(getArguments().getInt(KEY_MSG_NEGATIVE), sOnNegativeClickListener)
                .create();
    }
}
