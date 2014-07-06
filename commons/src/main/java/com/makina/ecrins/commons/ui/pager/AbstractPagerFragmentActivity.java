package com.makina.ecrins.commons.ui.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.makina.ecrins.commons.R;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.Map;

/**
 * Basic <code>ViewPager</code> implementation as <code>FragmentActivity</code>.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractPagerFragmentActivity extends ActionBarActivity
        implements
        OnClickListener,
        OnPageChangeListener {

    protected static final String KEY_PAGER_SIZE = "pager_size";
    protected static final String KEY_PAGER_POSITION = "pager_position";

    protected SimpleFragmentPagerAdapter mAdapter;
    protected Bundle mSavedState;
    protected EnablePagingViewPager mPager;
    protected Button mPreviousButton;
    protected Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pager);

        mAdapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager());
        mPager = (EnablePagingViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mPreviousButton = (Button) findViewById(R.id.previousButton);
        mNextButton = (Button) findViewById(R.id.nextButton);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Log.d(AbstractPagerFragmentActivity.class.getName(), "onPostCreate, savedInstanceState null");

            mSavedState = new Bundle();

            for (Integer fragmentKey : getPagerFragments().keySet()) {
                IValidateFragment fragment = getPagerFragments().get(fragmentKey);

                if (fragment instanceof Fragment) {
                    mAdapter.getFragments().put(fragmentKey, (Fragment) fragment);
                }
                else {
                    Log.w(AbstractPagerFragmentActivity.class.getName(), "fragment '" + fragmentKey + "' must extends Fragment class");
                }
            }

            mAdapter.notifyDataSetChanged();
            mSavedState.putInt(KEY_PAGER_SIZE, getPagerFragments().size());
            mSavedState.putInt(KEY_PAGER_POSITION, mPager.getCurrentItem());
        }
        else {
            Log.d(AbstractPagerFragmentActivity.class.getName(), "onPostCreate, savedInstanceState initialized");

            mSavedState = savedInstanceState;

            for (int i = 0; i < mSavedState.getInt(KEY_PAGER_SIZE); i++) {
                IValidateFragment fragment = getPageFragment(i);

                if (fragment == null) {
                    // no fragment found through getSupportFragmentManager() so try to find it through getPagerFragments()
                    fragment = (new ArrayList<IValidateFragment>(getPagerFragments().values())).get(i);
                }

                if (fragment == null) {
                    Log.w(AbstractPagerFragmentActivity.class.getName(), "onPostCreate : no fragment found at position " + i);
                }
                else {
                    mAdapter.getFragments().put(fragment.getResourceTitle(), (Fragment) fragment);
                }
            }

            mAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGER_POSITION));
        }

        setTitle(mAdapter.getPageTitle(mPager.getCurrentItem()));

        UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setFades(false);
        indicator.setOnPageChangeListener(this);

        mPreviousButton.setEnabled(mPager.getCurrentItem() > 0);
        mPreviousButton.setVisibility((mPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);
        mPreviousButton.setOnClickListener(this);

        mNextButton.setEnabled(false);
        mNextButton.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(AbstractPagerFragmentActivity.class.getName(), "onResume");

        mNextButton.setEnabled((getPageFragment(mPager.getCurrentItem()) == null) || getPageFragment(mPager.getCurrentItem()).validate());
        mNextButton.setText((mPager.getCurrentItem() < (mAdapter.getCount() - 1)) ? R.string.button_pager_next : R.string.button_pager_finish);

        // refreshes the current view if needed
        if (getPageFragment(mPager.getCurrentItem()) != null) {
            getPageFragment(mPager.getCurrentItem()).refreshView();

            // disable or enable paging control for the current instance of IValidateFragment
            mPager.setPagingEnabled(getPageFragment(mPager.getCurrentItem()).getPagingEnabled());
        }
    }

    protected abstract Map<Integer, IValidateFragment> getPagerFragments();

    protected abstract void performFinishAction();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.previousButton) {
            if (mPager.getCurrentItem() > 0) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
            }
        }
        else if (v.getId() == R.id.nextButton) {
            if (mPager.getCurrentItem() < (mAdapter.getCount() - 1)) {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
            }
            else if (mPager.getCurrentItem() == (mAdapter.getCount() - 1)) {
                // the last page
                performFinishAction();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing to do ...
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // nothing to do ...
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(AbstractPagerFragmentActivity.class.getName(), "onPageSelected : " + position);

        // sets default paging control
        mPager.setPagingEnabled(true);

        // checks validation before switching to the next page
        if ((position > 0) && !((getPageFragment(position - 1) == null) || getPageFragment(position - 1).validate())) {
            mPager.setCurrentItem(position - 1, true);
            return;
        }

        // updates title
        setTitle(mAdapter.getPageTitle(mPager.getCurrentItem()));

        // refreshes the current view if needed
        if (getPageFragment(position) != null) {
            getPageFragment(position).refreshView();

            // disable or enable paging control for the current instance of IValidateFragment
            mPager.setPagingEnabled(getPageFragment(position).getPagingEnabled());
        }

        // updates navigation buttons statuses
        setTitle(mAdapter.getPageTitle(mPager.getCurrentItem()));

        mPreviousButton.setEnabled(mPager.getCurrentItem() > 0);
        mPreviousButton.setVisibility((mPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);

        mNextButton.setText((mPager.getCurrentItem() < (mAdapter.getCount() - 1)) ? R.string.button_pager_next : R.string.button_pager_finish);
        mNextButton.setEnabled((getPageFragment(position) == null) || getPageFragment(position).validate());

        mSavedState.putInt(KEY_PAGER_POSITION, mPager.getCurrentItem());
    }

    public void validateCurrentPage() {
        if (mPager.getCurrentItem() < mAdapter.getCount()) {
            IValidateFragment fragment = getPageFragment(mPager.getCurrentItem());
            mNextButton.setEnabled((fragment == null) || fragment.validate());
        }
    }

    public void goToPreviousPage() {
        if (mPager.getCurrentItem() > 0) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
        }
    }

    public void goToNextPage() {
        if (mPager.getCurrentItem() < (mAdapter.getCount() - 1)) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        }
    }

    public void goToPage(int position) {
        mPager.setCurrentItem(position, true);
    }

    public void goToPageByKey(int key) {
        Fragment fragment = mAdapter.getFragments().get(key);

        if ((fragment != null) && (fragment instanceof IValidateFragment)) {
            Log.d(AbstractPagerFragmentActivity.class.getName(), "goToPageByKey : key '" + key + "'");

            mPager.setCurrentItem((new ArrayList<Fragment>(mAdapter.getFragments().values())).lastIndexOf(fragment), true);
        }
        else {
            Log.w(AbstractPagerFragmentActivity.class.getName(), "goToPageByKey : key '" + key + "' undefined");
        }
    }

    public void goToFirstPage() {
        mPager.setCurrentItem(0, true);
    }

    public void goToLastPage() {
        mPager.setCurrentItem(mAdapter.getCount() - 1, true);
    }

    /**
     * Gets the current {@link IValidateFragment} instance at the current position of this pager.
     *
     * @param position the position of {@link IValidateFragment} to retrieve
     * @return {@link IValidateFragment} instance
     */
    protected IValidateFragment getPageFragment(Integer position) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + ((position == null) ? mPager.getCurrentItem() : position.intValue()));

        if ((fragment != null) && (fragment instanceof IValidateFragment)) {
            return (IValidateFragment) fragment;
        }
        else {
            Log.w(AbstractPagerFragmentActivity.class.getName(), "getPageFragment : no fragment found through getSupportFragmentManager() at position " + ((position == null) ? mPager.getCurrentItem() : position.intValue()));

            return null;
        }
    }

    /**
     * Gets the current {@link IValidateFragment} instance for a given key of this pager.
     *
     * @param key the key of {@link IValidateFragment} to retrieve
     * @return {@link IValidateFragment} instance
     * @see AbstractPagerFragmentActivity#getPageFragment(Integer)
     */
    protected IValidateFragment getPageFragmentByKey(Integer key) {
        return getPageFragment((new ArrayList<Integer>(mAdapter.getFragments().keySet())).indexOf(key));
    }
}
