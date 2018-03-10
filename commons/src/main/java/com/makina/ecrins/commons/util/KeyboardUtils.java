package com.makina.ecrins.commons.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility methods for manipulating the onscreen keyboard.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class KeyboardUtils {

    /**
     * {@link KeyboardUtils} instances should NOT be constructed in standard programming.
     */
    private KeyboardUtils() {
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(@Nullable final Context context) {
        if (context == null) {
            return;
        }

        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN,
                                0);
        }
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(@NonNull final View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(@NonNull final View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }
}
