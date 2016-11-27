package com.makina.ecrins.commons.ui.pager;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.makina.ecrins.commons.R;

/**
 * {@code ViewPager} implementation as {@code AppCompatActivity} with navigation history support.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractNavigationHistoryPagerFragmentActivity
        extends AbstractPagerFragmentActivity {

    private static final String TAG = AbstractNavigationHistoryPagerFragmentActivity.class.getName();

    private static final String KEY_HISTORY_PREVIOUS = "KEY_HISTORY_PREVIOUS";

    private int mScrollState;
    private float mPositionOffset;
    private boolean mHistoryPrevious = false;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mHistoryPrevious = (savedInstanceState != null) && savedInstanceState.getBoolean(KEY_HISTORY_PREVIOUS,
                                                                                         false);

        mPreviousButton.setEnabled(!mPager.getHistory()
                                          .isEmpty());
        mPreviousButton.setVisibility((!mPager.getHistory()
                                              .isEmpty()) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_HISTORY_PREVIOUS, mHistoryPrevious);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.previousButton) {
            goToPreviousPage();
        }
        else if (v.getId() == R.id.nextButton) {
            if (mViewPager.getCurrentItem() < (mAdapter.getCount() - 1)) {
                mHistoryPrevious = false;
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1,
                                          true);
            }
            else if (mViewPager.getCurrentItem() == (mAdapter.getCount() - 1)) {
                // the last page
                performFinishAction();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // only if the pager is currently being dragged by the user
        if (state != ViewPager.SCROLL_STATE_SETTLING) {
            mScrollState = state;
        }
    }

    @Override
    public void onPageScrolled(int position,
                               float positionOffset,
                               int positionOffsetPixels) {
        if ((positionOffset > 0.0f) && (mScrollState == ViewPager.SCROLL_STATE_DRAGGING)) {
            mHistoryPrevious = mPositionOffset > positionOffset;
            mPositionOffset = positionOffset;
        }
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG,
              "onPageSelected, position: " + position + ", previous: " + mHistoryPrevious);

        // sets default paging control
        mViewPager.setPagingEnabled(true);

        if (mHistoryPrevious) {
            if (mPager.getHistory()
                      .isEmpty()) {
                mHistoryPrevious = false;
            }
            else {
                // go back in the navigation history
                final IValidateFragment fragment = getPageFragment(position);

                if ((fragment != null) && (fragment.getResourceTitle() == mPager.getHistory()
                                                                                .getLast())) {
                    mPager.getHistory()
                          .pollLast();
                }
                else {
                    goToPageByKey(mPager.getHistory()
                                        .getLast());

                    return;
                }
            }
        }
        else {
            final IValidateFragment fragment = getPageFragment(mPager.getPosition());

            if (fragment != null) {
                mPager.getHistory()
                      .addLast(fragment.getResourceTitle());
            }

            // checks validation before switching to the next page
            final IValidateFragment getLastFragmentInHistory = getPageFragmentByKey(mPager.getHistory()
                                                                                          .getLast());

            if ((position > 0) && !((getLastFragmentInHistory == null) || getLastFragmentInHistory.validate())) {
                goToPreviousPage();
                return;
            }
        }

        mHistoryPrevious = false;

        Log.d(TAG,
              "onPageSelected: " + mPager);

        IValidateFragment fragment = getPageFragment(position);

        // updates title
        setTitle(mAdapter.getPageTitle(mViewPager.getCurrentItem()));

        // refreshes the current view if needed
        if (fragment != null) {
            fragment.refreshView();

            // disable or enable paging control for the current instance of IValidateFragment
            mViewPager.setPagingEnabled(fragment.getPagingEnabled());
        }

        // updates navigation buttons statuses
        if ((fragment != null) && (fragment instanceof IValidateWithNavigationControlFragment)) {
            // disable or enable paging control for the current instance of IValidateWithNavigationControlFragment
            mViewPager.setPagingLeftEnabled(((IValidateWithNavigationControlFragment) fragment).getPagingToPreviousEnabled());
            mViewPager.setPagingRightEnabled(((IValidateWithNavigationControlFragment) fragment).getPagingToForwardEnabled());

            if (((IValidateWithNavigationControlFragment) fragment).getPagingToPreviousEnabled()) {
                mPreviousButton.setEnabled(mViewPager.getCurrentItem() > 0);
                mPreviousButton.setVisibility((mViewPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);
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
            mPreviousButton.setEnabled(mViewPager.getCurrentItem() > 0);
            mPreviousButton.setVisibility((mViewPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);

            mNextButton.setText((mViewPager.getCurrentItem() < (mAdapter.getCount() - 1)) ? R.string.button_pager_next : R.string.button_pager_finish);
            mNextButton.setEnabled((fragment == null) || fragment.validate());
            mNextButton.setVisibility(View.VISIBLE);
        }

        mPager.setPosition(mViewPager.getCurrentItem());
    }

    @Override
    public void goToPreviousPage() {
        if ((mViewPager.getCurrentItem() > 0) && (!mPager.getHistory()
                                                         .isEmpty())) {
            mHistoryPrevious = true;
            goToPageByKey(mPager.getHistory()
                                .getLast());
        }
    }

    /**
     * Go to the last given page in history.
     *
     * @param key the page key
     */
    public void goBackInHistory(int key) {
        while ((!mPager.getHistory()
                       .isEmpty()) && (key != mPager.getHistory()
                                                    .getLast())) {
            mPager.getHistory()
                  .pollLast();
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

        for (Integer pageKey : mPager.getHistory()) {
            if (pageKey == key) {
                count++;
            }
        }

        return count;
    }
}
