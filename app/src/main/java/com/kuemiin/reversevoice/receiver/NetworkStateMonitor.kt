package com.kuemiin.reversevoice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData
import com.kuemiin.reversevoice.utils.isAndroid13

class NetworkStateMonitor(private var mContext: Context) : BroadcastReceiver() {
    private var mIsUp: Boolean

    companion object{
        @Volatile
        var isConnectNetwork : MutableLiveData<Boolean?> = MutableLiveData()

        fun isOnline(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
            return false
        }
    }

    /**
     * call this when finished with it, and no later than onStop(): callback will crash if app has been destroyed
     */
    fun unregister() {
        mContext.unregisterReceiver(this)
    }

    /*
        * can be called at any time
        */
    private val isUp: Boolean
        get() {
            val connectivityManager =
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo

            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
        }

    /**
     * registerReceiver callback, passed to mListener
     */
    override fun onReceive(context: Context, intent: Intent) {
        val upNow = isUp
        if (upNow == mIsUp) return  // no change
        mIsUp = upNow
        isConnectNetwork.postValue(mIsUp)
    }

    init {
        //mListener = (Listener)context;
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        if(isAndroid13()){
            mContext.registerReceiver(this, intentFilter, RECEIVER_EXPORTED)
        }else{
            mContext.registerReceiver(this, intentFilter)
        }
        mIsUp = isUp
        isConnectNetwork.postValue(mIsUp)

    }


}