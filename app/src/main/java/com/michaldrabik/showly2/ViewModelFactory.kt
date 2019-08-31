package com.michaldrabik.showly2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.ShowDetailsViewModel
import com.michaldrabik.showly2.ui.discover.DiscoverInteractor
import com.michaldrabik.showly2.ui.discover.DiscoverViewModel
import com.michaldrabik.storage.cache.ImagesUrlCache
import com.michaldrabik.storage.repository.UserRepository

class ViewModelFactory(
  private val cloud: Cloud,
  private val userRepository: UserRepository,
  private val imagesCache: ImagesUrlCache,
  private val discoverInteractor: DiscoverInteractor
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>) = when {
    modelClass.isAssignableFrom(DiscoverViewModel::class.java) ->
      DiscoverViewModel(cloud, userRepository, imagesCache, discoverInteractor) as T

    modelClass.isAssignableFrom(ShowDetailsViewModel::class.java) ->
      ShowDetailsViewModel(cloud, userRepository, imagesCache) as T

    else -> throw IllegalStateException("Unknown ViewModel class")
  }
}
