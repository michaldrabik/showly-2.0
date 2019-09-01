package com.michaldrabik.showly2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.discover.DiscoverInteractor
import com.michaldrabik.showly2.ui.discover.DiscoverViewModel
import com.michaldrabik.showly2.ui.shows.ShowDetailsInteractor
import com.michaldrabik.showly2.ui.shows.ShowDetailsViewModel
import com.michaldrabik.storage.repository.UserRepository

class ViewModelFactory(
  private val cloud: Cloud,
  private val userRepository: UserRepository,
  private val discoverInteractor: DiscoverInteractor,
  private val showDetailsInteractor: ShowDetailsInteractor
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>) = when {
    modelClass.isAssignableFrom(DiscoverViewModel::class.java) ->
      DiscoverViewModel(discoverInteractor) as T

    modelClass.isAssignableFrom(ShowDetailsViewModel::class.java) ->
      ShowDetailsViewModel(cloud, userRepository, showDetailsInteractor) as T

    else -> throw IllegalStateException("Unknown ViewModel class")
  }
}
