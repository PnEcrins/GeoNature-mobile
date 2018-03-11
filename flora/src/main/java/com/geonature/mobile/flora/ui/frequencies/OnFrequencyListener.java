package com.geonature.mobile.flora.ui.frequencies;

import android.support.annotation.NonNull;

import com.geonature.mobile.flora.input.Frequency;

/**
 * Callback about {@link Frequency}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface OnFrequencyListener {

    void OnFrequencyUpdated(@NonNull final Frequency frequency);
}
