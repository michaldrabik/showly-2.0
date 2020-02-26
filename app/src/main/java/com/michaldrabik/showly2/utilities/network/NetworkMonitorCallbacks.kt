package com.michaldrabik.showly2.utilities.network

import android.app.Activity
import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import android.os.Bundle
import timber.log.Timber

class NetworkMonitorCallbacks(
  private val connectivityManager: ConnectivityManager
) : Application.ActivityLifecycleCallbacks {

  private var foregroundActivity: Activity? = null
  private val availableNetworksIds = mutableListOf<String>()

  override fun onActivityStarted(activity: Activity) {
    foregroundActivity = activity
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(TRANSPORT_WIFI)
      .addTransportType(TRANSPORT_CELLULAR)
      .addTransportType(TRANSPORT_ETHERNET)
      .build()

    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    Timber.d("Registering network callback.")
  }

  override fun onActivityStopped(activity: Activity) {
    connectivityManager.unregisterNetworkCallback(networkCallback)
    availableNetworksIds.clear()
    foregroundActivity = null
    Timber.d("Unregistering network callback.")
  }

  private val networkCallback = object : NetworkCallbackAdapter() {
    override fun onAvailable(network: Network) {
      availableNetworksIds.add(network.toString())
      foregroundActivity?.let { (it as? NetworkObserver)?.onNetworkAvailableListener(true) }
      Timber.d("Network available: $network")
    }

    override fun onLost(network: Network) {
      availableNetworksIds.remove(network.toString())
      if (availableNetworksIds.isEmpty()) {
        foregroundActivity?.let { (it as? NetworkObserver)?.onNetworkAvailableListener(false) }
      }
      Timber.d("Network lost: $network. Available networks: ${availableNetworksIds.size}")
    }

    override fun onUnavailable() {
      foregroundActivity?.let { (it as? NetworkObserver)?.onNetworkAvailableListener(false) }
      Timber.d("Network unavailable")
    }
  }

  override fun onActivityPaused(p0: Activity) = Unit
  override fun onActivityDestroyed(p0: Activity) = Unit
  override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit
  override fun onActivityCreated(p0: Activity, p1: Bundle?) = Unit
  override fun onActivityResumed(p0: Activity) = Unit
}
