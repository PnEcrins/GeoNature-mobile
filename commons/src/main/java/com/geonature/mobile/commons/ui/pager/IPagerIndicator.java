package com.geonature.mobile.commons.ui.pager;

import android.support.v4.view.ViewPager;

/**
 * A {@link IPagerIndicator} is responsible to show a visual indicator on the total views number and
 * the current visible view.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see <a href="https://github.com/JakeWharton/Android-ViewPagerIndicator">https://github.com/JakeWharton/Android-ViewPagerIndicator</a>
 */
public interface IPagerIndicator
        extends ViewPager.OnPageChangeListener {

    /**
     * Bind the indicator to a {@code ViewPager}.
     *
     * @param viewPager the {@code ViewPager} to bind
     */
    void setViewPager(ViewPager viewPager);

    /**
     * Bind the indicator to a {@code ViewPager}.
     *
     * @param viewPager       the {@code ViewPager} to bind
     * @param initialPosition the current position of the {@code ViewPager}
     */
    void setViewPager(ViewPager viewPager,
                      int initialPosition);

    /**
     * Set the current page of both the {@code ViewPager} and indicator.
     * <p>This <strong>must</strong> be used if you need to set the page before
     * the views are drawn on screen (e.g., default start page).</p>
     *
     * @param item the current item position of the {@code ViewPager}
     */
    void setCurrentItem(int item);

    /**
     * Notify the indicator that the {@code ViewPager} list has changed.
     */
    void notifyDataSetChanged();
}
