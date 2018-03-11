package com.geonature.mobile.flora.ui.input.observers;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.geonature.mobile.commons.ui.input.observers.AbstractObserversAndDateInputFragment;
import com.geonature.mobile.flora.R;
import com.geonature.mobile.flora.content.MainContentProvider;
import com.geonature.mobile.flora.ui.input.PagerFragmentActivity;
import com.geonature.mobile.flora.ui.observers.ObserverListActivity;

/**
 * Selected observer and current date as first {@code Fragment} used by {@link PagerFragmentActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class ObserversAndDateInputFragment
        extends AbstractObserversAndDateInputFragment {

    @NonNull
    @Override
    public String getObserversIntentAction() {
        return ObserverListActivity.class.getName();
    }

    @NonNull
    @Override
    public Uri getObserverLoaderUri(long ObserverId) {
        return Uri.withAppendedPath(MainContentProvider.CONTENT_OBSERVERS_URI,
                                    Long.toString(ObserverId));
    }

    @Override
    public int getDateFormatResourceId() {
        return R.string.observers_and_date_date_format;
    }
}
