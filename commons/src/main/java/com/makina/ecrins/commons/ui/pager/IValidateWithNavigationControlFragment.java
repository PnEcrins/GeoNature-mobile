package com.makina.ecrins.commons.ui.pager;

/**
 * Adding navigation control to {@link IValidateFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IValidateWithNavigationControlFragment extends IValidateFragment {

    /**
     * Enables or not the paging control to go to the previous page
     *
     * @return <code>true</code> if the paging control is enabled or not for going to the previous page.
     */
    boolean getPagingToPreviousEnabled();

    /**
     * Enables or not the paging control to go to the next page
     *
     * @return <code>true</code> if the paging control is enabled or not for going to the next page.
     */
    boolean getPagingToForwardEnabled();
}
