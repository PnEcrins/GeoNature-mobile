package com.makina.ecrins.mortality.content;

import android.net.Uri;

import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.mortality.MainApplication;

/**
 * Simple {@code ContentProvider} implementation.
 * <p>
 * Uses {@link com.makina.ecrins.commons.content.MainDatabaseHelper}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainContentProvider
        extends AbstractMainContentProvider {

    public static final String AUTHORITY = "com.makina.ecrins.mortality.content.MainContentProvider";

    public static final Uri CONTENT_OBSERVERS_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_OBSERVERS);
    public static final Uri CONTENT_TAXA_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_TAXA);
    public static final Uri CONTENT_TAXA_UNITY_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_TAXA_UNITY);
    public static final Uri CONTENT_CRITERIA_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_CRITERIA);
    public static final Uri CONTENT_CRITERIA_CLASS_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_CRITERIA_CLASS);

    @Override
    public String getAuthority() {

        return AUTHORITY;
    }

    @Override
    public AbstractAppSettings getAppSettings() {

        return MainApplication.getInstance()
                              .getAppSettings();
    }
}
