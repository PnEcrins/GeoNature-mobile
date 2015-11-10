package com.makina.ecrins.commons.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Basic implementation of a {@code ProgressDialog} as {@code DialogFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ProgressDialogFragment
        extends DialogFragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_PROGRESS_STYLE = "progress_style";
    private static final String KEY_MAX = "max";

    private ProgressDialog mProgressDialog;

    public static ProgressDialogFragment newInstance(
            int title,
            int message,
            int progressStyle,
            int max) {

        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE,
                    title);
        args.putInt(KEY_MESSAGE,
                    message);
        args.putInt(KEY_PROGRESS_STYLE,
                    progressStyle);
        args.putInt(KEY_MAX,
                    max);
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(false);

        return dialogFragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getText(getArguments().getInt(KEY_TITLE)));
        mProgressDialog.setMessage(getText(getArguments().getInt(KEY_MESSAGE)));
        mProgressDialog.setProgressStyle(getArguments().getInt(KEY_PROGRESS_STYLE));
        mProgressDialog.setMax(getArguments().getInt(KEY_MAX));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        return mProgressDialog;
    }

    public void setProgress(int progress) {

        if (mProgressDialog != null) {
            mProgressDialog.setProgress(progress);
        }
    }
}