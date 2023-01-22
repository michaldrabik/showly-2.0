package com.michaldrabik.ui_progress_movies.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress_movies.main.MovieCheckActionUiEvent
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainUiState
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesItemsCase
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesPinnedCase
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesSortCase
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProgressMoviesViewModel @Inject constructor(
  private val itemsCase: ProgressMoviesItemsCase,
  private val sortCase: ProgressMoviesSortCase,
  private val pinnedCase: ProgressMoviesPinnedCase,
  private val imagesProvider: MovieImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val workManager: WorkManager,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<ProgressMovieListItem>?>(null)
  private val scrollState = MutableStateFlow(Event(false))
  private val sortOrderState = MutableStateFlow<Event<Pair<SortOrder, SortType>>?>(null)
  private val overscrollState = MutableStateFlow(false)

  private var searchQuery: String? = null
  private var timestamp = 0L

  fun onParentState(state: ProgressMoviesMainUiState) {
    when {
      this.timestamp != state.timestamp && state.timestamp != 0L -> {
        this.timestamp = state.timestamp ?: 0L
        loadItems()
      }
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadItems(resetScroll = state.searchQuery.isNullOrBlank())
      }
    }
  }

  fun onMovieChecked(movie: Movie) {
    viewModelScope.launch {
      val isQuickRate = isQuickRateEnabled()
      eventChannel.send(MovieCheckActionUiEvent(movie, isQuickRate))
    }
  }

  private fun loadItems(resetScroll: Boolean = false) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      val items = itemsCase.loadItems(searchQuery ?: "")
      itemsState.value = items
      scrollState.value = Event(resetScroll)
      overscrollState.value = userTraktManager.isAuthorized() && items.isNotEmpty()
    }
  }

  fun findMissingImage(item: ProgressMovieListItem.MovieItem, force: Boolean) {
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }

  fun findMissingTranslation(item: ProgressMovieListItem.MovieItem) {
    val language = translationsRepository.getLanguage()
    if (item.translation != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.movie, language)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    viewModelScope.launch {
      sortCase.setSortOrder(sortOrder, sortType)
      loadItems(resetScroll = true)
    }
  }

  fun togglePinItem(item: ProgressMovieListItem.MovieItem) {
    if (item.isPinned) {
      pinnedCase.removePinnedItem(item.movie)
    } else {
      pinnedCase.addPinnedItem(item.movie)
    }
    loadItems(resetScroll = item.isPinned)
  }

  fun startTraktSync() {
    TraktSyncWorker.scheduleOneOff(
      workManager,
      isImport = true,
      isExport = true,
      isSilent = false
    )
  }

  private suspend fun isQuickRateEnabled(): Boolean {
    val isSignedIn = userTraktManager.isAuthorized()
    val isPremium = settingsRepository.isPremium
    val isQuickRate = settingsRepository.load().traktQuickRateEnabled
    return isPremium && isSignedIn && isQuickRate
  }

  private fun updateItem(new: ProgressMovieListItem.MovieItem) {
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it isSameAs new }
    itemsState.value = currentItems
    scrollState.value = Event(false)
  }

  val uiState = combine(
    itemsState,
    scrollState,
    sortOrderState,
    overscrollState
  ) { s1, s2, s3, s4 ->
    ProgressMoviesUiState(
      items = s1,
      scrollReset = s2,
      sortOrder = s3,
      isOverScrollEnabled = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ProgressMoviesUiState()
  )
}
