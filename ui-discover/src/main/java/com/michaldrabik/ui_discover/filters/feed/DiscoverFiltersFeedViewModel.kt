package com.michaldrabik.ui_discover.filters.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover.filters.feed.DiscoverFiltersFeedUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.feed.DiscoverFiltersFeedUiEvent.CloseFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiscoverFiltersFeedViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val feedOrderState = MutableStateFlow<DiscoverSortOrder?>(null)
  private val loadingState = MutableStateFlow(false)

  init {
    loadFilters()
  }

  private fun loadFilters() {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      feedOrderState.value = settings.discoverFilterFeed
    }
  }

  fun saveFeedOrder(feedOrder: DiscoverSortOrder) {
    viewModelScope.launch {
      if (feedOrder == feedOrderState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      val settings = settingsRepository.load()
      settingsRepository.update(
        settings.copy(discoverFilterFeed = feedOrder)
      )
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    feedOrderState,
    loadingState,
  ) { s1, s2 ->
    DiscoverFiltersFeedUiState(
      feedOrder = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverFiltersFeedUiState()
  )
}
