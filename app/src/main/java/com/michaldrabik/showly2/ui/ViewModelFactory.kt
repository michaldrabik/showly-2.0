package com.michaldrabik.showly2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.ui.discover.DiscoverInteractor
import com.michaldrabik.showly2.ui.discover.DiscoverViewModel
import com.michaldrabik.showly2.ui.search.SearchViewModel
import com.michaldrabik.showly2.ui.shows.ShowDetailsInteractor
import com.michaldrabik.showly2.ui.shows.ShowDetailsViewModel
import javax.inject.Inject

@AppScope
class ViewModelFactory @Inject constructor(
  private val discoverInteractor: DiscoverInteractor,
  private val showDetailsInteractor: ShowDetailsInteractor,
  private val uiCache: UiCache
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>) = when {
    modelClass.isAssignableFrom(DiscoverViewModel::class.java) ->
      DiscoverViewModel(discoverInteractor, uiCache) as T

    modelClass.isAssignableFrom(ShowDetailsViewModel::class.java) ->
      ShowDetailsViewModel(showDetailsInteractor) as T

    modelClass.isAssignableFrom(SearchViewModel::class.java) ->
      SearchViewModel() as T

    else -> throw IllegalStateException("Unknown ViewModel class")
  }
}
