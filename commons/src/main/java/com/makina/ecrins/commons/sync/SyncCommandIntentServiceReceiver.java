package com.makina.ecrins.commons.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

/**
 * <code>BroadcastReceiver</code> implementation to start from a given intent {@link SyncCommandIntentService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SyncCommandIntentServiceReceiver
        extends BroadcastReceiver {

    private static final String TAG = SyncCommandIntentServiceReceiver.class.getName();

    @Override
    public void onReceive(
            Context context,
            Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onReceive " + intent);
        }

        if (intent.getAction()
                  .equals(context.getPackageName() + "." + SyncCommandIntentService.INTENT_PACKAGE_INFO)) {
            Intent startIntent = new Intent(context,
                                            SyncCommandIntentService.class);
            startIntent.setAction(SyncCommandIntentService.INTENT_PACKAGE_INFO);

            context.startService(startIntent);
        }
        else if (intent.getAction()
                       .equals(context.getPackageName() + "." + SyncCommandIntentService.INTENT_DELETE_INPUT)) {
            if (intent.hasExtra(context.getPackageName() + "." + SyncCommandIntentService.INTENT_EXTRA_FILE)) {
                Intent startIntent = new Intent(context,
                                                SyncCommandIntentService.class);
                startIntent.setAction(SyncCommandIntentService.INTENT_DELETE_INPUT);
                startIntent.putExtra(SyncCommandIntentService.INTENT_EXTRA_FILE,
                                     intent.getExtras()
                                           .getString(context.getPackageName() + "." + SyncCommandIntentService.INTENT_EXTRA_FILE));

                context.startService(startIntent);
            }
            else {
                Log.w(TAG,
                      "missing key '" + context.getPackageName() + "." + SyncCommandIntentService.INTENT_EXTRA_FILE + "' for intent '" + context.getPackageName() + "." + SyncCommandIntentService.INTENT_DELETE_INPUT + "'");
            }
        }
        else if (intent.getAction()
                       .equals(context.getPackageName() + "." + SyncCommandIntentService.INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE)) {
            if (intent.hasExtra(context.getPackageName() + "." + SyncCommandIntentService.INTENT_EXTRA_FILE)) {
                Intent startIntent = new Intent(context,
                                                SyncCommandIntentService.class);
                startIntent.setAction(SyncCommandIntentService.INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE);
                startIntent.putExtra(SyncCommandIntentService.INTENT_EXTRA_FILE,
                                     intent.getExtras()
                                           .getString(context.getPackageName() + "." + SyncCommandIntentService.INTENT_EXTRA_FILE));

                context.startService(startIntent);
            }
            else {
                Log.w(TAG,
                      "missing key '" + context.getPackageName() + "." + SyncCommandIntentService.INTENT_EXTRA_FILE + "' for intent '" + context.getPackageName() + "." + SyncCommandIntentService.INTENT_MOVE_FILE_TO_EXTERNAL_STORAGE + "'");
            }
        }
        else {
            Log.w(TAG,
                  "no action defined for intent '" + intent.getAction() + "'");
        }
    }
}
