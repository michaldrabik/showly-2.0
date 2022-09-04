package com.michaldrabik.ui_discover.filters.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover.filters.genres.DiscoverFiltersGenresUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.genres.DiscoverFiltersGenresUiEvent.CloseFilters
import com.michaldrabik.ui_model.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiscoverFiltersGenresViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val genresState = MutableStateFlow<List<Genre>?>(null)
  private val loadingState = MutableStateFlow(false)

  init {
    loadGenres()
  }

  private fun loadGenres() {
    viewModelScope.launch {
      val settings = settingsRepository.load()
      genresState.value = settings.discoverFilterGenres.toList()
    }
  }

  fun saveGenres(genres: List<Genre>) {
    viewModelScope.launch {
      if (genres == genresState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      val settings = settingsRepository.load()
      settingsRepository.update(
        settings.copy(
          discoverFilterGenres = genres.toList(),
        )
      )
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    genresState,
    loadingState,
  ) { s1, s2 ->
    DiscoverFiltersGenresUiState(
      genres = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverFiltersGenresUiState()
  )
}
