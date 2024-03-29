package com.makina.ecrins.commons.ui.input;

import com.makina.ecrins.commons.input.AbstractInput;

/**
 * Callback used by all {@code Fragment}s using {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface OnInputFragmentListener {

    /**
     * Called when the input should be saved.
     */
    void onSaveInput();

    /**
     * Notify the application to close itself.
     */
    void onCloseApplication();
}
