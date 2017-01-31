package com.makina.ecrins.commons.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

/**
 * Base {@code Activity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractBaseActivity
        extends AppCompatActivity {

    private OnBackPressedListener mOnBackPressedListener;
    private OnDispatchKeyEventListener mOnDispatchKeyEventListener;

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener != null) {
            if (!mOnBackPressedListener.onBackPressed()) {
                super.onBackPressed();
            }
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mOnDispatchKeyEventListener != null) {
            final boolean dispatchKeyEvent = mOnDispatchKeyEventListener.dispatchKeyEvent(event);

            // noinspection SimplifiableIfStatement
            if (dispatchKeyEvent) {
                return true;
            }

            return super.dispatchKeyEvent(event);
        }
        else {
            return super.dispatchKeyEvent(event);
        }
    }

    public void setOnBackPressedListener(@Nullable final OnBackPressedListener pOnBackPressedListener) {
        this.mOnBackPressedListener = pOnBackPressedListener;
    }

    public void setOnDispatchKeyEventListener(@Nullable final OnDispatchKeyEventListener pOnDispatchKeyEventListener) {
        this.mOnDispatchKeyEventListener = pOnDispatchKeyEventListener;
    }

    /**
     * Callback about {@link #onBackPressed()}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    /**
     * Callback about {@link #dispatchKeyEvent(KeyEvent)}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnDispatchKeyEventListener {
        boolean dispatchKeyEvent(KeyEvent event);
    }
}
