package com.michaldrabik.ui_progress.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.history.entities.HistoryListItem
import com.michaldrabik.ui_progress.history.usecases.GetHistoryItemsCase
import com.michaldrabik.ui_progress.main.ProgressMainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
  private val getHistoryItemsCase: GetHistoryItemsCase,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider
) : ViewModel() {

  private val initialState = HistoryUiState()

  private val itemsState = MutableStateFlow(initialState.items)
  private val loadingState = MutableStateFlow(initialState.isLoading)
  private val resetScrollEvent = MutableStateFlow(initialState.resetScrollEvent)

  private var itemsJob: Job? = null
  private var translationJobs: MutableSet<IdTrakt> = mutableSetOf()

  private var searchQuery: String? = null
  private var timestamp = 0L

  fun handleParentAction(state: ProgressMainUiState) {
    when {
      timestamp != state.timestamp && state.timestamp != 0L -> {
        timestamp = state.timestamp ?: 0L
        loadItems()
      }
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadItems()
      }
    }
  }

  private fun loadItems(resetScroll: Boolean = false) {
    itemsJob?.cancel()
    itemsJob = viewModelScope.launch {
      val loadingJob = launch {
        delay(1000)
        loadingState.update { true }
      }
      try {
        val items = getHistoryItemsCase.loadItems(searchQuery)
        itemsState.update { items }
        resetScrollEvent.update { Event(resetScroll) }
        loadingState.update { false }
      } finally {
        loadingJob.cancel()
      }
    }
  }

  fun setPeriod(period: HistoryPeriod) {
    settingsRepository.filters.historyShowsPeriod = period
    loadItems(resetScroll = true)
  }

  fun findMissingImage(item: HistoryListItem, force: Boolean) {
    check(item is HistoryListItem.Episode)
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

  fun findMissingTranslation(item: HistoryListItem) {
    check(item is HistoryListItem.Episode)
    val showId = item.show.ids.trakt
    val language = translationsRepository.getLanguage()
    if (item.translations?.show != null ||
      language == Config.DEFAULT_LANGUAGE ||
      translationJobs.contains(showId)
    ) {
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
        translationJobs.remove(showId)
      }
    }
    translationJobs.add(showId)
  }

  private fun updateItem(newItem: HistoryListItem.Episode) {
    itemsState.update { value ->
      value.toMutableList().apply {
        findReplace(newItem) { it.isSameAs(newItem) }
      }
    }
  }

  val uiState = combine(
    itemsState,
    loadingState,
    resetScrollEvent
  ) { s1, s2, s3 ->
    HistoryUiState(
      items = s1,
      isLoading = s2,
      resetScrollEvent = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = HistoryUiState()
  )
}
