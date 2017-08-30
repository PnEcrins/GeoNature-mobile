package com.makina.ecrins.commons.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.makina.ecrins.commons.R;

/**
 * Basic implementation of a alert {@code Dialog} as {@code DialogFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class AlertDialogFragment
        extends DialogFragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_BUTTON_POSITIVE = "buttonPositive";
    private static final String KEY_BUTTON_NEGATIVE = "buttonNegative";

    private OnAlertDialogListener mOnAlertDialogListener;

    private final DialogInterface.OnClickListener mOnPositiveOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog,
                            int which) {
            if (mOnAlertDialogListener != null) {
                mOnAlertDialogListener.onPositiveButtonClick(dialog);
            }
        }
    };

    private final DialogInterface.OnClickListener mOnNegativeOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog,
                            int which) {
            if (mOnAlertDialogListener != null) {
                mOnAlertDialogListener.onNegativeButtonClick(dialog);
            }
        }
    };

    public static AlertDialogFragment newInstance(int title,
                                                  int message) {
        return newInstance(title,
                           message,
                           R.string.alert_dialog_ok,
                           R.string.alert_dialog_cancel);
    }

    public static AlertDialogFragment newInstance(int title,
                                                  int message,
                                                  int buttonPositiveResourceId,
                                                  int buttonNegativeResourceId) {
        return newInstance(title,
                           message,
                           buttonPositiveResourceId,
                           buttonNegativeResourceId,
                           false);
    }

    public static AlertDialogFragment newInstance(int title,
                                                  int message,
                                                  int buttonPositiveResourceId,
                                                  int buttonNegativeResourceId,
                                                  boolean cancelable) {
        final AlertDialogFragment dialogFragment = new AlertDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_TITLE,
                    title);
        args.putInt(KEY_MESSAGE,
                    message);
        args.putInt(KEY_BUTTON_POSITIVE,
                    buttonPositiveResourceId);
        args.putInt(KEY_BUTTON_NEGATIVE,
                    buttonNegativeResourceId);

        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(cancelable);

        return dialogFragment;
    }

    public void setOnAlertDialogListener(OnAlertDialogListener pOnAlertDialogListener) {

        this.mOnAlertDialogListener = pOnAlertDialogListener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity(),
                                       R.style.CommonsDialogStyle).setIcon(R.drawable.ic_action_alert)
                                                                  .setTitle(getArguments().getInt(KEY_TITLE))
                                                                  .setMessage(getArguments().getInt(KEY_MESSAGE))
                                                                  .setPositiveButton(getArguments().getInt(KEY_BUTTON_POSITIVE),
                                                                                     mOnPositiveOnClickListener)
                                                                  .setNegativeButton(getArguments().getInt(KEY_BUTTON_NEGATIVE),
                                                                                     mOnNegativeOnClickListener)
                                                                  .create();
    }

    /**
     * The callback used by {@link com.makina.ecrins.commons.ui.dialog.AlertDialogFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     * @see {@link android.content.DialogInterface.OnClickListener}
     */
    public interface OnAlertDialogListener {

        /**
         * Invoked when the positive button of the dialog is pressed.
         *
         * @param dialog the dialog that received the click
         */
        void onPositiveButtonClick(DialogInterface dialog);

        /**
         * Invoked when the negative button of the dialog is pressed.
         *
         * @param dialog the dialog that received the click
         */
        void onNegativeButtonClick(DialogInterface dialog);
    }
}
