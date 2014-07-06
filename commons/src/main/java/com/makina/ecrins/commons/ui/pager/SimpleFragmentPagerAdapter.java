package com.makina.ecrins.commons.ui.pager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic implementation of <code>FragmentPagerAdapter </code> used by {@link AbstractPagerFragmentActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private final Map<Integer, Fragment> mFragments = new LinkedHashMap<Integer, Fragment>();

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.mContext = context;
    }

    public Map<Integer, Fragment> getFragments() {
        return mFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return (new ArrayList<Fragment>(mFragments.values())).get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final Fragment fragment = getItem(position);

        if (fragment instanceof IValidateFragment) {
            return mContext.getText(((IValidateFragment) fragment).getResourceTitle());
        }
        else {
            return null;
        }
    }
}
