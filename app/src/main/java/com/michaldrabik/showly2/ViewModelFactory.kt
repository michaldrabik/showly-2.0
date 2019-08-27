package com.michaldrabik.showly2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.discover.DiscoverViewModel

class ViewModelFactory(
  private val cloud: Cloud
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>) = when {
    modelClass.isAssignableFrom(DiscoverViewModel::class.java) ->
      DiscoverViewModel(cloud) as T
    else -> throw IllegalStateException("Unknown ViewModel class")
  }
}
