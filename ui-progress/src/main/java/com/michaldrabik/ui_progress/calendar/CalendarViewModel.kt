package com.michaldrabik.ui_progress.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.calendar.cases.CalendarRatingsCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarFutureCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarRecentsCase
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import com.michaldrabik.ui_progress.main.EpisodeCheckActionUiEvent
import com.michaldrabik.ui_progress.main.ProgressMainUiState
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
class CalendarViewModel @Inject constructor(
  private val recentsCase: CalendarRecentsCase,
  private val futureCase: CalendarFutureCase,
  private val ratingsCase: CalendarRatingsCase,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var loadItemsJob: Job? = null
  private var loadTranslationJobs: MutableSet<IdTrakt> = mutableSetOf()

  private val itemsState = MutableStateFlow<List<CalendarListItem>?>(null)
  private val modeState = MutableStateFlow(CalendarMode.PRESENT_FUTURE)

  private var mode = CalendarMode.PRESENT_FUTURE
  private var searchQuery: String? = null
  private var timestamp = 0L

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

  fun onEpisodeChecked(episode: CalendarListItem.Episode) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode.episode, episode.season, episode.show)
      val isQuickRate = ratingsCase.isQuickRateEnabled()
      eventChannel.send(EpisodeCheckActionUiEvent(bundle, isQuickRate))
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
    val showId = item.show.ids.trakt
    val language = translationsRepository.getLanguage()
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE || loadTranslationJobs.contains(showId)) {
      return
    }
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Timber.e(error)
      } finally {
        loadTranslationJobs.remove(showId)
      }
    }
    loadTranslationJobs.add(showId)
  }

  private fun updateItem(new: CalendarListItem.Episode) {
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
    modeState.value = mode
  }

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
}
