package com.makina.ecrins.commons.ui.dialog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import com.makina.ecrins.commons.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Custom {@code Dialog} used to choose the date and the time.
 * <p/>
 * Use {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder} to instantiate
 * this {@code DialogFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see com.makina.ecrins.commons.ui.dialog.OnCalendarSetListener
 */
public final class DateTimePickerDialogFragment extends DialogFragment {

    public static final int TAB_DATE_INDEX = 0;
    public static final int TAB_TIME_INDEX = 1;

    private static final String KEY_SELECTED_TAB_INDEX = "selected_tab_index";
    private static final String KEY_SHOW_TIME = "show_time";
    private static final String KEY_CURRENT_DATE = "current_date";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_TIME_INTERVAL = "time_interval";
    private static final String KEY_SELECTED_DATE = "selected_date";

    private final Calendar mSelectedDateCalendar = Calendar.getInstance();
    private OnCalendarSetListener mOnCalendarSetListener;

    public void setOnCalendarSetListener(OnCalendarSetListener pOnCalendarSetListener) {
        mOnCalendarSetListener = pOnCalendarSetListener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view;

        if (getArguments().getBoolean(KEY_SHOW_TIME)) {
            view = View.inflate(
                    getActivity(),
                    R.layout.dialog_datetime,
                    null
            );

            configureTabHost((TabHost) view.findViewById(android.R.id.tabhost));
        }
        else {
            view = View.inflate(
                    getActivity(),
                    R.layout.dialog_date,
                    null
            );
        }

        Date minDate = (getArguments().getSerializable(KEY_MIN_DATE) == null) ? null : (Date) getArguments().getSerializable(KEY_MIN_DATE);
        Date maxDate = (getArguments().getSerializable(KEY_MAX_DATE) == null) ? null : (Date) getArguments().getSerializable(KEY_MAX_DATE);

        // do nothing if we have inconsistent values (e.g. minDate > maxDate)
        if (minDate != null && maxDate != null && minDate.after(maxDate)) {
            minDate = null;
            maxDate = null;
        }

        if (getArguments().getSerializable(KEY_CURRENT_DATE) != null) {
            mSelectedDateCalendar.setTime((Date) getArguments().getSerializable(KEY_CURRENT_DATE));
        }

        // restore the current selected date
        if (savedInstanceState != null) {
            mSelectedDateCalendar.setTime((Date) savedInstanceState.getSerializable(KEY_SELECTED_DATE));
        }

        if ((maxDate != null) && maxDate.before(mSelectedDateCalendar.getTime())) {
            mSelectedDateCalendar.setTime(maxDate);
        }

        if ((minDate != null) && minDate.after(mSelectedDateCalendar.getTime())) {
            mSelectedDateCalendar.setTime(minDate);
        }

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(
                        R.string.alert_dialog_ok,
                        new OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                mOnCalendarSetListener.onCalendarSet(mSelectedDateCalendar);
                            }
                        }
                )
                .setNegativeButton(
                        R.string.alert_dialog_cancel,
                        null
                )
                .create();

        configureDatePicker(
                (DatePicker) view.findViewById(R.id.datePicker1),
                dialog,
                minDate,
                maxDate
        );

        if (getArguments().getBoolean(KEY_SHOW_TIME)) {
            configureTimePicker(
                    (TimePicker) view.findViewById(R.id.timePicker1),
                    dialog,
                    getArguments().getInt(
                            KEY_TIME_INTERVAL,
                            1
                    )
            );
        }

        setDialogTitle(dialog);

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(
                KEY_SELECTED_DATE,
                mSelectedDateCalendar.getTime()
        );

        super.onSaveInstanceState(outState);
    }

    private void configureTabHost(@NonNull final TabHost tabHost) {
        tabHost.setup();

        tabHost.addTab(
                tabHost.newTabSpec("tabDate")
                        .setContent(R.id.tabDate)
                        .setIndicator(getString(R.string.alert_dialog_datetime_tab_date))
        );

        tabHost.addTab(
                tabHost.newTabSpec("tabTime")
                        .setContent(R.id.tabTime)
                        .setIndicator(getString(R.string.alert_dialog_datetime_tab_time))
        );

        tabHost.setCurrentTab(
                getArguments().getInt(
                        KEY_SELECTED_TAB_INDEX,
                        TAB_DATE_INDEX
                )
        );
    }

    @SuppressLint("NewApi")
    private void configureDatePicker(
            @NonNull final DatePicker datePicker,
            @NonNull final AlertDialog dialog,
            @Nullable final Date minDate,
            @Nullable final Date maxDate) {

        // bug fix: DatePicker and NumberPicker causes 'force close' when rotating screen
        // see: https://code.google.com/p/android/issues/detail?id=22754
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            datePicker.setSaveFromParentEnabled(false);
        }

        datePicker.setSaveEnabled(true);
        datePicker.init(
                mSelectedDateCalendar.get(Calendar.YEAR),
                mSelectedDateCalendar.get(Calendar.MONTH),
                mSelectedDateCalendar.get(Calendar.DAY_OF_MONTH),
                new OnDateChangedListener() {
                    @Override
                    public void onDateChanged(
                            DatePicker view,
                            int year,
                            int monthOfYear,
                            int dayOfMonth) {
                        mSelectedDateCalendar.set(
                                Calendar.YEAR,
                                year
                        );
                        mSelectedDateCalendar.set(
                                Calendar.MONTH,
                                monthOfYear
                        );
                        mSelectedDateCalendar.set(
                                Calendar.DAY_OF_MONTH,
                                dayOfMonth
                        );

                        // specific implementation to set the minimal date value as current date
                        if ((minDate != null) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) && mSelectedDateCalendar.getTime().before(minDate)) {
                            mSelectedDateCalendar.setTime(minDate);
                            view.init(
                                    mSelectedDateCalendar.get(Calendar.YEAR),
                                    mSelectedDateCalendar.get(Calendar.MONTH),
                                    mSelectedDateCalendar.get(Calendar.DAY_OF_MONTH),
                                    this
                            );
                        }

                        // specific implementation to set the maximal date value as current date
                        if ((maxDate != null) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) && mSelectedDateCalendar.getTime().after(maxDate)) {
                            mSelectedDateCalendar.setTime(maxDate);
                            view.init(
                                    mSelectedDateCalendar.get(Calendar.YEAR),
                                    mSelectedDateCalendar.get(Calendar.MONTH),
                                    mSelectedDateCalendar.get(Calendar.DAY_OF_MONTH),
                                    this
                            );
                        }

                        setDialogTitle(dialog);
                    }
                }
        );

        if ((minDate != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            datePicker.setMinDate(minDate.getTime());
        }

        if ((maxDate != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            datePicker.setMaxDate(maxDate.getTime());
        }
    }

    private void configureTimePicker(
            @NonNull final TimePicker timePicker,
            @NonNull final AlertDialog dialog,
            final int timeInterval) {

        // bug fix: TimePicker and NumberPicker causes 'force close' when rotating screen
        // see: https://code.google.com/p/android/issues/detail?id=22754
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            timePicker.setSaveFromParentEnabled(false);
        }

        timePicker.setSaveEnabled(true);

        // use or not 24-hour format according to system settings
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));

        // apply time interval for minutes field
        if (timeInterval > 1) {
            forceInterval(
                    timePicker,
                    timeInterval
            );

            mSelectedDateCalendar.set(
                    Calendar.MINUTE,
                    (mSelectedDateCalendar.get(Calendar.MINUTE) / timeInterval) * timeInterval
            );
        }

        timePicker.setOnTimeChangedListener(
                new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(
                            TimePicker view,
                            int hourOfDay,
                            int minute) {
                        mSelectedDateCalendar.set(
                                Calendar.HOUR_OF_DAY,
                                hourOfDay
                        );
                        mSelectedDateCalendar.set(
                                Calendar.MINUTE,
                                minute * timeInterval
                        );

                        setDialogTitle(dialog);
                    }
                }
        );

        timePicker.setCurrentHour(mSelectedDateCalendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(mSelectedDateCalendar.get(Calendar.MINUTE) / timeInterval);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void forceInterval(
            @NonNull final TimePicker timePicker,
            int timeInterval) {
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");

            NumberPicker minuteSpinner = (NumberPicker) timePicker.findViewById(
                    classForid.getField("minute")
                            .getInt(null)
            );
            minuteSpinner.setMinValue(0);
            minuteSpinner.setMaxValue((60 / timeInterval) - 1);

            final List<String> displayedValues = new ArrayList<>();

            for (int i = 0; i < 60; i += timeInterval) {
                displayedValues.add(
                        String.format(
                                "%02d",
                                i
                        )
                );
            }

            minuteSpinner.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
            minuteSpinner.setValue(mSelectedDateCalendar.get(Calendar.MINUTE) / timeInterval);

            EditText minuteSpinnerInput = (EditText) minuteSpinner.findViewById(
                    classForid.getField("numberpicker_input")
                            .getInt(null)
            );
            minuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            minuteSpinnerInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException ge) {
            Log.w(
                    DateTimePickerDialogFragment.class.getName(),
                    ge.getMessage()
            );
        }
    }

    private void setDialogTitle(@NonNull final AlertDialog dialog) {
        if (getArguments().getBoolean(KEY_SHOW_TIME)) {
            dialog.setTitle(DateFormat.getLongDateFormat(getActivity()).format(mSelectedDateCalendar.getTime()) + " " + DateFormat.getTimeFormat(getActivity()).format(mSelectedDateCalendar.getTime()));
        }
        else {
            dialog.setTitle(DateFormat.getLongDateFormat(getActivity()).format(mSelectedDateCalendar.getTime()));
        }
    }

    /**
     * Builder implementation used to instantiate {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static class Builder {

        private int mSelectedTabindex;
        private boolean mShowTime;
        private Date mCurrentDate;
        private Date mMinDate;
        private Date mMaxDate;
        private int mTimeInterval;

        /**
         * {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder} instances
         * should NOT be constructed in standard programming.
         */
        private Builder() {
            this.mSelectedTabindex = TAB_DATE_INDEX;
            this.mShowTime = true;
            this.mCurrentDate = Calendar.getInstance().getTime();
            this.mMinDate = null;
            this.mMaxDate = null;
            this.mTimeInterval = 1;
        }

        public static Builder newInstance() {
            return new Builder();
        }

        /**
         * Selects the current tab index if both date time pickers are visible.
         * <ul>
         * <li>{@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment#TAB_DATE_INDEX}: to select the date picker tab</li>
         * <li>{@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment#TAB_TIME_INDEX}: to select the time picker tab</li>
         * </ul>
         *
         * @param pSelectedTabindex the selected tab index
         *
         * @return This {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}
         * object to allow for chaining of calls to set methods
         */
        public Builder selectTabIndex(int pSelectedTabindex) {
            this.mSelectedTabindex = pSelectedTabindex;

            return this;
        }

        /**
         * Show or not the time picker within a tab widget.
         *
         * @param pShowTime flag to indicate if we want to show the time picker
         *
         * @return This {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}
         * object to allow for chaining of calls to set methods
         */
        public Builder showTime(boolean pShowTime) {
            this.mShowTime = pShowTime;

            return this;
        }

        /**
         * Sets the current date (default is now).
         *
         * @param pCurrentDate the current date to set
         *
         * @return This {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}
         * object to allow for chaining of calls to set methods
         */
        public Builder currentDate(Date pCurrentDate) {
            this.mCurrentDate = pCurrentDate;

            return this;
        }

        /**
         * Sets the minimal date under which the date should not be settable.
         * <p/>
         * Set this value to {@code null} to disable it.
         *
         * @param pMinDate the minimal date to set
         *
         * @return This {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}
         * object to allow for chaining of calls to set methods
         */
        public Builder minDate(@Nullable final Date pMinDate) {
            if (pMinDate == null) {
                this.mMinDate = null;
            }
            else {
                // bug fix: avoid IllegalArgumentException while setting the DatePicker.setMinDate()
                final Calendar minCalendar = Calendar.getInstance();
                minCalendar.setTime(pMinDate);
                minCalendar.set(
                        Calendar.HOUR_OF_DAY,
                        minCalendar.getMinimum(Calendar.HOUR_OF_DAY)
                );
                minCalendar.set(
                        Calendar.MINUTE,
                        minCalendar.getMinimum(Calendar.MINUTE)
                );
                minCalendar.set(
                        Calendar.SECOND,
                        minCalendar.getMinimum(Calendar.SECOND)
                );
                minCalendar.set(
                        Calendar.MILLISECOND,
                        minCalendar.getMinimum(Calendar.MILLISECOND)
                );

                this.mMinDate = minCalendar.getTime();
            }

            return this;
        }

        /**
         * Sets the maximal date under which the date should not be settable.
         * <p/>
         * Set this value to {@code null} to disable it.
         *
         * @param pMaxDate the maximal date to set
         *
         * @return This {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}
         * object to allow for chaining of calls to set methods
         */
        public Builder maxDate(@Nullable final Date pMaxDate) {
            if (pMaxDate == null) {
                this.mMaxDate = null;
            }
            else {
                // bug fix: avoid IllegalArgumentException while setting the DatePicker.setMaxDate()
                final Calendar maxCalendar = Calendar.getInstance();
                maxCalendar.setTime(pMaxDate);
                maxCalendar.set(
                        Calendar.HOUR_OF_DAY,
                        maxCalendar.getMaximum(Calendar.HOUR_OF_DAY)
                );
                maxCalendar.set(
                        Calendar.MINUTE,
                        maxCalendar.getMaximum(Calendar.MINUTE)
                );
                maxCalendar.set(
                        Calendar.SECOND,
                        maxCalendar.getMaximum(Calendar.SECOND)
                );
                maxCalendar.set(
                        Calendar.MILLISECOND,
                        maxCalendar.getMaximum(Calendar.MILLISECOND)
                );

                this.mMaxDate = maxCalendar.getTime();
            }

            return this;
        }

        /**
         * Sets the time interval for minute field (default: 1).
         * <p><b>Note:</b> Time interval is only available for devices running at least Android Honeycomb.</p>
         *
         * @param pTimeInterval the maximal date to set
         *
         * @return This {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}
         * object to allow for chaining of calls to set methods
         */
        public Builder timeInterval(int pTimeInterval) {
            this.mTimeInterval = pTimeInterval;

            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) || (this.mTimeInterval < 1) || (this.mTimeInterval > 60)) {
                this.mTimeInterval = 1;
            }

            return this;
        }

        /**
         * Creates a {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment} instance
         * with the arguments provided to this {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment.Builder}.
         *
         * @return a new instance of {@link com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment}
         */
        public DateTimePickerDialogFragment create() {
            final DateTimePickerDialogFragment dialogFragment = new DateTimePickerDialogFragment();
            final Bundle args = new Bundle();
            dialogFragment.setArguments(args);
            args.putInt(
                    KEY_SELECTED_TAB_INDEX,
                    mSelectedTabindex
            );
            args.putBoolean(
                    KEY_SHOW_TIME,
                    mShowTime
            );
            args.putSerializable(
                    KEY_MIN_DATE,
                    mMinDate
            );
            args.putSerializable(
                    KEY_CURRENT_DATE,
                    mCurrentDate
            );
            args.putSerializable(
                    KEY_MAX_DATE,
                    mMaxDate
            );
            args.putInt(
                    KEY_TIME_INTERVAL,
                    mTimeInterval
            );

            return dialogFragment;
        }
    }
}
