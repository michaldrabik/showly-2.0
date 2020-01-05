package com.michaldrabik.showly2.utilities.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class NetworkMonitor(private val connectivityManager: ConnectivityManager) : LifecycleObserver {

  var onNetworkAvailableCallback: ((Boolean) -> Unit)? = null

  @OnLifecycleEvent(ON_START)
  fun monitorInternet() {
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(TRANSPORT_WIFI)
      .addTransportType(TRANSPORT_CELLULAR)
      .addTransportType(TRANSPORT_ETHERNET)
      .build()

    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    Log.d("NetworkMonitor", "Registering network callback.")
  }

  @OnLifecycleEvent(ON_STOP)
  fun stopMonitoringInternet() {
    connectivityManager.unregisterNetworkCallback(networkCallback)
    Log.d("NetworkMonitor", "Unregistering network callback.")
  }

  private val networkCallback = object : NetworkCallbackAdapter() {
    override fun onAvailable(network: Network) {
      onNetworkAvailableCallback?.invoke(true)
    }

    override fun onLost(network: Network) {
      onNetworkAvailableCallback?.invoke(false)
    }

    override fun onUnavailable() {
      onNetworkAvailableCallback?.invoke(false)
    }
  }
}
