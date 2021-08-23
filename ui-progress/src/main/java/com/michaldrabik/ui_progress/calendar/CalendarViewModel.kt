package com.michaldrabik.ui_progress.calendar

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.cases.CalendarRatingsCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarFutureCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarRecentsCase
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import com.michaldrabik.ui_progress.main.ProgressMainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
  private val recentsCase: CalendarRecentsCase,
  private val futureCase: CalendarFutureCase,
  private val ratingsCase: CalendarRatingsCase,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<CalendarListItem>?>(null)
  private val modeState = MutableStateFlow(CalendarMode.PRESENT_FUTURE)

  val uiState = combine(
    itemsState,
    modeState
  ) { s1, s2 ->
    CalendarUiState(
      items = s1,
      mode = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CalendarUiState()
  )

  private val language by lazy { translationsRepository.getLanguage() }
  private var mode = CalendarMode.PRESENT_FUTURE
  private var searchQuery: String? = null
  private var timestamp = 0L
  var isQuickRateEnabled = false

  fun handleParentAction(state: ProgressMainUiState) {
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
    viewModelScope.launch {
      val items = when (mode) {
        CalendarMode.PRESENT_FUTURE -> futureCase.loadItems(searchQuery ?: "")
        CalendarMode.RECENTS -> recentsCase.loadItems(searchQuery ?: "")
      }
      itemsState.value = items
      modeState.value = mode
    }
  }

  fun addRating(rating: Int, bundle: EpisodeBundle) {
    viewModelScope.launch {
      try {
        ratingsCase.addRating(bundle.episode, rating)
        _messageState.emit(MessageEvent.info(R.string.textRateSaved))
        Analytics.logEpisodeRated(bundle.show.traktId, bundle.episode, rating)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
      }
    }
  }

  fun findMissingImage(item: CalendarListItem, force: Boolean) {
    check(item is CalendarListItem.Episode)
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }

  fun findMissingTranslation(item: CalendarListItem) {
    check(item is CalendarListItem.Episode)
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "CalendarViewModel::findMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: CalendarListItem.Episode) {
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
    modeState.value = mode
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      isQuickRateEnabled = ratingsCase.isQuickRateEnabled()
    }
  }
}
