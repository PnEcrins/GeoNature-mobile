package com.makina.ecrins.flora.ui.input.observers;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.ui.input.observers.AbstractObserversAndDateInputFragment;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;
import com.makina.ecrins.flora.ui.observers.ObserverListActivity;

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
