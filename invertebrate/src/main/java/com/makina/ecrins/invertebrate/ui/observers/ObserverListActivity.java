package com.makina.ecrins.invertebrate.ui.observers;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.observers.AbstractObserverListActivity;
import com.makina.ecrins.invertebrate.content.MainContentProvider;

/**
 * Let the user to choose an {@link Observer} from the list.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class ObserverListActivity
        extends AbstractObserverListActivity {

    @NonNull
    @Override
    public Uri getLoaderUri(int id,
                            long selectedObserverId) {
        switch (id) {
            case AbstractMainContentProvider.OBSERVERS:
                return MainContentProvider.CONTENT_OBSERVERS_URI;
            case AbstractMainContentProvider.OBSERVER_ID:
                return Uri.withAppendedPath(MainContentProvider.CONTENT_OBSERVERS_URI,
                                            Long.toString(selectedObserverId));
            default:
                throw new IllegalArgumentException("Unknown loader: " + id);
        }
    }
}
