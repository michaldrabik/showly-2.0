package com.michaldrabik.ui_progress_movies.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress_movies.calendar.cases.CalendarMoviesRatingsCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainUiState
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
class CalendarMoviesViewModel @Inject constructor(
  private val recentsCase: CalendarMoviesRecentsCase,
  private val futureCase: CalendarMoviesFutureCase,
  private val ratingsCase: CalendarMoviesRatingsCase,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : ViewModel() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<CalendarMovieListItem>?>(null)
  private val modeState = MutableStateFlow(CalendarMode.PRESENT_FUTURE)

  private var mode = CalendarMode.PRESENT_FUTURE
  private var searchQuery: String? = null
  private var timestamp = 0L
  var isQuickRateEnabled = false

  fun onParentState(state: ProgressMoviesMainUiState) {
    when {
      this.timestamp != state.timestamp && state.timestamp != 0L -> {
        this.timestamp = state.timestamp ?: 0L
        loadItems()
      }
      this.mode != state.calendarMode -> {
        this.mode = state.calendarMode ?: CalendarMode.PRESENT_FUTURE
        loadItems()
      }
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadItems()
      }
    }
  }

  private fun loadItems() {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      val items = when (mode) {
        CalendarMode.PRESENT_FUTURE -> futureCase.loadItems(searchQuery)
        CalendarMode.RECENTS -> recentsCase.loadItems(searchQuery)
      }
      itemsState.value = items
      modeState.value = mode
    }
  }

  fun findMissingImage(item: CalendarMovieListItem, force: Boolean) {
    check(item is CalendarMovieListItem.MovieItem)
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

  fun findMissingTranslation(item: CalendarMovieListItem) {
    check(item is CalendarMovieListItem.MovieItem)
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

  private fun updateItem(new: CalendarMovieListItem.MovieItem) {
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it isSameAs new }
    itemsState.value = currentItems
    modeState.value = mode
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      isQuickRateEnabled = ratingsCase.isQuickRateEnabled()
    }
  }

  val uiState = combine(
    itemsState,
    modeState
  ) { s1, s2 ->
    CalendarMoviesUiState(
      items = s1,
      mode = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CalendarMoviesUiState()
  )
}
