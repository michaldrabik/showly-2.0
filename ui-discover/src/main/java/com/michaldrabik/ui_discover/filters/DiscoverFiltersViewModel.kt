package com.michaldrabik.ui_discover.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover.filters.DiscoverFiltersUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.DiscoverFiltersUiEvent.CloseFilters
import com.michaldrabik.ui_model.DiscoverFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiscoverFiltersViewModel @Inject constructor(
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
        feedOrder = settings.discoverFilterFeed,
        hideAnticipated = !settings.showAnticipatedShows,
        hideCollection = !settings.showCollectionShows,
        genres = settings.discoverFilterGenres.toList(),
        networks = settings.discoverFilterNetworks.toList()
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
          discoverFilterFeed = filters.feedOrder,
          discoverFilterGenres = filters.genres,
          discoverFilterNetworks = filters.networks,
          showAnticipatedShows = !filters.hideAnticipated,
          showCollectionShows = !filters.hideCollection
        )
      )
      Analytics.logDiscoverFiltersApply(filters)
      eventChannel.send(ApplyFilters(filters))
    }
  }

  val uiState = combine(
    filtersState,
    loadingState,
  ) { s1, s2 ->
    DiscoverFiltersUiState(
      filters = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverFiltersUiState()
  )
}
