package com.makina.ecrins.commons.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses a {@code BroadcastReceiver} implementation that provides network connectivity state
 * information, independent of network type (mobile, Wi-Fi, etc.).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class NetworkConnectivityListener {

    private Context mContext;

    protected final AtomicBoolean mListening = new AtomicBoolean();
    protected OnNetworkConnectivityChangeListener mOnNetworkConnectivityChangeListener;

    private ConnectivityManager mConnectivityManager;
    private ConnectivityBroadcastReceiver mReceiver;

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
     * The callback used by {@link com.makina.ecrins.commons.net.NetworkConnectivityListener}.
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
        public void onReceive(
                Context context,
                Intent intent) {

            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || !mListening.get()) {
                Log.w(getClass().getName(),
                      "onReceive() called with " + intent);

                return;
            }

            final NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(),
                      "onReceive: " + networkInfo.toString());
            }

            if (mOnNetworkConnectivityChangeListener != null) {
                mOnNetworkConnectivityChangeListener.onNetworkConnectivityChange(networkInfo);
            }
        }
    }
}
