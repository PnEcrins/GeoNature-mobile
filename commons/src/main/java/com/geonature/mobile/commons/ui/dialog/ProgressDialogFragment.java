package com.geonature.mobile.commons.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geonature.mobile.commons.R;

import java.text.NumberFormat;

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

    private ProgressBar mProgressBar;
    private TextView mTextViewProgressPercent;
    private TextView mTextViewProgressNumber;

    private String mProgressNumberFormat;
    private NumberFormat mProgressPercentFormat;

    public static ProgressDialogFragment newInstance(
            int title,
            int message,
            int progressStyle,
            int max) {

        final ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        final Bundle args = new Bundle();
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

    public ProgressDialogFragment() {
        initFormats();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments() == null ? new Bundle() : getArguments();
        final Context context = getContext();

        if (context == null) {
            throw new IllegalArgumentException("Null Context while creating " + ProgressDialogFragment.class.getName());
        }

        final View view = View.inflate(getContext(),
                                       (arguments.getInt(KEY_PROGRESS_STYLE) == ProgressDialog.STYLE_SPINNER) ? R.layout.dialog_progress_indeterminate : R.layout.dialog_progress,
                                       null);

        mProgressBar = view.findViewById(android.R.id.progress);
        mProgressBar.setMax(arguments.getInt(KEY_MAX));

        final int messageResourceId = arguments.getInt(KEY_MESSAGE);

        if (arguments.getInt(KEY_PROGRESS_STYLE) == ProgressDialog.STYLE_SPINNER) {
            final TextView mTextViewMessage = view.findViewById(android.R.id.message);

            if (messageResourceId == 0) {
                mTextViewMessage.setVisibility(View.GONE);
            }
            else {
                mTextViewMessage.setText(messageResourceId);
            }
        }
        else {
            mTextViewProgressPercent = view.findViewById(R.id.textViewProgressPercent);
            mTextViewProgressNumber = view.findViewById(R.id.textViewProgressNumber);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),
                                                                    R.style.CommonsDialogStyle).setTitle(getArguments().getInt(KEY_TITLE))
                                                                                               .setView(view)
                                                                                               .setCancelable(false);

        if ((getArguments().getInt(KEY_PROGRESS_STYLE) == ProgressDialog.STYLE_HORIZONTAL) && (messageResourceId != 0)) {
            builder.setMessage(messageResourceId);
        }

        return builder.create();
    }

    public void setProgress(int progress) {
        final Bundle arguments = getArguments() == null ? new Bundle() : getArguments();

        if (mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }

        if (mTextViewProgressPercent != null) {
            double percent = (double) progress / (double) arguments.getInt(KEY_MAX);
            SpannableString spannableString = new SpannableString(mProgressPercentFormat.format(percent));
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                                    0,
                                    spannableString.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextViewProgressPercent.setText(spannableString);
        }

        if (mTextViewProgressNumber != null) {
            mTextViewProgressNumber.setText(String.format(mProgressNumberFormat,
                                                          progress,
                                                          arguments.getInt(KEY_MAX)));
        }
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }
}