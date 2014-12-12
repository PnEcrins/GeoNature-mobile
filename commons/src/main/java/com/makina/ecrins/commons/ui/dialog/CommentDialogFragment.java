package com.makina.ecrins.commons.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.makina.ecrins.commons.R;

/**
 * Custom <code>Dialog</code> used to add or edit a comment.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CommentDialogFragment extends DialogFragment {

    private static final String KEY_MESSAGE = "message";

    private static OnCommentDialogValidateListener sOnCommentDialogValidateListener;
    private static OnClickListener sOnNegativeClickListener;

    public static CommentDialogFragment newInstance(String message, OnCommentDialogValidateListener pOnCommentDialogValidateListener, OnClickListener pOnNegativeClickListener) {
        Log.d(CommentDialogFragment.class.getName(), "newInstance");

        CommentDialogFragment dialogFragment = new CommentDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        dialogFragment.setArguments(args);

        sOnCommentDialogValidateListener = pOnCommentDialogValidateListener;
        sOnNegativeClickListener = pOnNegativeClickListener;

        return dialogFragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_comment, null);

        final EditText textViewComment = (EditText) view.findViewById(R.id.editTextComment);

        int alertDialogTitleResource = R.string.alert_dialog_add_comment_title;

        if (getArguments().containsKey(KEY_MESSAGE) && (!getArguments().getString(KEY_MESSAGE).isEmpty())) {
            textViewComment.setText(getArguments().getString(KEY_MESSAGE));
            alertDialogTitleResource = R.string.alert_dialog_edit_comment_title;
        }

        // adding OnFocusChangeListener to this input text to display or hide soft keyboard
        textViewComment.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(textViewComment, 0);
                }
                else {
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textViewComment.getWindowToken(), 0);
                }
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(alertDialogTitleResource)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sOnCommentDialogValidateListener.onClick(dialog, which, textViewComment.getText().toString());
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, sOnNegativeClickListener)
                .create();
    }

    public interface OnCommentDialogValidateListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog  the dialog that received the click
         * @param which   the button that was clicked or the position of the item clicked
         * @param message the string message edited from thsi dialog
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
         */
        public void onClick(DialogInterface dialog, int which, String message);
    }
}
