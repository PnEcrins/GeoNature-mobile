package com.makina.ecrins.commons.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TabHost;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.makina.ecrins.commons.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Custom <code>Dialog</code> used to choose the date and the time.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class DateTimePickerDialogFragment extends DialogFragment {

    private static final String KEY_MAX_DATE = "max_date";

    private static OnCalendarSetListener sOnCalendarSetListener;

    public static DateTimePickerDialogFragment newInstance(OnCalendarSetListener pOnCalendarSetListener) {
        return newInstance(0, pOnCalendarSetListener);
    }

    public static DateTimePickerDialogFragment newInstance(long maxDate, OnCalendarSetListener pOnCalendarSetListener) {
        Log.d(DateTimePickerDialogFragment.class.getName(), "newInstance");

        DateTimePickerDialogFragment dialogFragment = new DateTimePickerDialogFragment();
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_datetime, null);

        final Calendar current = Calendar.getInstance();

        if (getArguments().getLong(KEY_MAX_DATE) > 0) {
            current.setTimeInMillis(getArguments().getLong(KEY_MAX_DATE));
        }

        final Calendar selected = Calendar.getInstance();
        selected.setTime(current.getTime());

        if (current.before(selected)) {
            selected.setTime(current.getTime());
        }

        final TabHost tabs = (TabHost) view.findViewById(android.R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("tabDate");
        spec.setContent(R.id.tabDate);
        spec.setIndicator(getString(R.string.alert_dialog_datetime_tab_date));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabTime");
        spec.setContent(R.id.tabTime);
        spec.setIndicator(getString(R.string.alert_dialog_datetime_tab_time));
        tabs.addTab(spec);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("test")
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sOnCalendarSetListener.onCalendarSet(selected);
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, null)
                .create();

        dialog.setTitle(getDateTimeFormat(selected.getTime()));

        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker1);
        datePicker.init(selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selected.set(Calendar.YEAR, year);
                selected.set(Calendar.MONTH, monthOfYear);
                selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // specific implementation to set the maximum date value as current date
                if ((getArguments().getLong(KEY_MAX_DATE) > 0) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) && current.before(selected)) {
                    selected.setTime(current.getTime());
                    view.init(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH), this);
                }

                dialog.setTitle(getDateTimeFormat(selected.getTime()));
            }
        });

        if ((getArguments().getLong(KEY_MAX_DATE) > 0) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            datePicker.setMaxDate(current.getTimeInMillis());
        }

        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker1);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(selected.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(selected.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selected.set(Calendar.MINUTE, minute);

                dialog.setTitle(getDateTimeFormat(selected.getTime()));
            }
        });

        return dialog;
    }

    private String getDateTimeFormat(Date date) {
        return DateFormat.getLongDateFormat(getActivity()).format(date) + " " + DateFormat.getTimeFormat(getActivity()).format(date);
    }
}
