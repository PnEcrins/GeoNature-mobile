package com.makina.ecrins.commons.ui.dialog;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

/**
 * Custom <code>Dialog</code> used to choose the date.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class DatePickerDialogFragment extends DialogFragment {

    private static final String KEY_MAX_DATE = "max_date";

    private static OnCalendarSetListener sOnCalendarSetListener;

    public static DatePickerDialogFragment newInstance(OnCalendarSetListener pOnCalendarSetListener) {
        return newInstance(0, pOnCalendarSetListener);
    }

    public static DatePickerDialogFragment newInstance(long maxDate, OnCalendarSetListener pOnCalendarSetListener) {
        Log.d(DatePickerDialogFragment.class.getName(), "newInstance");

        DatePickerDialogFragment dialogFragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_MAX_DATE, maxDate);
        dialogFragment.setArguments(args);

        sOnCalendarSetListener = pOnCalendarSetListener;

        return dialogFragment;
    }

    public void setOnCalendarSetListener(OnCalendarSetListener pOnCalendarSetListener) {
        sOnCalendarSetListener = pOnCalendarSetListener;
    }

    @Override
    @SuppressLint("NewApi")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar current = Calendar.getInstance();

        if (getArguments().getLong(KEY_MAX_DATE) > 0) {
            current.setTimeInMillis(getArguments().getLong(KEY_MAX_DATE));
        }

        final Calendar selected = Calendar.getInstance();
        selected.setTime(current.getTime());

        if (current.before(selected)) {
            selected.setTime(current.getTime());
        }

        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selected.set(Calendar.YEAR, year);
                selected.set(Calendar.MONTH, monthOfYear);
                selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                sOnCalendarSetListener.onCalendarSet(selected);
            }
        },
                selected.get(Calendar.YEAR),
                selected.get(Calendar.MONTH),
                selected.get(Calendar.DAY_OF_MONTH)
        ) {
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                super.onDateChanged(view, year, month, day);

                selected.set(Calendar.YEAR, year);
                selected.set(Calendar.MONTH, month);
                selected.set(Calendar.DAY_OF_MONTH, day);

                // specific implementation to set the maximum date value as current date
                if ((getArguments().getLong(KEY_MAX_DATE) > 0) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) && current.before(selected)) {
                    selected.setTime(current.getTime());
                    view.init(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH), this);
                }
            }
        };

        if ((getArguments().getLong(KEY_MAX_DATE) > 0) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            datePickerDialog.getDatePicker().setMaxDate(current.getTimeInMillis());
        }

        return datePickerDialog;
    }
}
