package com.makina.ecrins.fauna.ui.sync;

import com.makina.ecrins.commons.sync.AbstractSynchronizationActivity;
import com.makina.ecrins.commons.sync.SyncSettings;
import com.makina.ecrins.fauna.MainApplication;

/**
 * Synchronization view.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SynchronizationActivity
        extends AbstractSynchronizationActivity {

    @Override
    protected SyncSettings getSyncSettings() {

        return ((MainApplication) getApplication()).getAppSettings()
                                                   .getSyncSettings();
    }
}
