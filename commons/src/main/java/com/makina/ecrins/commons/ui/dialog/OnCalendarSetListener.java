package com.makina.ecrins.commons.ui.dialog;

import java.util.Calendar;

/**
 * The callback used to indicate the user changed the date and the time.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see {@link DateTimePickerDialogFragment}, {@link DatePickerDialogFragment}
 */
public interface OnCalendarSetListener {

    /**
     * Called upon a date or time change.
     *
     * @param calendar the updated calendar
     */
    public void onCalendarSet(Calendar calendar);
}
