package com.geonature.mobile.commons.input;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.geonature.mobile.commons.BuildConfig;
import com.geonature.mobile.commons.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Custom {@code IntentService} to read, save or export as JSON file a given {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractInputIntentService
        extends IntentService
        implements InputJsonReader.OnInputJsonReaderListener,
                   InputJsonWriter.OnInputJsonWriterListener {

    private static final String TAG = AbstractInputIntentService.class.getName();

    private static final String BROADCAST_ACTION = "BROADCAST_ACTION";

    static final String ACTION_READ = "ACTION_READ";
    static final String ACTION_SAVE = "ACTION_SAVE";
    static final String ACTION_DELETE = "ACTION_DELETE";
    static final String ACTION_EXPORT = "ACTION_EXPORT";

    private static final String EXTRA_DATE_FORMAT = "EXTRA_DATE_FORMAT";
    public static final String EXTRA_STATUS = "EXTRA_STATUS";
    public static final String EXTRA_INPUT = "EXTRA_INPUT";

    static final String KEY_PREFERENCE_CURRENT_INPUT = "KEY_PREFERENCE_CURRENT_INPUT";

    private final InputJsonReader mInputJsonReader;
    private final InputJsonWriter mInputJsonWriter;

    public static void readInput(@NonNull final Context context,
                                 @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                 @NonNull final String broadcastAction,
                                 @NonNull final String dateFormat) {
        context.startService(buildIntent(context,
                                         clazz,
                                         ACTION_READ,
                                         broadcastAction,
                                         dateFormat,
                                         null));
    }

    public static void saveInput(@NonNull final Context context,
                                 @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                 @NonNull final String broadcastAction,
                                 @NonNull final String dateFormat,
                                 @Nullable final AbstractInput input) {
        context.startService(buildIntent(context,
                                         clazz,
                                         ACTION_SAVE,
                                         broadcastAction,
                                         dateFormat,
                                         input));
    }

    public static void deleteInput(@NonNull final Context context,
                                   @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                   @NonNull final String broadcastAction) {
        context.startService(buildIntent(context,
                                         clazz,
                                         ACTION_DELETE,
                                         broadcastAction,
                                         null,
                                         null));
    }

    public static void exportInput(@NonNull final Context context,
                                   @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                   @NonNull final String broadcastAction,
                                   @NonNull final String dateFormat,
                                   @Nullable final AbstractInput input) {
        context.startService(buildIntent(context,
                                         clazz,
                                         ACTION_EXPORT,
                                         broadcastAction,
                                         dateFormat,
                                         input));
    }

    @NonNull
    static Intent buildIntent(@NonNull final Context context,
                              @NonNull final Class<? extends AbstractInputIntentService> clazz,
                              @NonNull final String action,
                              @NonNull final String broadcastAction,
                              @Nullable final String dateFormat,
                              @Nullable final AbstractInput input) {
        final Intent intent = new Intent(context,
                                         clazz);
        intent.setAction(action);
        intent.putExtra(BROADCAST_ACTION,
                        broadcastAction);

        if (!TextUtils.isEmpty(dateFormat)) {
            intent.putExtra(EXTRA_DATE_FORMAT,
                            dateFormat);
        }

        if (input != null) {
            intent.putExtra(EXTRA_INPUT,
                            input);
        }

        return intent;
    }

    public AbstractInputIntentService() {
        super("AbstractInputIntentService");

        mInputJsonReader = new InputJsonReader(this);
        mInputJsonWriter = new InputJsonWriter(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.w(TAG,
                  "onHandleIntent: null intent");

            return;
        }

        final String dateFormat = intent.getStringExtra(EXTRA_DATE_FORMAT);

        if (!TextUtils.isEmpty(dateFormat)) {
            mInputJsonReader.setDateFormat(dateFormat);
            mInputJsonWriter.setDateFormat(dateFormat);
        }

        final String broadcastAction = intent.getStringExtra(BROADCAST_ACTION);

        if (TextUtils.isEmpty(broadcastAction)) {
            Log.w(TAG,
                  "onHandleIntent: no broadcast action defined");

            return;
        }

        final String action = intent.getAction();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onHandleIntent, action: " + action);
        }

        if (action == null) {
            return;
        }

        switch (action) {
            case ACTION_READ:
                sendBroadcast(broadcastAction,
                              Status.STARTING);

                // read input as JSON from shared preferences
                final String json = PreferenceManager.getDefaultSharedPreferences(this)
                                                     .getString(KEY_PREFERENCE_CURRENT_INPUT,
                                                                null);

                if (TextUtils.isEmpty(json)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "onHandleIntent, " + action + ": no JSON found");
                    }

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_NOT_FOUND);
                }
                else {
                    final AbstractInput input = mInputJsonReader.read(json);

                    if (input == null) {
                        sendBroadcast(broadcastAction,
                                      Status.FINISHED_WITH_ERRORS);
                    }
                    else {
                        sendBroadcast(broadcastAction,
                                      Status.FINISHED,
                                      input);
                    }
                }

                break;
            case ACTION_SAVE:
                sendBroadcast(broadcastAction,
                              Status.STARTING);

                final AbstractInput inputToSave = intent.getParcelableExtra(EXTRA_INPUT);

                if (inputToSave == null) {
                    Log.w(TAG,
                          "onHandleIntent, " + action + ": no input to write");

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_WITH_ERRORS);
                }
                else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "onHandleIntent, " + action + ", input to save: " + inputToSave.getInputId());
                    }

                    final String inputAsJson = mInputJsonWriter.write(inputToSave);

                    if (TextUtils.isEmpty(inputAsJson)) {
                        sendBroadcast(broadcastAction,
                                      Status.FINISHED_WITH_ERRORS);
                    }
                    else {
                        PreferenceManager.getDefaultSharedPreferences(this)
                                         .edit()
                                         .putString(KEY_PREFERENCE_CURRENT_INPUT,
                                                    inputAsJson)
                                         .apply();

                        sendBroadcast(broadcastAction,
                                      Status.FINISHED,
                                      inputToSave);
                    }
                }

                break;
            case ACTION_DELETE:
                sendBroadcast(broadcastAction,
                              Status.STARTING);

                PreferenceManager.getDefaultSharedPreferences(this)
                                 .edit()
                                 .remove(KEY_PREFERENCE_CURRENT_INPUT)
                                 .apply();

                sendBroadcast(broadcastAction,
                              Status.FINISHED);

                break;
            case ACTION_EXPORT:
                sendBroadcast(broadcastAction,
                              Status.STARTING);

                final AbstractInput inputToExport = intent.getParcelableExtra(EXTRA_INPUT);

                if (inputToExport == null) {
                    Log.w(TAG,
                          "onHandleIntent, " + action + ": no input to write");

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_WITH_ERRORS);
                }
                else {
                    try {
                        mInputJsonWriter.write(getInputExportWriter(inputToExport),
                                               inputToExport);

                        PreferenceManager.getDefaultSharedPreferences(this)
                                         .edit()
                                         .remove(KEY_PREFERENCE_CURRENT_INPUT)
                                         .apply();

                        sendBroadcast(broadcastAction,
                                      Status.FINISHED,
                                      inputToExport);
                    }
                    catch (IOException ioe) {
                        sendBroadcast(broadcastAction,
                                      Status.FINISHED_WITH_ERRORS);
                    }
                }

                break;
        }
    }

    void sendBroadcast(@NonNull final String action,
                       @NonNull final Status status) {
        sendBroadcast(action,
                      status,
                      null);
    }

    void sendBroadcast(@NonNull final String action,
                       @NonNull final Status status,
                       @Nullable final AbstractInput input) {
        final Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(EXTRA_STATUS,
                        status);

        if (input != null) {
            intent.putExtra(EXTRA_INPUT,
                            input);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "sendBroadcast, action: " + action + ", status: " + status);
        }

        LocalBroadcastManager.getInstance(this)
                             .sendBroadcast(intent);
    }

    @NonNull
    Writer getInputExportWriter(@NonNull final AbstractInput input) throws
                                                                              IOException {
        final File inputDir = FileUtils.getInputsFolder(this);

        // noinspection ResultOfMethodCallIgnored
        inputDir.mkdirs();

        final File inputFile = new File(inputDir,
                                        "input_" + input.getInputId() + ".json");

        return new FileWriter(inputFile);
    }

    /**
     * The current status of {@link AbstractInputIntentService}.
     */
    public enum Status {

        /**
         * Indicates that the {@link AbstractInputIntentService} is starting an action.
         */
        STARTING,

        /**
         * Indicates that the {@link AbstractInputIntentService} has finished successfully.
         */
        FINISHED,

        /**
         * Indicates that the {@link AbstractInputIntentService} has finished successfully and no {@link AbstractInput} was found.
         */
        FINISHED_NOT_FOUND,

        /**
         * Indicates that the {@link AbstractInputIntentService} has finished with errors.
         */
        FINISHED_WITH_ERRORS
    }
}
