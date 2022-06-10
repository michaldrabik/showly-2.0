package com.michaldrabik.ui_base.network

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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusProvider @Inject constructor(
  private val connectivityManager: ConnectivityManager
) : DefaultLifecycleObserver {

  private val _status = MutableStateFlow(false)
  val status = _status.asStateFlow()

  private val availableNetworksIds = mutableListOf<String>()

  fun isOnline() = status.value

  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)
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

    Timber.d("Registering network observer.")
  }

  override fun onStop(owner: LifecycleOwner) {
    connectivityManager.unregisterNetworkCallback(networkCallback)
    availableNetworksIds.clear()
    Timber.d("Unregistered network observer.")
    super.onStop(owner)
  }

  private val networkCallback = object : NetworkCallbackAdapter() {
    override fun onAvailable(network: Network) {
      availableNetworksIds.add(network.toString())
      _status.update { true }
      Timber.d("Network available: $network")
    }

    override fun onLost(network: Network) {
      availableNetworksIds.remove(network.toString())
      if (availableNetworksIds.isEmpty()) {
        _status.update { false }
      }
      Timber.d("Network lost: $network. Available networks: ${availableNetworksIds.size}")
    }

    override fun onUnavailable() {
      _status.update { false }
      Timber.d("Network unavailable")
    }
  }
}
