package com.makina.ecrins.maps.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

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
     * @see ApplicationInfo#FLAG_DEBUGGABLE
     */
    public static boolean isDebuggable(Context pContext) {
        return (pContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
