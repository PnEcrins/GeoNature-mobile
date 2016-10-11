package com.makina.ecrins.commons.ui.input;

import android.support.annotation.NonNull;

import com.makina.ecrins.commons.input.AbstractInput;

/**
 * Callback used by all {@code Fragment}s using {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface OnInputFragmentListener {

    /**
     * Gets the current {@link AbstractInput} to update.
     *
     * @return the current {@link AbstractInput}
     */
    @NonNull
    AbstractInput getInput();
}
