package com.michaldrabik.ui_progress_movies.progress

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainUiState
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesItemsCase
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesPinnedCase
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesSortCase
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressMoviesViewModel @Inject constructor(
  private val itemsCase: ProgressMoviesItemsCase,
  private val sortCase: ProgressMoviesSortCase,
  private val pinnedCase: ProgressMoviesPinnedCase,
  private val imagesProvider: MovieImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<ProgressMovieListItem.MovieItem>?>(null)
  private val scrollState = MutableStateFlow(ActionEvent(false))
  private val sortOrderState = MutableStateFlow<ActionEvent<SortOrder>?>(null)

  val uiState = combine(
    itemsState,
    scrollState,
    sortOrderState
  ) { s1, s2, s3 ->
    ProgressMoviesUiState(
      items = s1,
      scrollReset = s2,
      sortOrder = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ProgressMoviesUiState()
  )

  private val language by lazy { translationsRepository.getLanguage() }
  private var searchQuery: String? = null
  private var timestamp = 0L
  var isQuickRateEnabled = false

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

  private fun loadItems(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val items = itemsCase.loadItems(searchQuery ?: "")
      itemsState.value = items
      scrollState.value = ActionEvent(resetScroll)
    }
  }

  fun loadSortOrder() {
    if (itemsState.value?.isEmpty() == true) return
    viewModelScope.launch {
      val sortOrder = sortCase.loadSortOrder()
      sortOrderState.value = ActionEvent(sortOrder)
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
    if (item.translation != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.movie, language)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ProgressMoviesViewModel::findMissingTranslation()")
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortCase.setSortOrder(sortOrder)
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

  fun addRating(rating: Int, movie: Movie) {
    viewModelScope.launch {
      try {
        val token = userTraktManager.checkAuthorization().token
        ratingsRepository.movies.addRating(token, movie, rating)
        _messageState.emit(MessageEvent.info(R.string.textRateSaved))
        Analytics.logMovieRated(movie, rating)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
      }
    }
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      val isSignedIn = userTraktManager.isAuthorized()
      val isPremium = settingsRepository.isPremium
      val isQuickRate = settingsRepository.load().traktQuickRateEnabled
      isQuickRateEnabled = isPremium && isSignedIn && isQuickRate
    }
  }

  private fun updateItem(new: ProgressMovieListItem.MovieItem) {
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
    scrollState.value = ActionEvent(false)
  }
}
