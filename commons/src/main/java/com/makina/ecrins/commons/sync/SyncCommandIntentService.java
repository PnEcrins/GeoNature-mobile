package com.makina.ecrins.commons.sync;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Executes a list of commands from a given {@link android.content.Intent} :
 * <ul>
 * <li>INTENT_PACKAGE_INFO : Gets application information and export as a JSON file (version_<application package name>.json).</li>
 * <li>INTENT_DELETE_INPUT : Deletes a given {@link com.makina.ecrins.commons.input.AbstractInput} file.</li>
 * <li>INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE : Moves a given file to external storage directory used by this context.</li>
 * </ul>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("Registered")
public class SyncCommandIntentService
        extends IntentService {

    /**
     * Command to execute to get application information and export it as a JSON file
     */
    public static final String INTENT_PACKAGE_INFO = "INTENT_PACKAGE_INFO";

    /**
     * Command to execute to delete a given {@link com.makina.ecrins.commons.input.AbstractInput} file.
     */
    public static final String INTENT_DELETE_INPUT = "INTENT_DELETE_INPUT";

    /**
     * Command to execute to move a given file to external storage directory used by this context
     * according to {@link com.makina.ecrins.commons.util.FileUtils#getExternalStorageDirectory()}.
     */
    public static final String INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE = "INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE";

    public static final String INTENT_EXTRA_FILE = "file";

    public SyncCommandIntentService() {

        super(SyncCommandIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction()
                  .equals(INTENT_PACKAGE_INFO)) {
            new PackageInfoAsyncTask(this).execute();
        }
        else if (intent.getAction()
                       .equals(INTENT_DELETE_INPUT)) {
            if (intent.hasExtra(INTENT_EXTRA_FILE)) {
                new DeleteInputAsyncTask(this).execute(intent.getExtras()
                                                             .getString(INTENT_EXTRA_FILE));
            }
            else {
                Log.w(getClass().getName(),
                      "missing key '" + INTENT_EXTRA_FILE + "' for intent '" + INTENT_DELETE_INPUT + "'");
            }
        }
        else if (intent.getAction()
                       .equals(INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE)) {
            if (intent.hasExtra(INTENT_EXTRA_FILE)) {
                new MoveFileToExternalStorageAsyncTask(this).execute(intent.getExtras()
                                                                           .getString(INTENT_EXTRA_FILE));
            }
            else {
                Log.w(getClass().getName(),
                      "missing key '" + INTENT_EXTRA_FILE + "' for intent '" + INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE + "'");
            }
        }
        else {
            Log.w(getClass().getName(),
                  "no action defined for intent '" + intent.getAction() + "'");
        }
    }

    private static class PackageInfoAsyncTask
            extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        private PackageInfoAsyncTask(final Context pContext) {

            super();

            this.mContext = pContext;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(getClass().getName(),
                  "doInBackground");

            PackageManager pm = this.mContext.getPackageManager();

            try {
                PackageInfo pi = pm.getPackageInfo(this.mContext.getPackageName(),
                                                   0);

                JSONObject packageInfoJson = new JSONObject();
                packageInfoJson.put("package",
                                    this.mContext.getPackageName());
                packageInfoJson.put("sharedUserId",
                                    pm.getPackageInfo(this.mContext.getPackageName(),
                                                      PackageManager.GET_META_DATA).sharedUserId);
                packageInfoJson.put("versionCode",
                                    pi.versionCode);
                packageInfoJson.put("versionName",
                                    pi.versionName);

                FileUtils.writeStringToFile(FileUtils.getFile(FileUtils.getRootFolder(this.mContext,
                                                                                      MountPoint.StorageType.INTERNAL),
                                                              "version_" + this.mContext.getPackageName() + ".json"),
                                            packageInfoJson.toString());
            }
            catch (NameNotFoundException | IOException | JSONException ge) {
                Log.e(getClass().getName(),
                      ge.getMessage(),
                      ge);
            }

            return null;
        }
    }

    private static class DeleteInputAsyncTask
            extends AsyncTask<String, Void, Void> {

        private Context mContext;

        private DeleteInputAsyncTask(final Context pContext) {

            super();

            this.mContext = pContext;
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                File inputDir = FileUtils.getInputsFolder(mContext);
                File inputFile = new File(inputDir,
                                          params[0]);

                Log.d(getClass().getName(),
                      "input to delete : " + params[0]);

                // noinspection ResultOfMethodCallIgnored
                inputFile.delete();
            }
            catch (IOException ioe) {
                Log.e(getClass().getName(),
                      ioe.getMessage(),
                      ioe);
            }

            return null;
        }
    }

    private static class MoveFileToExternalStorageAsyncTask
            extends AsyncTask<String, Void, Void> {

        private Context mContext;

        private MoveFileToExternalStorageAsyncTask(final Context pContext) {

            super();

            this.mContext = pContext;
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                StringBuilder relativePath = new StringBuilder();

                if (params[0].lastIndexOf("/") != -1) {
                    relativePath.append(params[0].substring(0,
                                                            params[0].lastIndexOf("/")));
                }

                File fileToMove = FileUtils.getFile(Environment.getExternalStorageDirectory(),
                                                    FileUtils.getRelativeSharedPath(mContext) + params[0]);

                if (fileToMove.exists() && !Environment.getExternalStorageDirectory()
                                                       .getAbsolutePath()
                                                       .equals(FileUtils.getExternalStorageDirectory()
                                                                        .getAbsolutePath())) {
                    FileUtils.deleteQuietly(FileUtils.getFile(FileUtils.getExternalStorageDirectory(),
                                                              FileUtils.getRelativeSharedPath(mContext) + params[0]));
                    FileUtils.moveFileToDirectory(fileToMove,
                                                  FileUtils.getFile(FileUtils.getExternalStorageDirectory(),
                                                                    FileUtils.getRelativeSharedPath(mContext),
                                                                    relativePath.toString()),
                                                  true);
                }
            }
            catch (NameNotFoundException | IOException ge) {
                Log.e(getClass().getName(),
                      ge.getMessage(),
                      ge);
            }

            return null;
        }
    }
}
