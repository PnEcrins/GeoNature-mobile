package com.makina.ecrins.commons.input;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Custom {@code IntentService} to read, save or export as JSON file a given {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractInputIntentService
        extends IntentService
        implements InputJsonReader.OnInputJsonReaderListener,
                   InputJsonWriter.OnInputJsonWriterListener {

    private static final String TAG = AbstractInputIntentService.class.getSimpleName();

    private static final String BROADCAST_ACTION = "BROADCAST_ACTION";

    private static final String ACTION_READ = "ACTION_READ";
    private static final String ACTION_SAVE = "ACTION_SAVE";
    private static final String ACTION_EXPORT = "ACTION_EXPORT";

    private static final String EXTRA_DATE_FORMAT = "EXTRA_DATE_FORMAT";
    public static final String EXTRA_STATUS = "EXTRA_STATUS";
    public static final String EXTRA_INPUT = "EXTRA_INPUT";

    private static final String KEY_PREFERENCE_CURRENT_INPUT = "KEY_PREFERENCE_CURRENT_INPUT";

    private final InputJsonReader mInputJsonReader;
    private final InputJsonWriter mInputJsonWriter;

    public static void readInput(@NonNull final Context context,
                                 @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                 @NonNull final String broadcastAction,
                                 @NonNull final String dateFormat) {
        final Intent intent = new Intent(context,
                                         clazz);
        intent.setAction(ACTION_READ);
        intent.putExtra(BROADCAST_ACTION,
                        broadcastAction);
        intent.putExtra(EXTRA_DATE_FORMAT,
                        dateFormat);

        context.startService(intent);
    }

    public static void saveInput(@NonNull final Context context,
                                 @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                 @NonNull final String broadcastAction,
                                 @NonNull final String dateFormat,
                                 @NonNull final AbstractInput input) {
        final Intent intent = new Intent(context,
                                         clazz);
        intent.setAction(ACTION_SAVE);
        intent.putExtra(BROADCAST_ACTION,
                        broadcastAction);
        intent.putExtra(EXTRA_DATE_FORMAT,
                        dateFormat);
        intent.putExtra(EXTRA_INPUT,
                        input);

        context.startService(intent);
    }

    public static void exportInput(@NonNull final Context context,
                                   @NonNull final Class<? extends AbstractInputIntentService> clazz,
                                   @NonNull final String broadcastAction,
                                   @NonNull final String dateFormat,
                                   @NonNull final AbstractInput input) {
        final Intent intent = new Intent(context,
                                         clazz);
        intent.setAction(ACTION_EXPORT);
        intent.putExtra(BROADCAST_ACTION,
                        broadcastAction);
        intent.putExtra(EXTRA_DATE_FORMAT,
                        dateFormat);
        intent.putExtra(EXTRA_INPUT,
                        input);

        context.startService(intent);
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

        Log.d(TAG,
              "onHandleIntent, action: " + intent.getAction());

        switch (intent.getAction()) {
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
                              "onHandleIntent, " + intent.getAction() + ": no JSON found");
                    }

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_NOT_FOUND);

                    break;
                }

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

                break;
            case ACTION_SAVE:
                sendBroadcast(broadcastAction,
                              Status.STARTING);

                final AbstractInput inputToSave = intent.getParcelableExtra(EXTRA_INPUT);

                if (inputToSave == null) {
                    Log.w(TAG,
                          "onHandleIntent, " + intent.getAction() + ": no input to write");

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_WITH_ERRORS);

                    break;
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

                break;
            case ACTION_EXPORT:
                sendBroadcast(broadcastAction,
                              Status.STARTING);

                final AbstractInput inputToExport = intent.getParcelableExtra(EXTRA_INPUT);

                if (inputToExport == null) {
                    Log.w(TAG,
                          "onHandleIntent, " + intent.getAction() + ": no input to write");

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_WITH_ERRORS);

                    break;
                }

                try {
                    final File inputDir = FileUtils.getInputsFolder(this);

                    // noinspection ResultOfMethodCallIgnored
                    inputDir.mkdirs();

                    final File inputFile = new File(inputDir,
                                                    "input_" + inputToExport.getInputId() + ".json");

                    final FileWriter fileWriter = new FileWriter(inputFile);

                    mInputJsonWriter.write(fileWriter,
                                           inputToExport);

                    sendBroadcast(broadcastAction,
                                  Status.FINISHED,
                                  inputToExport);
                }
                catch (IOException ioe) {
                    sendBroadcast(broadcastAction,
                                  Status.FINISHED_WITH_ERRORS);
                }

                break;
        }
    }

    private void sendBroadcast(@NonNull final String action,
                               @NonNull final Status status) {
        sendBroadcast(action,
                      status,
                      null);
    }

    private void sendBroadcast(@NonNull final String action,
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

        LocalBroadcastManager.getInstance(this)
                             .sendBroadcast(intent);
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
