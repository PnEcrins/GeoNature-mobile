package com.geonature.mobile.flora.ui.sync;

import com.geonature.mobile.commons.sync.AbstractSynchronizationActivity;
import com.geonature.mobile.commons.sync.SyncSettings;
import com.geonature.mobile.flora.MainApplication;

/**
 * Synchronization view.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SynchronizationActivity extends AbstractSynchronizationActivity {

    @Override
    protected SyncSettings getSyncSettings() {
        return ((MainApplication) getApplication()).getAppSettings()
                                                   .getSyncSettings();
    }
}
