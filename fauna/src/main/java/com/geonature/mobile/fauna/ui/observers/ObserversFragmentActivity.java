package com.geonature.mobile.fauna.ui.observers;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.geonature.mobile.commons.content.AbstractMainContentProvider;
import com.geonature.mobile.commons.input.InputType;
import com.geonature.mobile.commons.input.Observer;
import com.geonature.mobile.commons.ui.observers.AbstractObserversFragmentActivity;
import com.geonature.mobile.fauna.content.MainContentProvider;
import com.geonature.mobile.fauna.MainApplication;

/**
 * Lists all {@link Observer}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ObserversFragmentActivity
        extends AbstractObserversFragmentActivity {

    @Override
    public int getFilter() {

        return InputType.FAUNA.getKey();
    }

    @NonNull
    @Override
    public Uri getLoaderUri(
            int id,
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

    @Override
    public void initializeSelection() {

        getSelectedObservers().clear();

        if (isSingleChoice()) {
            if (((MainApplication) getApplication()).getDefaultObserver() != null) {
                getSelectedObservers().put(((MainApplication) getApplication()).getDefaultObserver()
                                                                               .getObserverId(),
                                           ((MainApplication) getApplication()).getDefaultObserver());
            }
        }
        else {
            getSelectedObservers().putAll(((MainApplication) getApplication()).getInput()
                                                                              .getObservers());
        }
    }

    @Override
    public boolean updateSelection() {

        if (isSingleChoice()) {
            if (getSelectedObservers().isEmpty()) {
                ((MainApplication) getApplication()).setDefaultObserver(null);
            }
            else {
                ((MainApplication) getApplication()).setDefaultObserver(getSelectedObservers().values()
                                                                                              .iterator()
                                                                                              .next());
            }

            return ((MainApplication) getApplication()).getDefaultObserver() != null;
        }
        else {
            if (getSelectedObservers().isEmpty()) {
                ((MainApplication) getApplication()).getInput()
                                                    .getObservers()
                                                    .clear();
            }
            else {
                ((MainApplication) getApplication()).getInput()
                                                    .getObservers()
                                                    .clear();
                ((MainApplication) getApplication()).getInput()
                                                    .getObservers()
                                                    .putAll(getSelectedObservers());
            }

            return !((MainApplication) getApplication()).getInput()
                                                        .getObservers()
                                                        .isEmpty();
        }
    }
}
