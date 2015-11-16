package com.makina.ecrins.mortality.ui.sync;

import com.makina.ecrins.commons.sync.AbstractSynchronizationActivity;
import com.makina.ecrins.commons.sync.SyncSettings;
import com.makina.ecrins.mortality.MainApplication;

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
