package com.makina.ecrins.commons.input;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.makina.ecrins.commons.settings.ProtocolSettings;

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
 * @see AbstractInputIntentService
 */
public class InputHelper {

    private static final String TAG = InputHelper.class.getName();

    public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

    private final Context mContext;
    private final String mDateFormat;
    private final OnInputHelperListener mOnInputHelperListener;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,
                              Intent intent) {
            if ((intent == null) || (intent.getAction() == null)) {
                return;
            }

            final AbstractInputIntentService.Status status = (AbstractInputIntentService.Status) intent.getSerializableExtra(AbstractInputIntentService.EXTRA_STATUS);
            final AbstractInput input = intent.getParcelableExtra(AbstractInputIntentService.EXTRA_INPUT);

            if (status == null) {
                Log.w(TAG,
                      "onReceive, no status defined for action " + intent.getAction());

                return;
            }

            Log.d(TAG,
                  "onReceive, action: " + intent.getAction() + ", status: " + status);

            if (intent.getAction()
                      .equals(getBroadcastActionReadInput())) {
                switch (status) {
                    case FINISHED:
                        mInput = input;
                }

                mOnInputHelperListener.onReadInput(status);
            }

            if (intent.getAction()
                      .equals(getBroadcastActionSaveInput())) {
                mOnInputHelperListener.onSaveInput(status);
            }

            if (intent.getAction()
                      .equals(getBroadcastActionDeleteInput())) {
                mOnInputHelperListener.onDeleteInput(status);
            }

            if (intent.getAction()
                      .equals(getBroadcastActionExportInput())) {
                mOnInputHelperListener.onExportInput(status);
            }
        }
    };

    private Protocol mProtocol;
    private AbstractInput mInput;

    public InputHelper(@NonNull final Context context,
                       @NonNull final OnInputHelperListener onInputHelperListener) {
        this(context,
             null,
             DEFAULT_DATE_FORMAT,
             onInputHelperListener);
    }

    public InputHelper(@NonNull final Context context,
                       @Nullable final ProtocolSettings protocolSettings,
                       @NonNull final OnInputHelperListener onInputHelperListener) {
        this(context,
             protocolSettings,
             DEFAULT_DATE_FORMAT,
             onInputHelperListener);
    }

    public InputHelper(@NonNull final Context context,
                       @Nullable final ProtocolSettings protocolSettings,
                       @NonNull final String dateFormat,
                       @NonNull final OnInputHelperListener onInputHelperListener) {
        this.mContext = context;

        if (protocolSettings != null) {
            this.mProtocol = new Protocol(protocolSettings.getOrganism(),
                                          protocolSettings.getProtocol(),
                                          protocolSettings.getLot());
        }

        this.mDateFormat = dateFormat;
        this.mOnInputHelperListener = onInputHelperListener;
    }

    @NonNull
    public AbstractInput startInput() {
        mInput = mOnInputHelperListener.createInput();

        if (mInput.getInputId() == 0) {
            mInput.mInputId = generateId();
        }

        Log.d(TAG,
              "startInput, input: " + mInput.getInputId());

        return mInput;
    }

    @Nullable
    public AbstractInput getInput() {
        return mInput;
    }

    public void setInput(@Nullable final AbstractInput input) {
        this.mInput = input;
    }

    public void readInput() {
        mInput = null;
        AbstractInputIntentService.readInput(mContext,
                                             mOnInputHelperListener.getInputIntentServiceClass(),
                                             getBroadcastActionReadInput(),
                                             mDateFormat);
    }

    public void saveInput() {
        if (mInput == null) {
            Log.w(TAG,
                  "saveInput: no input to save");
        }

        AbstractInputIntentService.saveInput(mContext,
                                             mOnInputHelperListener.getInputIntentServiceClass(),
                                             getBroadcastActionSaveInput(),
                                             mDateFormat,
                                             mInput);
    }

    public void deleteInput() {
        mInput = null;
        AbstractInputIntentService.deleteInput(mContext,
                                               mOnInputHelperListener.getInputIntentServiceClass(),
                                               getBroadcastActionDeleteInput());
    }

    public void exportInput() {
        if (mInput == null) {
            Log.w(TAG,
                  "exportInput: no input to export");
        }

        mInput.setProtocol(mProtocol);

        AbstractInputIntentService.exportInput(mContext,
                                               mOnInputHelperListener.getInputIntentServiceClass(),
                                               getBroadcastActionExportInput(),
                                               mDateFormat,
                                               mInput);
    }

    public void resume() {
        LocalBroadcastManager.getInstance(mContext)
                             .registerReceiver(mBroadcastReceiver,
                                               new IntentFilter(getBroadcastActionReadInput()));
        LocalBroadcastManager.getInstance(mContext)
                             .registerReceiver(mBroadcastReceiver,
                                               new IntentFilter(getBroadcastActionSaveInput()));
        LocalBroadcastManager.getInstance(mContext)
                             .registerReceiver(mBroadcastReceiver,
                                               new IntentFilter(getBroadcastActionDeleteInput()));
        LocalBroadcastManager.getInstance(mContext)
                             .registerReceiver(mBroadcastReceiver,
                                               new IntentFilter(getBroadcastActionExportInput()));
    }

    public void dispose() {
        Log.d(TAG,
              "dispose");

        LocalBroadcastManager.getInstance(mContext)
                             .unregisterReceiver(mBroadcastReceiver);
    }

    @NonNull
    private String getBroadcastActionReadInput() {
        return mContext.getPackageName() + ".broadcast.input.read";
    }

    @NonNull
    private String getBroadcastActionSaveInput() {
        return mContext.getPackageName() + ".broadcast.input.save";
    }

    @NonNull
    private String getBroadcastActionDeleteInput() {
        return mContext.getPackageName() + ".broadcast.input.delete";
    }

    @NonNull
    private String getBroadcastActionExportInput() {
        return mContext.getPackageName() + ".broadcast.input.export";
    }

    /**
     * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2000, midnight.
     *
     * @return an unique ID
     */
    private long generateId() {
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

    /**
     * Callback used by {@link InputHelper}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnInputHelperListener {

        /**
         * Returns a new instance of {@link AbstractInput}.
         *
         * @return new instance of {@link AbstractInput}
         */
        @NonNull
        AbstractInput createInput();

        @NonNull
        Class<? extends AbstractInputIntentService> getInputIntentServiceClass();

        void onReadInput(@NonNull final AbstractInputIntentService.Status status);

        void onSaveInput(@NonNull final AbstractInputIntentService.Status status);

        void onDeleteInput(@NonNull final AbstractInputIntentService.Status status);

        void onExportInput(@NonNull final AbstractInputIntentService.Status status);
    }
}
