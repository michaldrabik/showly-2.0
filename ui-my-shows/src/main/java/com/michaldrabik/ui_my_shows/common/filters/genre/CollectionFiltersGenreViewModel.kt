package com.michaldrabik.ui_my_shows.common.filters.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Genre
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
internal class CollectionFiltersGenreViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val genresState = MutableStateFlow<List<Genre>?>(null)
  private val loadingState = MutableStateFlow(false)

  private lateinit var origin: CollectionFiltersOrigin

  fun loadData(origin: CollectionFiltersOrigin) {
    this.origin = origin
    genresState.value = when (origin) {
      MY_SHOWS -> settingsRepository.filters.myShowsGenres
      WATCHLIST_SHOWS -> settingsRepository.filters.watchlistShowsGenres
      HIDDEN_SHOWS -> settingsRepository.filters.hiddenShowsGenres
    }.toList()
  }

  fun saveGenres(genres: List<Genre>) {
    viewModelScope.launch {
      if (genres == genresState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      when (origin) {
        MY_SHOWS -> settingsRepository.filters.myShowsGenres = genres
        WATCHLIST_SHOWS -> settingsRepository.filters.watchlistShowsGenres = genres
        HIDDEN_SHOWS -> settingsRepository.filters.hiddenShowsGenres = genres
      }
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    genresState,
    loadingState,
  ) { s1, s2 ->
    CollectionFiltersGenreUiState(
      genres = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CollectionFiltersGenreUiState()
  )
}
