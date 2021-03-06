package com.makina.ecrins.commons.ui.input;

import android.support.annotation.NonNull;

import com.makina.ecrins.commons.input.AbstractInput;

/**
 * {@code Fragment} using {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface IInputFragment {

    /**
     * Sets the current {@link AbstractInput} to update.
     *
     * @param input the current {@link AbstractInput} to update
     */
    void setInput(@NonNull final AbstractInput input);
}
