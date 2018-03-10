package com.makina.ecrins.commons.ui.pager;

/**
 * {@code Fragment} with a validation control step.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IValidateFragment {

    /**
     * Returns the view title.
     * <p>
     * <strong>Note: </strong> Must be a constant value (e.g. R.string.my_value).
     * </p>
     *
     * @return view title as resource ID
     */
    int getResourceTitle();

    /**
     * Enables or not the paging control
     *
     * @return <code>true</code> if the paging control is enabled.
     */
    boolean getPagingEnabled();

    /**
     * Validate the current view
     *
     * @return <code>true</code> if this view is validated, <code>false</code> otherwise
     */
    boolean validate();

    /**
     * Updates the current view
     */
    void refreshView();
}
