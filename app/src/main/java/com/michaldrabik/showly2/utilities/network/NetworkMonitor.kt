package com.michaldrabik.showly2.utilities.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber

class NetworkMonitor(private val connectivityManager: ConnectivityManager) : LifecycleObserver {

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
    Timber.d("Registering network callback.")
  }

  @OnLifecycleEvent(ON_STOP)
  fun stopMonitoringInternet() {
    connectivityManager.unregisterNetworkCallback(networkCallback)
    Timber.d("Unregistering network callback.")
  }

  private val networkCallback = object : NetworkCallbackAdapter() {
    override fun onAvailable(network: Network) {
      Timber.d("Network available: $network")
      availableNetworksIds.add(network.toString())
      onNetworkAvailableCallback?.invoke(true)
    }

    override fun onLost(network: Network) {
      Timber.d("Network lost: $network")
      availableNetworksIds.remove(network.toString())
      if (availableNetworksIds.isEmpty()) {
        onNetworkAvailableCallback?.invoke(false)
      }
    }

    override fun onUnavailable() {
      Timber.d("Network unavailable")
      onNetworkAvailableCallback?.invoke(false)
    }
  }
}
