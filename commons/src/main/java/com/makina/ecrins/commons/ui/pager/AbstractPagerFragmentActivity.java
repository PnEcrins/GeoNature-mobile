package com.makina.ecrins.commons.ui.pager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.makina.ecrins.commons.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * Basic {@code ViewPager} implementation as {@code AppCompatActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractPagerFragmentActivity
        extends AppCompatActivity
        implements OnClickListener,
                   OnPageChangeListener {

    private static final String TAG = AbstractPagerFragmentActivity.class.getName();

    protected static final String KEY_PAGER = "KEY_PAGER";

    protected SimpleFragmentPagerAdapter mAdapter;
    protected EnablePagingViewPager mViewPager;
    protected Button mPreviousButton;
    protected Button mNextButton;

    protected Pager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pager);

        mAdapter = new SimpleFragmentPagerAdapter(this,
                                                  getSupportFragmentManager());
        mViewPager = (EnablePagingViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        mPreviousButton = (Button) findViewById(R.id.previousButton);
        mNextButton = (Button) findViewById(R.id.nextButton);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(this);

        mPager = (savedInstanceState == null) ? new Pager(0) : (Pager) savedInstanceState.getParcelable(KEY_PAGER);

        if (mPager == null) {
            mPager = new Pager(0);
        }

        if (savedInstanceState == null) {
            mPager.setSize(getPagerFragments().size());
            mPager.setPosition(mViewPager.getCurrentItem());
        }

        for (int i = 0; i < getPagerFragments().size(); i++) {
            IValidateFragment fragment = getPageFragment(i);

            if (fragment == null) {
                // no fragment found through getSupportFragmentManager() so try to find it through getPagerFragments()
                fragment = (new ArrayList<>(getPagerFragments().values())).get(i);
            }

            if (fragment == null) {
                Log.w(TAG,
                      "onPostCreate: no fragment found at position " + i);
            }
            else {
                mAdapter.getFragments()
                        .put(fragment.getResourceTitle(),
                             (Fragment) fragment);
            }
        }

        mAdapter.notifyDataSetChanged();

        if (savedInstanceState == null) {
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    onPageSelected(mViewPager.getCurrentItem());
                }
            });
        }
        else {
            mViewPager.setCurrentItem(mPager.getPosition());
        }

        setTitle(mAdapter.getPageTitle(mViewPager.getCurrentItem()));

        mPreviousButton.setEnabled(mViewPager.getCurrentItem() > 0);
        mPreviousButton.setVisibility((mViewPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);
        mPreviousButton.setOnClickListener(this);

        mNextButton.setEnabled(false);
        mNextButton.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_PAGER,
                               mPager);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IValidateFragment fragment = getPageFragment(mViewPager.getCurrentItem());

        mNextButton.setEnabled((fragment == null) || fragment.validate());
        mNextButton.setText((mViewPager.getCurrentItem() < (mAdapter.getCount() - 1)) ? R.string.button_pager_next : R.string.button_pager_finish);

        // refreshes the current view if needed
        if (fragment != null) {
            fragment.refreshView();

            // disable or enable paging control for the current instance of IValidateFragment
            mViewPager.setPagingEnabled(fragment.getPagingEnabled());
        }
    }

    @NonNull
    protected abstract Map<Integer, IValidateFragment> getPagerFragments();

    protected abstract void performFinishAction();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.previousButton) {
            if (mViewPager.getCurrentItem() > 0) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1,
                                          true);
            }
        }
        else if (v.getId() == R.id.nextButton) {
            if (mViewPager.getCurrentItem() < (mAdapter.getCount() - 1)) {
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
        // nothing to do ...
    }

    @Override
    public void onPageScrolled(int position,
                               float positionOffset,
                               int positionOffsetPixels) {
        // nothing to do ...
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG,
              "onPageSelected: " + position);

        // sets default paging control
        mViewPager.setPagingEnabled(true);

        // checks validation before switching to the next page
        final IValidateFragment fragmentAtPreviousPosition = getPageFragment(position - 1);

        if ((position > 0) && !((fragmentAtPreviousPosition == null) || fragmentAtPreviousPosition.validate())) {
            mViewPager.setCurrentItem(position - 1,
                                      true);
            return;
        }

        // updates title
        setTitle(mAdapter.getPageTitle(mViewPager.getCurrentItem()));

        final IValidateFragment fragmentAtPosition = getPageFragment(position);

        // refreshes the current view if needed
        if (fragmentAtPosition != null) {
            fragmentAtPosition.refreshView();

            // disable or enable paging control for the current instance of IValidateFragment
            mViewPager.setPagingEnabled(fragmentAtPosition.getPagingEnabled());
        }

        // updates navigation buttons statuses
        setTitle(mAdapter.getPageTitle(mViewPager.getCurrentItem()));

        mPreviousButton.setEnabled(mViewPager.getCurrentItem() > 0);
        mPreviousButton.setVisibility((mViewPager.getCurrentItem() > 0) ? View.VISIBLE : View.INVISIBLE);

        mNextButton.setText((mViewPager.getCurrentItem() < (mAdapter.getCount() - 1)) ? R.string.button_pager_next : R.string.button_pager_finish);
        mNextButton.setEnabled((fragmentAtPosition == null) || fragmentAtPosition.validate());

        mPager.setPosition(mViewPager.getCurrentItem());
    }

    public void validateCurrentPage() {
        if (mViewPager.getCurrentItem() < mAdapter.getCount()) {
            IValidateFragment fragment = getPageFragment(mViewPager.getCurrentItem());
            mNextButton.setEnabled((fragment == null) || fragment.validate());
        }
    }

    public void goToPreviousPage() {
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1,
                                      true);
        }
    }

    public void goToNextPage() {
        if (mViewPager.getCurrentItem() < (mAdapter.getCount() - 1)) {
            IValidateFragment fragment = getPageFragment(mViewPager.getCurrentItem());

            if ((fragment != null) && fragment.validate()) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1,
                                          true);
            }
        }
    }

    public void goToPage(int position) {
        mViewPager.setCurrentItem(position,
                                  true);
    }

    public void goToPageByKey(int key) {
        Fragment fragment = mAdapter.getFragments()
                                    .get(key);

        if ((fragment != null) && (fragment instanceof IValidateFragment)) {
            Log.d(TAG,
                  "goToPageByKey: key '" + key + "'");

            mViewPager.setCurrentItem((new ArrayList<>(mAdapter.getFragments()
                                                               .values())).lastIndexOf(fragment),
                                      true);
        }
        else {
            Log.w(TAG,
                  "goToPageByKey: key '" + key + "' undefined");
        }
    }

    public void goToFirstPage() {
        mViewPager.setCurrentItem(0,
                                  true);
    }

    public void goToLastPage() {
        mViewPager.setCurrentItem(mAdapter.getCount() - 1,
                                  true);
    }

    /**
     * Gets the current {@link IValidateFragment} instance at the current position of this pager.
     *
     * @return {@link IValidateFragment} instance
     */
    @Nullable
    public IValidateFragment getCurrentPageFragment() {
        IValidateFragment pageFragment = getPageFragment(mViewPager.getCurrentItem());

        if (pageFragment == null) {
            // no fragment found through getSupportFragmentManager() so try to find it through getPagerFragments()
            pageFragment = (new ArrayList<>(getPagerFragments().values())).get(mViewPager.getCurrentItem());
        }

        if (pageFragment == null) {
            Log.w(TAG,
                  "getCurrentPageFragment: no fragment found at position " + mViewPager.getCurrentItem());
        }

        return pageFragment;
    }

    /**
     * Gets the current {@link IValidateFragment} instance at the current position of this pager.
     *
     * @param position the position of {@link IValidateFragment} to retrieve
     *
     * @return {@link IValidateFragment} instance
     */
    @Nullable
    protected IValidateFragment getPageFragment(Integer position) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + ((position == null) ? mViewPager.getCurrentItem() : position));

        if ((fragment != null) && (fragment instanceof IValidateFragment)) {
            return (IValidateFragment) fragment;
        }
        else {
            Log.w(TAG,
                  "getPageFragment: no fragment found through getSupportFragmentManager() at position " + ((position == null) ? mViewPager.getCurrentItem() : position));

            return null;
        }
    }

    /**
     * Gets the current {@link IValidateFragment} instance for a given key of this pager.
     *
     * @param key the key of {@link IValidateFragment} to retrieve
     *
     * @return {@link IValidateFragment} instance
     *
     * @see AbstractPagerFragmentActivity#getPageFragment(Integer)
     */
    @Nullable
    protected IValidateFragment getPageFragmentByKey(Integer key) {
        return getPageFragment((new ArrayList<>(mAdapter.getFragments()
                                                        .keySet())).indexOf(key));
    }
}
