package com.michaldrabik.ui_discover.filters.networks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover.filters.networks.DiscoverFiltersNetworksUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.networks.DiscoverFiltersNetworksUiEvent.CloseFilters
import com.michaldrabik.ui_model.Network
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiscoverFiltersNetworksViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val networksState = MutableStateFlow<List<Network>?>(null)
  private val loadingState = MutableStateFlow(false)

  init {
    loadNetworks()
  }

  private fun loadNetworks() {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      networksState.value = settings.discoverFilterNetworks.toList()
    }
  }

  fun saveNetworks(networks: List<Network>) {
    viewModelScope.launch {
      if (networks == networksState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      val settings = settingsRepository.load()
      settingsRepository.update(
        settings.copy(
          discoverFilterNetworks = networks.toList(),
        )
      )
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    networksState,
    loadingState,
  ) { s1, s2 ->
    DiscoverFiltersNetworksUiState(
      networks = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverFiltersNetworksUiState()
  )
}
