package com.makina.ecrins.app.ui.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.makina.ecrins.app.BuildConfig;
import com.makina.ecrins.app.R;
import com.makina.ecrins.app.ui.adapter.StringResourcesArrayAdapter;
import com.makina.ecrins.app.ui.service.ProgressRequestHandler;
import com.makina.ecrins.commons.service.AbstractRequestHandler;
import com.makina.ecrins.commons.service.RequestHandlerServiceClient;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.ChooseActionDialogFragment;
import com.makina.ecrins.commons.ui.dialog.CommentDialogFragment;
import com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment;
import com.makina.ecrins.commons.ui.dialog.OnCalendarSetListener;
import com.makina.ecrins.commons.ui.dialog.ProgressDialogFragment;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A {@code Fragment} about testing dialogs.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DialogListFragment
        extends Fragment
        implements AbsListView.OnItemClickListener {

    private static final String TAG = DialogListFragment.class.getSimpleName();

    protected static final String ALERT_DIALOG_FRAGMENT = "ALERT_DIALOG_FRAGMENT";
    protected static final String CHOOSE_ACTION_DIALOG_FRAGMENT = "CHOOSE_ACTION_DIALOG_FRAGMENT";
    protected static final String COMMENT_DIALOG_FRAGMENT = "COMMENT_DIALOG_FRAGMENT";
    protected static final String DATE_PICKER_DIALOG_FRAGMENT = "DATE_PICKER_DIALOG_FRAGMENT";
    protected static final String DATE_TIME_PICKER_DIALOG_FRAGMENT = "DATE_TIME_PICKER_DIALOG_FRAGMENT";
    protected static final String PROGRESS_DIALOG_FRAGMENT = "PROGRESS_DIALOG_FRAGMENT";

    private AlertDialogFragment.OnAlertDialogListener mOnAlertDialogListener = new AlertDialogFragment.OnAlertDialogListener() {
        @Override
        public void onPositiveButtonClick(DialogInterface dialog) {
            Toast
                    .makeText(
                            getActivity(),
                            R.string.toast_alert_dialog_positive_button,
                            Toast.LENGTH_SHORT
                    )
                    .show();
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            Toast
                    .makeText(
                            getActivity(),
                            R.string.toast_alert_dialog_negative_button,
                            Toast.LENGTH_SHORT
                    )
                    .show();
        }
    };

    private ChooseActionDialogFragment.OnChooseActionDialogListener mOnChooseActionDialogListener = new ChooseActionDialogFragment.OnChooseActionDialogListener() {
        @Override
        public void onItemClick(
                DialogInterface dialog,
                int position,
                int actionResourceId) {
            Toast
                    .makeText(
                            getActivity(),
                            getString(
                                    R.string.toast_choose_action_dialog_action_selected,
                                    getString(actionResourceId)
                            ),
                            Toast.LENGTH_SHORT
                    )
                    .show();
            dialog.dismiss();
        }
    };

    private CommentDialogFragment.OnCommentDialogValidateListener mOnCommentDialogValidateListener = new CommentDialogFragment.OnCommentDialogValidateListener() {
        @Override
        public void onPositiveButtonClick(
                DialogInterface dialog,
                String message) {
            Toast
                    .makeText(
                            getActivity(),
                            getString(
                                    R.string.toast_comment_dialog_positive_button,
                                    message
                            ),
                            Toast.LENGTH_SHORT
                    )
                    .show();
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            Toast
                    .makeText(
                            getActivity(),
                            R.string.toast_comment_dialog_negative_button,
                            Toast.LENGTH_SHORT
                    )
                    .show();
        }
    };

    private OnCalendarSetListener mOnCalendarSetListener = new OnCalendarSetListener() {
        @Override
        public void onCalendarSet(Calendar calendar) {
            Toast
                    .makeText(
                            getActivity(),
                            getString(
                                    R.string.toast_calendar_set,
                                    DateFormat.getDateTimeInstance().format(calendar.getTime())
                            ),
                            Toast.LENGTH_SHORT
                    )
                    .show();
        }
    };

    private RequestHandlerServiceClient.ServiceClientListener mServiceClientListener = new RequestHandlerServiceClient.ServiceClientListener() {

        @Override
        public void onConnected(@NonNull String token) {
            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "onConnected: " + token
                );
            }

            mRequestHandlerServiceClientToken = token;
        }

        @Override
        public void onDisconnected() {
            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        "onDisconnected"
                );
            }
        }

        @Override
        public void onHandleMessage(
                @NonNull Class<? extends AbstractRequestHandler> requestHandlerClass,
                @NonNull Bundle data) {
            if (isAdded() && (requestHandlerClass == ProgressRequestHandler.class)) {
                if (data.containsKey(ProgressRequestHandler.KEY_PROGRESS_START) && data.containsKey(ProgressRequestHandler.KEY_PROGRESS_END) && data.containsKey(ProgressRequestHandler.KEY_PROGRESS_VALUE) && !data.getBoolean(ProgressRequestHandler.KEY_PROGRESS_FINISH, false)) {
                    // find ProgressDialogFragment to update
                    ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_FRAGMENT);

                    if (progressDialogFragment == null) {
                        progressDialogFragment = ProgressDialogFragment.newInstance(
                                R.string.progress_dialog_title,
                                R.string.progress_dialog_message,
                                ProgressDialog.STYLE_HORIZONTAL,
                                data.getInt(ProgressRequestHandler.KEY_PROGRESS_END));
                        progressDialogFragment.show(
                                getActivity().getSupportFragmentManager(),
                                PROGRESS_DIALOG_FRAGMENT
                        );
                    }

                    progressDialogFragment.setProgress(data.getInt(ProgressRequestHandler.KEY_PROGRESS_VALUE));
                }
                else if (data.getBoolean(ProgressRequestHandler.KEY_PROGRESS_FINISH)) {
                    // find ProgressDialogFragment to update
                    ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_FRAGMENT);

                    if (progressDialogFragment != null) {
                        progressDialogFragment.dismiss();
                    }
                }
            }
        }
    };

    private final List<Integer> mStringResources = Arrays.asList(
            R.string.button_alert_dialog,
            R.string.button_choose_action_dialog,
            R.string.button_comment_dialog,
            R.string.button_date_picker_dialog,
            R.string.button_date_time_picker_dialog,
            R.string.button_progress_dialog
    );

    private RequestHandlerServiceClient mRequestHandlerServiceClient;
    private String mRequestHandlerServiceClientToken;

    /**
     * The Adapter which will be used to populate the ListView with Views.
     */
    private StringResourcesArrayAdapter mAdapter;

    public static DialogListFragment newInstance() {
        final DialogListFragment fragment = new DialogListFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DialogListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new StringResourcesArrayAdapter(getActivity());

        if (savedInstanceState != null) {
            mRequestHandlerServiceClientToken = savedInstanceState.getString(AbstractRequestHandler.KEY_CLIENT_TOKEN);
        }

        // restore AlertDialogFragment state after resume if needed
        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(ALERT_DIALOG_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
        }

        // restore ChooseActionDialogFragment state after resume if needed
        final ChooseActionDialogFragment chooseActionDialogFragment = (ChooseActionDialogFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(CHOOSE_ACTION_DIALOG_FRAGMENT);

        if (chooseActionDialogFragment != null) {
            chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
        }

        // restore CommentDialogFragment state after resume if needed
        final CommentDialogFragment commentDialogFragment = (CommentDialogFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(COMMENT_DIALOG_FRAGMENT);

        if (commentDialogFragment != null) {
            commentDialogFragment.setOnCommentDialogValidateListener(mOnCommentDialogValidateListener);
        }

        // restore DateTimePickerDialogFragment state after resume if needed
        final DateTimePickerDialogFragment datePickerDialogFragment = (DateTimePickerDialogFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(DATE_PICKER_DIALOG_FRAGMENT);

        if (datePickerDialogFragment != null) {
            datePickerDialogFragment.setOnCalendarSetListener(mOnCalendarSetListener);
        }

        // restore DateTimePickerDialogFragment state after resume if needed
        final DateTimePickerDialogFragment dateTimePickerDialogFragment = (DateTimePickerDialogFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(DATE_TIME_PICKER_DIALOG_FRAGMENT);

        if (dateTimePickerDialogFragment != null) {
            dateTimePickerDialogFragment.setOnCalendarSetListener(mOnCalendarSetListener);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_item_list,
                container,
                false
        );

        final ListView mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mListView.setAdapter(mAdapter);

        // set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(
            View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(
                view,
                savedInstanceState
        );

        if (getArguments() != null) {
            mAdapter.clear();

            for (Integer stringResource : mStringResources) {
                mAdapter.add(stringResource);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequestHandlerServiceClient == null) {
            mRequestHandlerServiceClient = new RequestHandlerServiceClient(getActivity());
        }

        mRequestHandlerServiceClient.setServiceClientListener(mServiceClientListener);
        mRequestHandlerServiceClient.connect(mRequestHandlerServiceClientToken);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mRequestHandlerServiceClient != null) {
            mRequestHandlerServiceClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(
                AbstractRequestHandler.KEY_CLIENT_TOKEN,
                mRequestHandlerServiceClientToken
        );
    }

    @Override
    public void onItemClick(
            AdapterView<?> parent,
            View view,
            int position,
            long id) {
        switch (mAdapter.getItem(position)) {
            case R.string.button_alert_dialog:
                final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                        R.string.alert_dialog_title,
                        R.string.alert_dialog_message
                );
                alertDialogFragment.setOnAlertDialogListener(mOnAlertDialogListener);
                alertDialogFragment.show(
                        getFragmentManager(),
                        ALERT_DIALOG_FRAGMENT
                );

                break;
            case R.string.button_choose_action_dialog:
                final ChooseActionDialogFragment chooseActionDialogFragment = ChooseActionDialogFragment.newInstance(
                        R.string.choose_action_dialog_title,
                        Arrays.asList(
                                R.string.choose_action_dialog_action_1,
                                R.string.choose_action_dialog_action_2
                        )
                );
                chooseActionDialogFragment.setOnChooseActionDialogListener(mOnChooseActionDialogListener);
                chooseActionDialogFragment.show(
                        getFragmentManager(),
                        CHOOSE_ACTION_DIALOG_FRAGMENT
                );

                break;
            case R.string.button_comment_dialog:
                final CommentDialogFragment commentDialogFragment = CommentDialogFragment.newInstance(null);
                commentDialogFragment.setOnCommentDialogValidateListener(mOnCommentDialogValidateListener);
                commentDialogFragment.show(
                        getFragmentManager(),
                        COMMENT_DIALOG_FRAGMENT
                );

                break;
            case R.string.button_date_picker_dialog:
                final DateTimePickerDialogFragment datePickerDialogFragment = DateTimePickerDialogFragment.newInstance(
                        System.currentTimeMillis(),
                        false
                );
                datePickerDialogFragment.setOnCalendarSetListener(mOnCalendarSetListener);
                datePickerDialogFragment.show(
                        getFragmentManager(),
                        DATE_PICKER_DIALOG_FRAGMENT
                );

                break;
            case R.string.button_date_time_picker_dialog:
                final DateTimePickerDialogFragment dateTimePickerDialogFragment = DateTimePickerDialogFragment.newInstance(
                        System.currentTimeMillis(),
                        true
                );
                dateTimePickerDialogFragment.setOnCalendarSetListener(mOnCalendarSetListener);
                dateTimePickerDialogFragment.show(
                        getFragmentManager(),
                        DATE_TIME_PICKER_DIALOG_FRAGMENT
                );

                break;
            case R.string.button_progress_dialog:
                final Bundle data = new Bundle();
                data.putInt(
                        ProgressRequestHandler.KEY_PROGRESS_START,
                        0
                );
                data.putInt(
                        ProgressRequestHandler.KEY_PROGRESS_END,
                        15
                );

                mRequestHandlerServiceClient.send(
                        ProgressRequestHandler.class,
                        data
                );

                break;
        }
    }
}
