package com.michaldrabik.showly2.utilities

import android.app.Activity
import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_VPN
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_VPN
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import com.michaldrabik.showly2.App
import com.michaldrabik.showly2.ui.main.MainActivity
import timber.log.Timber

class NetworkMonitorCallbacks(
  private val connectivityManager: ConnectivityManager
) : Application.ActivityLifecycleCallbacks {

  private var foregroundActivity: Activity? = null
  private val availableNetworksIds = mutableListOf<String>()

  override fun onActivityStarted(activity: Activity) {
    if (activity !is MainActivity) return

    foregroundActivity = activity
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(TRANSPORT_WIFI)
      .addTransportType(TRANSPORT_CELLULAR)
      .addTransportType(TRANSPORT_ETHERNET)
      .addTransportType(TRANSPORT_VPN)
      .addCapability(NET_CAPABILITY_INTERNET)
      .removeCapability(NET_CAPABILITY_NOT_VPN)
      .build()

    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      connectivityManager.requestNetwork(networkRequest, networkCallback, 1000)
    }

    Timber.d("Registering network callback.")
  }

  override fun onActivityStopped(activity: Activity) {
    if (activity !is MainActivity) return

    connectivityManager.unregisterNetworkCallback(networkCallback)
    availableNetworksIds.clear()
    foregroundActivity = null

    Timber.d("Unregistering network callback.")
  }

  private val networkCallback = object : NetworkCallbackAdapter() {
    override fun onAvailable(network: Network) {
      availableNetworksIds.add(network.toString())
      foregroundActivity?.let {
        (it.applicationContext as App).isAppOnline = true
        (it as? NetworkObserver)?.onNetworkAvailableListener(true)
      }
      Timber.d("Network available: $network")
    }

    override fun onLost(network: Network) {
      availableNetworksIds.remove(network.toString())
      if (availableNetworksIds.isEmpty()) {
        foregroundActivity?.let {
          (it.applicationContext as App).isAppOnline = false
          (it as? NetworkObserver)?.onNetworkAvailableListener(false)
        }
      }
      Timber.d("Network lost: $network. Available networks: ${availableNetworksIds.size}")
    }

    override fun onUnavailable() {
      foregroundActivity?.let {
        (it.applicationContext as App).isAppOnline = false
        (it as? NetworkObserver)?.onNetworkAvailableListener(false)
      }
      Timber.d("Network unavailable")
    }
  }

  override fun onActivityPaused(p0: Activity) = Unit
  override fun onActivityDestroyed(p0: Activity) = Unit
  override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit
  override fun onActivityCreated(p0: Activity, p1: Bundle?) = Unit
  override fun onActivityResumed(p0: Activity) = Unit
}
