package com.makina.ecrins.maps.util;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * Utility class for debugging purpose.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class DebugUtils {

    /**
     * {@link DebugUtils} instances should NOT be constructed in standard programming.
     */
    private DebugUtils() {

    }

    /**
     * Returns <code>true</code> if the application would like to allow debugging of its code, even when installed on a non-development system.
     *
     * @param pContext the current context
     * @return <code>true</code> if the application is being run in debug mode.
     * @see {@link ApplicationInfo#FLAG_DEBUGGABLE}
     */
    public static boolean isDebuggable(Context pContext) {
        return (pContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * Returns <code>true</code> if {@link permission#ACCESS_MOCK_LOCATION} permission is granted.
     *
     * @param pContext the current context
     * @return <code>true</code> if {@link permission#ACCESS_MOCK_LOCATION} permission is granted.
     */
    public static boolean hasAccessMockLocationPermission(Context pContext) {
        return (pContext.checkCallingOrSelfPermission(permission.ACCESS_MOCK_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (Settings.Secure.getInt(pContext.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 1);
    }
}
