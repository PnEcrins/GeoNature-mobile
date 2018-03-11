package com.geonature.mobile.commons.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.geonature.mobile.commons.BuildConfig;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses a {@code BroadcastReceiver} implementation that provides network connectivity state
 * information, independent of network type (mobile, Wi-Fi, etc.).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class NetworkConnectivityListener {

    private static final String TAG = NetworkConnectivityListener.class.getName();

    private final Context mContext;

    private final AtomicBoolean mListening = new AtomicBoolean();
    private OnNetworkConnectivityChangeListener mOnNetworkConnectivityChangeListener;

    private final ConnectivityManager mConnectivityManager;
    private final ConnectivityBroadcastReceiver mReceiver;

    public NetworkConnectivityListener(Context pContext) {

        mContext = pContext;
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mReceiver = new ConnectivityBroadcastReceiver();
    }

    /**
     * Gets the current state of this listener
     *
     * @return {@code true} if this instance is listening network activity change or not
     */
    public boolean isListening() {

        return mListening.get();
    }

    /**
     * This method starts listening for network connectivity state changes.
     */
    public synchronized void startListening(final OnNetworkConnectivityChangeListener pOnNetworkConnectivityChangeListener) {

        if (!mListening.get()) {
            this.mOnNetworkConnectivityChangeListener = pOnNetworkConnectivityChangeListener;

            final IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mReceiver,
                                      filter);
            mListening.set(true);
        }
    }

    /**
     * This method stops this class from listening for network changes.
     */
    public synchronized void stopListening() {

        if (mListening.get()) {
            mContext.unregisterReceiver(mReceiver);
            mOnNetworkConnectivityChangeListener = null;
            mListening.set(false);
        }
    }

    /**
     * The callback used by {@link NetworkConnectivityListener}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public interface OnNetworkConnectivityChangeListener {

        /**
         * Invoked when the state of network connectivity change.
         */
        void onNetworkConnectivityChange(@Nullable final NetworkInfo networkInfo);
    }

    private class ConnectivityBroadcastReceiver
            extends BroadcastReceiver {

        @Override
        public void onReceive(Context context,
                              Intent intent) {
            final String action = intent.getAction();

            if (action == null) {
                Log.w(TAG, "no action defined for intent");

                return;
            }

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || !mListening.get()) {
                Log.w(TAG,
                      "onReceive() called with " + intent);

                return;
            }

            final NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onReceive: " + networkInfo.toString());
            }

            if (mOnNetworkConnectivityChangeListener != null) {
                mOnNetworkConnectivityChangeListener.onNetworkConnectivityChange(networkInfo);
            }
        }
    }
}
