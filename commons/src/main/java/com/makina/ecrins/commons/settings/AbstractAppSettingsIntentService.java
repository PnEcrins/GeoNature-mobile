package com.makina.ecrins.commons.settings;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Custom {@code IntentService} to read as JSON file a given {@link AbstractAppSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractAppSettingsIntentService
        extends IntentService
        implements AppSettingsReader.OnAppSettingsReaderListener {

    private static final String TAG = AbstractAppSettingsIntentService.class.getSimpleName();

    private static final String BROADCAST_ACTION = "BROADCAST_ACTION";

    protected static final String ACTION_READ = "ACTION_READ";

    public static final String EXTRA_STATUS = "EXTRA_STATUS";
    public static final String EXTRA_FILENAME = "EXTRA_FILENAME";
    public static final String EXTRA_SETTINGS = "EXTRA_SETTINGS";

    private final AppSettingsReader mAppSettingsReader;

    public static void readSettings(@NonNull final Context context,
                                    @NonNull final Class<? extends AbstractAppSettingsIntentService> clazz,
                                    @NonNull final String broadcastAction,
                                    @NonNull final String filename) {
        context.startService(buildIntent(context,
                                         clazz,
                                         ACTION_READ,
                                         broadcastAction,
                                         filename));
    }

    @NonNull
    protected static Intent buildIntent(@NonNull final Context context,
                                        @NonNull final Class<? extends AbstractAppSettingsIntentService> clazz,
                                        @NonNull final String action,
                                        @NonNull final String broadcastAction,
                                        @NonNull final String filename) {
        final Intent intent = new Intent(context,
                                         clazz);
        intent.setAction(action);
        intent.putExtra(BROADCAST_ACTION,
                        broadcastAction);
        intent.putExtra(EXTRA_FILENAME,
                        filename);

        return intent;
    }

    public AbstractAppSettingsIntentService() {
        super("AbstractAppSettingsIntentService");

        mAppSettingsReader = new AppSettingsReader(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.w(TAG,
                  "onHandleIntent: null intent");

            return;
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
                              AbstractAppSettingsIntentService.Status.STARTING);

                final String filename = intent.getStringExtra(EXTRA_FILENAME);

                if (TextUtils.isEmpty(filename)) {
                    Log.w(TAG,
                          "onHandleIntent, " + intent.getAction() + ": no filename defined");

                    sendBroadcast(broadcastAction,
                                  AbstractAppSettingsIntentService.Status.FINISHED_WITH_ERRORS);
                }
                else {
                    Log.i(TAG,
                          "loading settings '" + filename + "'");

                    try {
                        final AbstractAppSettings appSettings = mAppSettingsReader.read(getAppSettingsReader(filename));

                        sendBroadcast(broadcastAction,
                                      Status.FINISHED,
                                      appSettings);
                    }
                    catch (FileNotFoundException fnfe) {
                        sendBroadcast(broadcastAction,
                                      Status.FINISHED_NOT_FOUND);
                    }
                    catch (IOException ioe) {
                        sendBroadcast(broadcastAction,
                                      AbstractAppSettingsIntentService.Status.FINISHED_WITH_ERRORS);
                    }
                }

                break;
        }
    }

    protected void sendBroadcast(@NonNull final String action,
                                 @NonNull final AbstractAppSettingsIntentService.Status status) {
        sendBroadcast(action,
                      status,
                      null);
    }

    protected void sendBroadcast(@NonNull final String action,
                                 @NonNull final AbstractAppSettingsIntentService.Status status,
                                 @Nullable final AbstractAppSettings appSettings) {
        final Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(EXTRA_STATUS,
                        status);

        if (appSettings != null) {
            intent.putExtra(EXTRA_SETTINGS,
                            appSettings);
        }

        Log.d(TAG,
              "sendBroadcast, action: " + action + ", status: " + status);

        LocalBroadcastManager.getInstance(this)
                             .sendBroadcast(intent);
    }

    @NonNull
    protected Reader getAppSettingsReader(@NonNull final String filename) throws
                                                                          IOException {
        // noinspection ResultOfMethodCallIgnored
        FileUtils.getRootFolder(this,
                                MountPoint.StorageType.INTERNAL)
                 .mkdirs();
        final File settingsJsonFile = FileUtils.getFile(FileUtils.getRootFolder(this,
                                                                                MountPoint.StorageType.INTERNAL),
                                                        filename);

        if (!settingsJsonFile.exists()) {
            throw new FileNotFoundException("unable to load settings from path '" + settingsJsonFile.getPath() + "'");
        }

        return new FileReader(settingsJsonFile);
    }

    /**
     * The current status of {@link AbstractAppSettingsIntentService}.
     */
    public enum Status {

        /**
         * Indicates that the {@link AbstractAppSettingsIntentService} is starting an action.
         */
        STARTING,

        /**
         * Indicates that the {@link AbstractAppSettingsIntentService} has finished successfully.
         */
        FINISHED,

        /**
         * Indicates that the {@link AbstractAppSettingsIntentService} has finished successfully and
         * no {@link AbstractAppSettings} was found.
         */
        FINISHED_NOT_FOUND,

        /**
         * Indicates that the {@link AbstractAppSettingsIntentService} has finished with errors.
         */
        FINISHED_WITH_ERRORS
    }
}
