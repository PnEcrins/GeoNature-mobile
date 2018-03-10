package com.makina.ecrins.commons.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.util.KeyboardUtils;

/**
 * Custom {@code Dialog} used to add or edit a comment.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CommentDialogFragment
        extends DialogFragment {

    private static final String KEY_MESSAGE = "message";

    private OnCommentDialogValidateListener mOnCommentDialogValidateListener;

    public static CommentDialogFragment newInstance(@Nullable String message) {

        if (BuildConfig.DEBUG) {
            Log.d(CommentDialogFragment.class.getName(),
                  "newInstance");
        }

        final CommentDialogFragment dialogFragment = new CommentDialogFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_MESSAGE,
                       message);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    public void setOnCommentDialogValidateListener(OnCommentDialogValidateListener pOnCommentDialogValidateListener) {

        this.mOnCommentDialogValidateListener = pOnCommentDialogValidateListener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments() == null ? new Bundle() : getArguments();
        final Context context = getContext();

        if (context == null) {
            throw new IllegalArgumentException("Null Context while creating " + CommentDialogFragment.class.getName());
        }

        final View view = View.inflate(context,
                                       R.layout.dialog_comment,
                                       null);

        final EditText textViewComment = view.findViewById(R.id.editTextComment);

        int alertDialogTitleResource = R.string.alert_dialog_add_comment_title;


        if (arguments.containsKey(KEY_MESSAGE) && (!TextUtils.isEmpty(arguments.getString(KEY_MESSAGE)))) {
            textViewComment.setText(arguments.getString(KEY_MESSAGE));
            alertDialogTitleResource = R.string.alert_dialog_edit_comment_title;
        }

        // show automatically the soft keyboard for the EditText
        textViewComment.requestFocus();
        textViewComment.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            KeyboardUtils.showSoftKeyboard(textViewComment);
                                        }
                                    },
                                    250);

        return new AlertDialog.Builder(context,
                                       R.style.CommonsDialogStyle).setTitle(alertDialogTitleResource)
                                                                  .setView(view)
                                                                  .setPositiveButton(R.string.alert_dialog_ok,
                                                                                     new OnClickListener() {
                                                                                         @Override
                                                                                         public void onClick(
                                                                                                 DialogInterface dialog,
                                                                                                 int which) {
                                                                                             KeyboardUtils.hideSoftKeyboard(textViewComment);

                                                                                             if (mOnCommentDialogValidateListener != null) {
                                                                                                 mOnCommentDialogValidateListener.onPositiveButtonClick(dialog,
                                                                                                                                                        textViewComment.getText()
                                                                                                                                                                       .toString());
                                                                                             }
                                                                                         }
                                                                                     })
                                                                  .setNegativeButton(R.string.alert_dialog_cancel,
                                                                                     new OnClickListener() {
                                                                                         @Override
                                                                                         public void onClick(
                                                                                                 DialogInterface dialog,
                                                                                                 int which) {
                                                                                             KeyboardUtils.hideSoftKeyboard(textViewComment);

                                                                                             if (mOnCommentDialogValidateListener != null) {
                                                                                                 mOnCommentDialogValidateListener.onNegativeButtonClick(dialog);
                                                                                             }
                                                                                         }
                                                                                     })
                                                                  .create();
    }

    /**
     * The callback used by {@link com.makina.ecrins.commons.ui.dialog.CommentDialogFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     * @see android.content.DialogInterface.OnClickListener
     */
    public interface OnCommentDialogValidateListener {

        /**
         * Invoked when the positive button of the dialog is pressed.
         *
         * @param dialog  the dialog that received the click
         * @param message the string message edited from this dialog
         */
        void onPositiveButtonClick(
                DialogInterface dialog,
                String message);

        /**
         * Invoked when the negative button of the dialog is pressed.
         *
         * @param dialog the dialog that received the click
         */
        void onNegativeButtonClick(DialogInterface dialog);
    }
}
