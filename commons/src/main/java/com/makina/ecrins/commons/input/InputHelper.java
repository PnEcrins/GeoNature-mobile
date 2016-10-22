package com.makina.ecrins.commons.input;

import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Manage {@link AbstractInput}:
 * <ul>
 * <li>Create a new {@link AbstractInput}</li>
 * <li>Read the current {@link AbstractInput}</li>
 * <li>Save the current {@link AbstractInput}</li>
 * <li>Export the current {@link AbstractInput} as {@code JSON} file.</li>
 * </ul>
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class InputHelper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

    private final String dateFormat;

    public InputHelper() {
        this(DEFAULT_DATE_FORMAT);
    }

    public InputHelper(@NonNull final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2000, midnight.
     *
     * @return an unique ID
     */
    public static long generateId() {
        final Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND,
                0);

        final Calendar start = Calendar.getInstance();
        start.set(2000,
                  Calendar.JANUARY,
                  1,
                  0,
                  0,
                  0);
        start.set(Calendar.MILLISECOND,
                  0);

        return (now.getTimeInMillis() - start.getTimeInMillis()) / 1000;
    }
}
