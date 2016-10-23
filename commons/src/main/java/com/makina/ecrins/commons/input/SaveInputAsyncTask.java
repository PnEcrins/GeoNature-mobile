package com.makina.ecrins.commons.input;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.makina.ecrins.commons.util.FileUtils;

import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * {@code AsyncTask} implementation to save current {@link AbstractInput} as {@code JSON} file.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @deprecated use {@link AbstractInputIntentService} instead
 */
@Deprecated
public class SaveInputAsyncTask
        extends AsyncTask<AbstractInput, Void, Boolean> {

    public static final int HANDLER_INPUT_SAVE_START = 0;
    public static final int HANDLER_INPUT_SAVED = 1;
    public static final int HANDLER_INPUT_SAVE_FAILED = 2;

    private Context mContext;
    private Handler mHandler;

    public SaveInputAsyncTask(Context pContext,
                              Handler pHandler) {

        super();
        this.mContext = pContext;
        this.mHandler = pHandler;
    }

    public void setHandler(Handler pHandler) {

        this.mHandler = pHandler;
    }

    @Override
    protected Boolean doInBackground(AbstractInput... params) {

        Message message = mHandler.obtainMessage(HANDLER_INPUT_SAVE_START);
        message.sendToTarget();

        if ((params != null) && (params.length > 0) && (params[0] != null)) {
            try {
                File inputDir = FileUtils.getInputsFolder(mContext);

                // noinspection ResultOfMethodCallIgnored
                inputDir.mkdirs();

                File inputFile = new File(inputDir,
                                          "input_" + params[0].getInputId() + ".json");

                FileWriter fileWriter = new FileWriter(inputFile);
                fileWriter.write(params[0].getJSONObject()
                                          .toString());
                fileWriter.flush();
                fileWriter.close();

                Log.d(getClass().getName(),
                      "input : " + params[0].getJSONObject()
                                            .toString());
            }
            catch (IOException ioe) {
                Log.e(getClass().getName(),
                      ioe.getMessage(),
                      ioe);
                return false;
            }
            catch (JSONException je) {
                Log.e(getClass().getName(),
                      je.getMessage(),
                      je);
                return false;
            }

            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            Message message = mHandler.obtainMessage(HANDLER_INPUT_SAVED);
            message.sendToTarget();
        }
        else {
            Message message = mHandler.obtainMessage(HANDLER_INPUT_SAVE_FAILED);
            message.sendToTarget();
        }
    }
}
