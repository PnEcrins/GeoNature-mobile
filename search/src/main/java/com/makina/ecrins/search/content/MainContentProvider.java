package com.makina.ecrins.search.content;

import android.net.Uri;

import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.search.MainApplication;

/**
 * Simple {@code ContentProvider} implementation.
 * <p/>
 * Uses {@link com.makina.ecrins.commons.content.MainDatabaseHelper}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainContentProvider
        extends AbstractMainContentProvider {

    public static final String AUTHORITY = "com.makina.ecrins.search.content.MainContentProvider";

    public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_SEARCH);

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    @Override
    public AbstractAppSettings getAppSettings() {
        return MainApplication.getInstance().getAppSettings();
    }
}
