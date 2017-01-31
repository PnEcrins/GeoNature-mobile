package com.makina.ecrins.flora.ui.frequencies;

import android.support.annotation.NonNull;

import com.makina.ecrins.flora.input.Frequency;

/**
 * Callback about {@link Frequency}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface OnFrequencyListener {

    void OnFrequencyUpdated(@NonNull final Frequency frequency);
}
