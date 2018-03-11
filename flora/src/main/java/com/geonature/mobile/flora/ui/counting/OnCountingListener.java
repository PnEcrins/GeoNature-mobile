package com.geonature.mobile.flora.ui.counting;

import android.support.annotation.NonNull;

import com.geonature.mobile.flora.input.Counting;

/**
 * Callback about {@link Counting}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface OnCountingListener {

    void OnCountingUpdated(@NonNull final Counting counting,
                           boolean enableFinish);
}
