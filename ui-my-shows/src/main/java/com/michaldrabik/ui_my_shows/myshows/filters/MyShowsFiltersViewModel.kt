package com.michaldrabik.ui_my_shows.myshows.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_my_shows.myshows.filters.MyShowsFiltersUiEvent.ApplyFilters
import com.michaldrabik.ui_my_shows.myshows.filters.MyShowsFiltersUiEvent.CloseFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MyShowsFiltersViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val sectionState = MutableStateFlow<MyShowsSection?>(null)
  private val loadingState = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      sectionState.value = settingsRepository.filters.myShowsType
    }
  }

  fun applySectionType(sectionType: MyShowsSection) {
    viewModelScope.launch {
      if (sectionType == sectionState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      settingsRepository.filters.myShowsType = sectionType
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    sectionState,
    loadingState,
  ) { s1, s2 ->
    MyShowsFiltersUiState(
      sectionType = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MyShowsFiltersUiState()
  )
}
