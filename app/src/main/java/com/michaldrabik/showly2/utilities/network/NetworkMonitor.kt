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

  companion object {
    private const val TAG = "NetworkMonitor"
  }

  var onNetworkAvailableCallback: ((Boolean) -> Unit)? = null
  private var availableNetworksIds = mutableListOf<String>()

  @OnLifecycleEvent(ON_START)
  fun monitorInternet() {
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(TRANSPORT_WIFI)
      .addTransportType(TRANSPORT_CELLULAR)
      .addTransportType(TRANSPORT_ETHERNET)
      .build()

    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    Log.d(TAG, "Registering network callback.")
  }

  @OnLifecycleEvent(ON_STOP)
  fun stopMonitoringInternet() {
    connectivityManager.unregisterNetworkCallback(networkCallback)
    Log.d(TAG, "Unregistering network callback.")
  }

  private val networkCallback = object : NetworkCallbackAdapter() {
    override fun onAvailable(network: Network) {
      Log.d(TAG, "Network available: $network")
      availableNetworksIds.add(network.toString())
      onNetworkAvailableCallback?.invoke(true)
    }

    override fun onLost(network: Network) {
      Log.d(TAG, "Network lost: $network")
      availableNetworksIds.remove(network.toString())
      if (availableNetworksIds.isEmpty()) {
        onNetworkAvailableCallback?.invoke(false)
      }
    }

    override fun onUnavailable() {
      Log.d(TAG, "Network unavailable")
      onNetworkAvailableCallback?.invoke(false)
    }
  }
}
