package com.makina.ecrins.commons.ui.pager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Manage {@code ViewPager} information.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class PagerHelper {

    private static final String TAG = PagerHelper.class.getName();

    private static final String KEY_PAGER_PREFIX = "KEY_PAGER_";

    private final Context mContext;
    private final PagerJsonReader mPagerJsonReader;
    private final PagerJsonWriter mPagerJsonWriter;

    public PagerHelper(@NonNull final Context context) {
        this.mContext = context;
        this.mPagerJsonReader = new PagerJsonReader();
        this.mPagerJsonWriter = new PagerJsonWriter();
    }

    @NonNull
    public Pager load(long pagerId) {
        // read input as JSON from shared preferences
        final String json = PreferenceManager.getDefaultSharedPreferences(mContext)
                                             .getString(getPagerPreferenceKey(pagerId),
                                                        null);

        if (TextUtils.isEmpty(json)) {
            return new Pager(pagerId);
        }

        final Pager pager = mPagerJsonReader.read(json);

        if (pager == null) {
            return new Pager(pagerId);
        }

        return pager;
    }

    public void save(@NonNull final Pager pager) {
        final String pagerAsJson = mPagerJsonWriter.write(pager);

        if (TextUtils.isEmpty(pagerAsJson)) {
            Log.w(TAG,
                  "PagerHelper: failed to save pager metadata " + pager);
        }

        PreferenceManager.getDefaultSharedPreferences(mContext)
                         .edit()
                         .putString(getPagerPreferenceKey(pager.getId()),
                                    pagerAsJson)
                         .apply();
    }

    public void delete(long pagerId) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                         .edit()
                         .remove(getPagerPreferenceKey(pagerId))
                         .apply();
    }

    @Nullable
    protected String getPagerPreferenceKey(long pagerId) {
        return KEY_PAGER_PREFIX + pagerId;
    }
}
