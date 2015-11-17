package com.makina.ecrins.fauna.ui.input.observers;

import android.net.Uri;

import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.input.observers.AbstractObserversAndDateFragment;
import com.makina.ecrins.fauna.MainApplication;
import com.makina.ecrins.fauna.R;
import com.makina.ecrins.fauna.content.MainContentProvider;
import com.makina.ecrins.fauna.ui.input.PagerFragmentActivity;
import com.makina.ecrins.fauna.ui.observers.ObserversFragmentActivity;

import java.util.Date;
import java.util.Map;

/**
 * Selected observer and current date as first <code>Fragment</code> used by {@link PagerFragmentActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
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
