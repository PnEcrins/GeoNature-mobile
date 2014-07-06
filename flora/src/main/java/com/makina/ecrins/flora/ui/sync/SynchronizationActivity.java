package com.makina.ecrins.flora.ui.sync;

import com.makina.ecrins.commons.sync.AbstractSynchronizationActivity;
import com.makina.ecrins.commons.sync.SyncSettings;
import com.makina.ecrins.flora.MainApplication;

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
