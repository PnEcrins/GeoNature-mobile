package com.makina.ecrins.flora.ui.settings;

import com.makina.ecrins.commons.ui.settings.AbstractPreferencesActivity;
import com.makina.ecrins.commons.ui.settings.AbstractPreferencesFragment;

/**
 * Global preferences for this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see MainPreferencesFragment
 */
public class MainPreferencesActivity
        extends AbstractPreferencesActivity {

    @Override
    protected AbstractPreferencesFragment newFragment() {

        return new MainPreferencesFragment();
    }
}
