package com.michaldrabik.showly2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.discover.DiscoverViewModel
import javax.inject.Inject

@AppScope
class ViewModelFactory @Inject constructor(
  private val cloud: Cloud
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>) = when {
    modelClass.isAssignableFrom(DiscoverViewModel::class.java) -> DiscoverViewModel(cloud) as T
    else -> throw IllegalStateException("Unknown ViewModel class")
  }
}
