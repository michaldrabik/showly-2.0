package com.michaldrabik.ui_progress.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.locale.AppLocale
import com.michaldrabik.ui_progress.main.EpisodeCheckActionUiEvent
import com.michaldrabik.ui_progress.main.ProgressMainUiState
import com.michaldrabik.ui_progress.main.RequestWidgetsUpdate
import com.michaldrabik.ui_progress.progress.cases.ProgressFiltersCase
import com.michaldrabik.ui_progress.progress.cases.ProgressHeadersCase
import com.michaldrabik.ui_progress.progress.cases.ProgressItemsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressSortOrderCase
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
  private val itemsCase: ProgressItemsCase,
  private val headersCase: ProgressHeadersCase,
  private val sortOrderCase: ProgressSortOrderCase,
  private val filtersCase: ProgressFiltersCase,
  private val imagesProvider: ShowImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val workManager: WorkManager,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<ProgressListItem>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val overscrollState = MutableStateFlow(false)
  private val scrollState = MutableStateFlow(Event(false))
  private val sortOrderState = MutableStateFlow<Event<Triple<SortOrder, SortType, Boolean>>?>(null)

  private var searchQuery: String? = null
  private var timestamp = 0L

  fun onParentState(state: ProgressMainUiState) {
    when {
      this.timestamp != state.timestamp && state.timestamp != 0L -> {
        this.timestamp = state.timestamp ?: 0L
        loadItems(resetScroll = state.resetScroll?.consume() == true)
      }
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadItems(resetScroll = state.searchQuery.isNullOrBlank())
      }
    }
  }

  private fun loadItems(resetScroll: Boolean = false) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      loadingState.value = true

      val items = itemsCase.loadItems(searchQuery ?: "")
      itemsState.value = items
      loadingState.value = false
      scrollState.value = Event(resetScroll)
      overscrollState.value = userTraktManager.isAuthorized() && items.isNotEmpty()

      eventChannel.send(RequestWidgetsUpdate)
    }
  }

  fun loadSortOrder() {
    if (itemsState.value?.isEmpty() == true) return
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      sortOrderState.value = Event(sortOrder)
    }
  }

  fun onEpisodeChecked(episode: ProgressListItem.Episode) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode.requireEpisode(), episode.requireSeason(), episode.show)
      eventChannel.send(
        EpisodeCheckActionUiEvent(
          episode = bundle,
          dateSelectionType = settingsRepository.progressDateSelectionType
        )
      )
    }
  }

  fun findMissingImage(item: ProgressListItem, force: Boolean) {
    check(item is ProgressListItem.Episode)
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

  fun findMissingTranslation(item: ProgressListItem) {
    check(item is ProgressListItem.Episode)
    val locale = translationsRepository.getLocale()
    if (item.translations?.show != null || locale == AppLocale.default()) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, locale)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType, newAtTop: Boolean) {
    sortOrderCase.setSortOrder(sortOrder, sortType, newAtTop)
    loadItems(resetScroll = true)
  }

  fun setUpcomingFilter(isEnabled: Boolean) {
    filtersCase.setUpcomingFilter(isEnabled)
    loadItems(resetScroll = true)
  }

  fun setOnHoldFilter(isEnabled: Boolean) {
    filtersCase.setOnHoldFilter(isEnabled)
    loadItems(resetScroll = true)
  }

  fun toggleHeaderCollapsed(headerType: ProgressListItem.Header.Type) {
    headersCase.toggleHeaderCollapsed(headerType)
    loadItems()
  }

  fun startTraktSync() {
    TraktSyncWorker.scheduleOneOff(
      workManager,
      isImport = true,
      isExport = true,
      isSilent = false
    )
  }

  private fun updateItem(newItem: ProgressListItem) {
    itemsState.update { value ->
      value?.toMutableList()?.apply {
        findReplace(newItem) { it.isSameAs(newItem) }
      }
    }
    scrollState.update { Event(false) }
  }

  val uiState = combine(
    itemsState,
    scrollState,
    sortOrderState,
    loadingState,
    overscrollState
  ) { s1, s2, s3, s4, s5 ->
    ProgressUiState(
      items = s1,
      scrollReset = s2,
      sortOrder = s3,
      isLoading = s4,
      isOverScrollEnabled = s5
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ProgressUiState()
  )
}
