package com.makina.ecrins.flora.content;

import android.net.Uri;

import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.flora.MainApplication;

/**
 * Simple <code>ContentProvider</code> implementation.
 * <p/>
 * Uses {@link com.makina.ecrins.commons.content.MainDatabaseHelper}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainContentProvider extends AbstractMainContentProvider {

    public static final String AUTHORITY = "com.makina.ecrins.flora.content.MainContentProvider";

    public static final Uri CONTENT_OBSERVERS_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_OBSERVERS);
    public static final Uri CONTENT_TAXA_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_TAXA);
    public static final Uri CONTENT_TAXA_UNITY_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_TAXA_UNITY);
    public static final Uri CONTENT_INCLINES_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_INCLINES);
    public static final Uri CONTENT_PHENOLOGY_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_PHENOLOGY);
    public static final Uri CONTENT_PHYSIOGNOMY_GROUPS_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_PHYSIOGNOMY_GROUPS);
    public static final Uri CONTENT_DISTURBANCES_CLASSIFICATIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_DISTURBANCES_CLASSIFICATIONS);
    public static final Uri CONTENT_PROSPECTING_AREAS_TAXON_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_PROSPECTING_AREAS_TAXON);

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    @Override
    public AbstractAppSettings getAppSettings() {
        return MainApplication.getInstance().getAppSettings();
    }
}
