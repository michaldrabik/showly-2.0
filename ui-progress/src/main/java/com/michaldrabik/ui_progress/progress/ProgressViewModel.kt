package com.michaldrabik.ui_progress.progress

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress.main.EpisodeCheckActionUiEvent
import com.michaldrabik.ui_progress.main.ProgressMainUiState
import com.michaldrabik.ui_progress.progress.cases.ProgressItemsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressRatingsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressSortOrderCase
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
  private val itemsCase: ProgressItemsCase,
  private val sortOrderCase: ProgressSortOrderCase,
  private val ratingsCase: ProgressRatingsCase,
  private val imagesProvider: ShowImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<ProgressListItem>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val overscrollState = MutableStateFlow(false)
  private val scrollState = MutableStateFlow(Event(false))
  private val sortOrderState = MutableStateFlow<Event<Pair<SortOrder, SortType>>?>(null)

  private val language by lazy { translationsRepository.getLanguage() }
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
      val isQuickRate = ratingsCase.isQuickRateEnabled()
      _eventChannel.send(EpisodeCheckActionUiEvent(bundle, isQuickRate))
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
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ProgressViewModel::findMissingTranslation()")
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    sortOrderCase.setSortOrder(sortOrder, sortType)
    loadItems(resetScroll = true)
  }

  private fun updateItem(new: ProgressListItem) {
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
    scrollState.value = Event(false)
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
