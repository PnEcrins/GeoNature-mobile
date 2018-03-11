package com.geonature.mobile.search.content;

import android.net.Uri;

import com.geonature.mobile.commons.content.AbstractMainContentProvider;
import com.geonature.mobile.commons.content.MainDatabaseHelper;
import com.geonature.mobile.commons.settings.AbstractAppSettings;
import com.geonature.mobile.search.MainApplication;

/**
 * Simple {@code ContentProvider} implementation.
 * <p/>
 * Uses {@link MainDatabaseHelper}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainContentProvider
        extends AbstractMainContentProvider {

    public static final String AUTHORITY = "com.geonature.mobile.search.content.MainContentProvider";

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
