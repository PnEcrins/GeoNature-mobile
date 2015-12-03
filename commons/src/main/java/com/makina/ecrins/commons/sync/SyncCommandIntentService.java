package com.makina.ecrins.commons.sync;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Executes a list of commands from a given {@code Intent} :
 * <ul>
 * <li>{@link #INTENT_PACKAGE_INFO}: Gets application information and export as a JSON file ({@code version_<application package name>.json}).</li>
 * <li>{@link #INTENT_DELETE_INPUT}: Deletes a given {@link com.makina.ecrins.commons.input.AbstractInput} file.</li>
 * <li>{@link #INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE}: Moves a given file to external storage directory used by this context.</li>
 * </ul>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("Registered")
public class SyncCommandIntentService
        extends IntentService {

    private static final String TAG = SyncCommandIntentService.class.getName();

    /**
     * Command to execute to get application information and export it as a JSON file
     */
    public static final String INTENT_PACKAGE_INFO = "INTENT_PACKAGE_INFO";

    /**
     * Command to execute to delete a given {@link com.makina.ecrins.commons.input.AbstractInput} file.
     */
    @Deprecated
    public static final String INTENT_DELETE_INPUT = "INTENT_DELETE_INPUT";

    /**
     * Command to execute to move a given file to external storage directory used by this context
     * according to {@link FileUtils#getExternalStorageDirectory()}.
     */
    @Deprecated
    public static final String INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE = "INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE";

    public static final String INTENT_EXTRA_FILE = "file";

    public SyncCommandIntentService() {

        super(SyncCommandIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        switch (intent.getAction()) {
            case INTENT_PACKAGE_INFO:
                exportPackageInfo();

                break;
            case INTENT_DELETE_INPUT:
                final String inputFilename = intent.getExtras()
                                                   .getString(INTENT_EXTRA_FILE);

                if (TextUtils.isEmpty(inputFilename)) {
                    Log.w(TAG,
                          "missing key '" + INTENT_EXTRA_FILE + "' for intent '" + intent.getAction() + "'");
                }
                else {
                    deleteInput(inputFilename);
                }

                break;
            case INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE:
                final String filename = intent.getExtras()
                                              .getString(INTENT_EXTRA_FILE);

                if (TextUtils.isEmpty(filename)) {
                    Log.w(TAG,
                          "missing key '" + INTENT_EXTRA_FILE + "' for intent '" + intent.getAction() + "'");
                }
                else {
                    moveFileToExternalStorage(filename);
                }

                break;
            default:
                Log.w(TAG,
                      "no action defined for intent '" + intent.getAction() + "'");
        }
    }

    private void exportPackageInfo() {

        PackageManager pm = getPackageManager();

        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(),
                                               0);

            JSONObject packageInfoJson = new JSONObject();
            packageInfoJson.put("package",
                                getPackageName());
            packageInfoJson.put("sharedUserId",
                                pm.getPackageInfo(getPackageName(),
                                                  PackageManager.GET_META_DATA).sharedUserId);
            packageInfoJson.put("versionCode",
                                pi.versionCode);
            packageInfoJson.put("versionName",
                                pi.versionName);

            FileUtils.writeStringToFile(FileUtils.getFile(FileUtils.getRootFolder(this,
                                                                                  MountPoint.StorageType.INTERNAL),
                                                          "version_" + getPackageName() + ".json"),
                                        packageInfoJson.toString());
        }
        catch (NameNotFoundException | IOException | JSONException ge) {
            Log.e(TAG,
                  ge.getMessage(),
                  ge);
        }
    }

    private void deleteInput(@NonNull final String filename) {

        try {
            File inputDir = FileUtils.getInputsFolder(this);
            File inputFile = new File(inputDir,
                                      filename);

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "input to delete : " + filename);
            }

            // noinspection ResultOfMethodCallIgnored
            inputFile.delete();
        }
        catch (IOException ioe) {
            Log.e(TAG,
                  ioe.getMessage(),
                  ioe);
        }
    }

    private void moveFileToExternalStorage(@NonNull final String filename) {

        try {
            StringBuilder relativePath = new StringBuilder();

            if (filename.lastIndexOf("/") != -1) {
                relativePath.append(filename.substring(0,
                                                       filename.lastIndexOf("/")));
            }

            File fileToMove = FileUtils.getFile(Environment.getExternalStorageDirectory(),
                                                FileUtils.getRelativeSharedPath(this) + filename);

            if (fileToMove.exists() && !Environment.getExternalStorageDirectory()
                                                   .getAbsolutePath()
                                                   .equals(FileUtils.getExternalStorageDirectory()
                                                                    .getAbsolutePath())) {
                FileUtils.deleteQuietly(FileUtils.getFile(FileUtils.getExternalStorageDirectory(),
                                                          FileUtils.getRelativeSharedPath(this) + filename));
                FileUtils.moveFileToDirectory(fileToMove,
                                              FileUtils.getFile(FileUtils.getExternalStorageDirectory(),
                                                                FileUtils.getRelativeSharedPath(this),
                                                                relativePath.toString()),
                                              true);
            }
        }
        catch (NameNotFoundException | IOException ge) {
            Log.e(TAG,
                  ge.getMessage(),
                  ge);
        }
    }
}
