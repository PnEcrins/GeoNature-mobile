package com.geonature.mobile.commons.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;

import com.geonature.mobile.commons.R;

/**
 * Helper class about application theme.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class ThemeUtils {

    @ColorInt
    public static int getPrimaryColor(Context context) {
        return getColor(context,
                        R.attr.colorPrimary);
    }

    @ColorInt
    public static int getPrimaryDarkColor(Context context) {
        return getColor(context,
                        R.attr.colorPrimaryDark);
    }

    @ColorInt
    public static int getAccentColor(Context context) {
        return getColor(context,
                        R.attr.colorAccent);
    }

    @ColorInt
    private static int getColor(Context context,
                                int colorAttribute) {
        final TypedArray typedArray = context.getTheme()
                                             .obtainStyledAttributes(new int[] {
                                                     colorAttribute
                                             });
        int color = typedArray.getColor(0,
                                        0);

        typedArray.recycle();

        return color;
    }
}
