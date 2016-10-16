package com.makina.ecrins.commons.ui.pager;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.makina.ecrins.commons.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * {@code ViewPager} implementation as {@code FragmentActivity} with navigation history support.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractNavigationHistoryPagerFragmentActivity
        extends AbstractPagerFragmentActivity {

    private static final String TAG = AbstractNavigationHistoryPagerFragmentActivity.class.getName();

    private static final String KEY_NAVIGATION_HISTORY = "navigation_history";
    private static final String KEY_HISTORY_PREVIOUS = "history_previous";
    private static final String KEY_SCROLL_STATE = "scroll_state";
    private static final String KEY_POSITION_OFFSET = "position_offset";

    private final Deque<Integer> mHistory = new ArrayDeque<>();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSavedState.putIntegerArrayList(KEY_NAVIGATION_HISTORY,
                                            new ArrayList<>(mHistory));
            mSavedState.putBoolean(KEY_HISTORY_PREVIOUS,
                                   false);
        }
        else {
            final ArrayList<Integer> navigationHistoryList = savedInstanceState.getIntegerArrayList(KEY_NAVIGATION_HISTORY);

            if (navigationHistoryList != null) {
                mHistory.addAll(navigationHistoryList);
            }
        }

        mPreviousButton.setEnabled(!mHistory.isEmpty());
        mPreviousButton.setVisibility((!mHistory.isEmpty()) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSavedState.putIntegerArrayList(KEY_NAVIGATION_HISTORY,
                                        new ArrayList<>(mHistory));

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.previousButton) {
            goToPreviousPage();
        }
        else if (v.getId() == R.id.nextButton) {
            if (mPager.getCurrentItem() < (mAdapter.getCount() - 1)) {
                mSavedState.putBoolean(KEY_HISTORY_PREVIOUS,
                                       false);
                mPager.setCurrentItem(mPager.getCurrentItem() + 1,
                                      true);
            }
            else if (mPager.getCurrentItem() == (mAdapter.getCount() - 1)) {
                // the last page
                performFinishAction();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // only if the pager is currently being dragged by the user
        if (state != ViewPager.SCROLL_STATE_SETTLING) {
            mSavedState.putInt(KEY_SCROLL_STATE,
                               state);
        }
    }

    @Override
    public void onPageScrolled(int position,
                               float positionOffset,
                               int positionOffsetPixels) {
        if ((positionOffset > 0.0f) && (mSavedState.getInt(KEY_SCROLL_STATE,
                                                           ViewPager.SCROLL_STATE_IDLE) == ViewPager.SCROLL_STATE_DRAGGING)) {
            mSavedState.putBoolean(KEY_HISTORY_PREVIOUS,
                                   mSavedState.getFloat(KEY_POSITION_OFFSET,
                                                        0.0f) > positionOffset);
            mSavedState.putFloat(KEY_POSITION_OFFSET,
                                 positionOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG,
              "onPageSelected, position: " + position + ", previous: " + mSavedState.getBoolean(KEY_HISTORY_PREVIOUS));

        // sets default paging control
        mPager.setPagingEnabled(true);

        if (mSavedState.getBoolean(KEY_HISTORY_PREVIOUS)) {
            if (mHistory.isEmpty()) {
                mSavedState.putBoolean(KEY_HISTORY_PREVIOUS,
                                       false);
            }
            else {
                // go back in the navigation history
                final IValidateFragment fragment = getPageFragment(position);

                if ((fragment != null) && (fragment.getResourceTitle() == mHistory.getLast())) {
                    mHistory.pollLast();
                }
                else {
                    goToPageByKey(mHistory.getLast());

                    return;
                }
            }
        }
        else {
            final IValidateFragment fragment = getPageFragment(mSavedState.getInt(KEY_PAGER_POSITION));

            if (fragment != null) {
                mHistory.addLast(fragment.getResourceTitle());
            }

            // checks validation before switching to the next page
            final IValidateFragment getLastFragmentInHistory = getPageFragmentByKey(mHistory.getLast());

            if ((position > 0) && !((getLastFragmentInHistory == null) || getLastFragmentInHistory.validate())) {
                goToPreviousPage();
                return;
            }
        }

        mSavedState.putBoolean(KEY_HISTORY_PREVIOUS,
                               false);

        Log.d(TAG,
              "onPageSelected, position: " + position + ", history: " + mHistory.toString());

        IValidateFragment fragment = getPageFragment(position);

        // updates title
        setTitle(mAdapter.getPageTitle(mPager.getCurrentItem()));

        // refreshes the current view if needed
        if (fragment != null) {
            fragment.refreshView();

            // disable or enable paging control for the current instance of IValidateFragment
            mPager.setPagingEnabled(fragment.getPagingEnabled());
        }

        // updates navigation buttons statuses
        if ((fragment != null) && (fragment instanceof IValidateWithNavigationControlFragment)) {
            // disable or enable paging control for the current instance of IValidateWithNavigationControlFragment
            mPager.setPagingLeftEnabled(((IValidateWithNavigationControlFragment) fragment).getPagingToPreviousEnabled());
            mPager.setPagingRightEnabled(((IValidateWithNavigationControlFragment) fragment).getPagingToForwardEnabled());

            if (((IValidateWithNavigationControlFragment) fragment).getPagingToPreviousEnabled()) {
                mPreviousButton.setEnabled(mPager.getCurrentItem() > 0);
                mPreviousButton.setVisibility((mPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);
            }
            else {
                mPreviousButton.setVisibility(View.INVISIBLE);
            }

            if (((IValidateWithNavigationControlFragment) fragment).getPagingToForwardEnabled()) {
                mNextButton.setEnabled(fragment.validate());
                mNextButton.setVisibility(View.VISIBLE);
            }
            else {
                mNextButton.setVisibility(View.INVISIBLE);
            }
        }
        else {
            mPreviousButton.setEnabled(mPager.getCurrentItem() > 0);
            mPreviousButton.setVisibility((mPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);

            mNextButton.setText((mPager.getCurrentItem() < (mAdapter.getCount() - 1)) ? R.string.button_pager_next : R.string.button_pager_finish);
            mNextButton.setEnabled((fragment == null) || fragment.validate());
            mNextButton.setVisibility(View.VISIBLE);
        }

        mSavedState.putInt(KEY_PAGER_POSITION,
                           mPager.getCurrentItem());
    }

    @Override
    public void goToPreviousPage() {
        if ((mPager.getCurrentItem() > 0) && (!mHistory.isEmpty())) {
            mSavedState.putBoolean(KEY_HISTORY_PREVIOUS,
                                   true);
            goToPageByKey(mHistory.getLast());
        }
    }

    /**
     * Go to the last given page in history.
     *
     * @param key the page key
     */
    public void goBackInHistory(int key) {
        while ((!mHistory.isEmpty()) && (key != mHistory.getLast())) {
            mHistory.pollLast();
        }

        goToPreviousPage();
    }

    /**
     * Gets the number of pages for a given key saved in history.
     *
     * @param key the page key
     *
     * @return the number of pages in history for a given page key
     *
     * @see IValidateFragment#getResourceTitle()
     */
    public int countPagesInHistory(int key) {
        int count = 0;

        for (Integer pageKey : mHistory) {
            if (pageKey == key) {
                count++;
            }
        }

        return count;
    }
}
