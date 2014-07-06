package com.makina.ecrins.commons.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses a <code>BroadcastReceiver</code> implementation that provides network connectivity state
 * information, independent of network type (mobile, Wi-Fi, etc.).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class NetworkConnectivityListener {

    private Context mContext;
    protected Handler mHandler;
    protected int mWhat;

    /**
     * Network connectivity information
     */
    protected NetworkInfo mNetworkInfo;
    protected State mState;
    protected AtomicBoolean mListening = new AtomicBoolean();
    protected String mReason;

    private ConnectivityManager mConnectivityManager;
    private ConnectivityBroadcastReceiver mReceiver;

    public NetworkConnectivityListener(Context pContext) {
        mContext = pContext;
        mState = State.UNKNOWN;
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mReceiver = new ConnectivityBroadcastReceiver();
    }

    /**
     * Gets the current state of this listener
     *
     * @return <code>true</code> if this instance is listening network activity change or not
     */
    public boolean isListening() {
        return mListening.get();
    }

    public State getState() {
        return mState;
    }

    public String getReason() {
        return mReason;
    }

    /**
     * This method starts listening for network connectivity state changes.
     */
    public synchronized void startListening(Handler pHandler, int pWhat) {
        if (!mListening.get()) {
            mHandler = pHandler;
            mWhat = pWhat;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mReceiver, filter);
            mListening.set(true);
        }
    }

    /**
     * This method stops this class from listening for network changes.
     */
    public synchronized void stopListening() {
        if (mListening.get()) {
            mContext.unregisterReceiver(mReceiver);
            mNetworkInfo = null;
            mReason = null;
            mListening.set(false);
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || !mListening.get()) {
                Log.w(getClass().getName(), "onReceive() called with " + intent + ", status : " + mState.toString());
                return;
            }

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                mState = State.DISCONNECTED;
            }
            else {
                mState = State.CONNECTED;
            }

            mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            Message message = mHandler.obtainMessage(mWhat);
            message.sendToTarget();

            Log.d(getClass().getName(), "onReceive : " + mState.toString());
        }
    }
}
