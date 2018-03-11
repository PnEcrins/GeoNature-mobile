package com.geonature.mobile.mortality.ui.input.observers;

import android.net.Uri;

import com.geonature.mobile.commons.input.Observer;
import com.geonature.mobile.commons.ui.input.observers.AbstractObserversAndDateFragment;
import com.geonature.mobile.mortality.MainApplication;
import com.geonature.mobile.mortality.R;
import com.geonature.mobile.mortality.content.MainContentProvider;
import com.geonature.mobile.mortality.ui.input.PagerFragmentActivity;
import com.geonature.mobile.mortality.ui.observers.ObserversFragmentActivity;

import java.util.Date;
import java.util.Map;

/**
 * Selected observer and current date as first {@code Fragment} used by {@link PagerFragmentActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressWarnings("ALL")
public class ObserversAndDateFragment
        extends AbstractObserversAndDateFragment {

    @Override
    public String getObserversIntentAction() {

        return ObserversFragmentActivity.class.getName();
    }

    @Override
    public int getDateFormatResourceId() {

        return R.string.observers_and_date_date_format;
    }

    @Override
    public Observer getDefaultObserver() {

        return ((MainApplication) getActivity().getApplication()).getDefaultObserver();
    }

    @Override
    public Map<Long, Observer> getSelectedObservers() {

        return ((MainApplication) getActivity().getApplication()).getInput()
                                                                 .getObservers();
    }

    @Override
    public Date getSelectedDate() {

        return ((MainApplication) getActivity().getApplication()).getInput()
                                                                 .getDate();
    }

    @Override
    public void updateSelectedDate(Date pDate) {

        ((MainApplication) getActivity().getApplication()).getInput()
                                                          .setDate(pDate);
    }

    @Override
    public Uri getLoaderUri() {

        return MainContentProvider.CONTENT_OBSERVERS_URI;
    }
}
