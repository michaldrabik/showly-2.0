package com.michaldrabik.ui_discover_movies.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover_movies.filters.DiscoverMoviesFiltersUiEvent.ApplyFilters
import com.michaldrabik.ui_discover_movies.filters.DiscoverMoviesFiltersUiEvent.CloseFilters
import com.michaldrabik.ui_model.DiscoverFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiscoverMoviesFiltersViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val filtersState = MutableStateFlow<DiscoverFilters?>(null)
  private val loadingState = MutableStateFlow(false)

  init {
    loadFilters()
  }

  private fun loadFilters() {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      val filters = DiscoverFilters(
        feedOrder = settings.discoverMoviesFilterFeed,
        hideAnticipated = !settings.showAnticipatedMovies,
        hideCollection = !settings.showCollectionMovies,
        genres = settings.discoverMoviesFilterGenres.toList()
      )
      filtersState.value = filters
    }
  }

  fun saveFilters(filters: DiscoverFilters) {
    viewModelScope.launch {
      if (filters == filtersState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      val settings = settingsRepository.load()
      settingsRepository.update(
        settings.copy(
          discoverMoviesFilterFeed = filters.feedOrder,
          discoverMoviesFilterGenres = filters.genres,
          showAnticipatedMovies = !filters.hideAnticipated,
          showCollectionMovies = !filters.hideCollection
        )
      )
      Analytics.logDiscoverMoviesFiltersApply(filters)
      eventChannel.send(ApplyFilters(filters))
    }
  }

  val uiState = combine(
    filtersState,
    loadingState,
  ) { s1, s2 ->
    DiscoverMoviesFiltersUiState(
      filters = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverMoviesFiltersUiState()
  )
}
