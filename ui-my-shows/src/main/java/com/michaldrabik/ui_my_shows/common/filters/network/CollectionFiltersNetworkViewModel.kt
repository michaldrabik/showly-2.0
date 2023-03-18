package com.michaldrabik.ui_my_shows.common.filters.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Network
import com.michaldrabik.ui_my_shows.common.filters.CollectionFiltersOrigin
import com.michaldrabik.ui_my_shows.common.filters.CollectionFiltersOrigin.HIDDEN_SHOWS
import com.michaldrabik.ui_my_shows.common.filters.CollectionFiltersOrigin.MY_SHOWS
import com.michaldrabik.ui_my_shows.common.filters.CollectionFiltersOrigin.WATCHLIST_SHOWS
import com.michaldrabik.ui_my_shows.common.filters.CollectionFiltersUiEvent.ApplyFilters
import com.michaldrabik.ui_my_shows.common.filters.CollectionFiltersUiEvent.CloseFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CollectionFiltersNetworkViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val networksState = MutableStateFlow<List<Network>?>(null)
  private val loadingState = MutableStateFlow(false)

  private lateinit var origin: CollectionFiltersOrigin

  fun loadData(origin: CollectionFiltersOrigin) {
    this.origin = origin
    networksState.value = when (origin) {
      MY_SHOWS -> settingsRepository.filters.myShowsNetworks
      WATCHLIST_SHOWS -> settingsRepository.filters.watchlistShowsNetworks
      HIDDEN_SHOWS -> settingsRepository.filters.hiddenShowsNetworks
    }.toList()
  }

  fun saveNetworks(networks: List<Network>) {
    viewModelScope.launch {
      if (networks == networksState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      when (origin) {
        MY_SHOWS -> settingsRepository.filters.myShowsNetworks = networks
        WATCHLIST_SHOWS -> settingsRepository.filters.watchlistShowsNetworks = networks
        HIDDEN_SHOWS -> settingsRepository.filters.hiddenShowsNetworks = networks
      }
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    networksState,
    loadingState,
  ) { s1, s2 ->
    CollectionFiltersNetworkUiState(
      networks = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CollectionFiltersNetworkUiState()
  )
}
