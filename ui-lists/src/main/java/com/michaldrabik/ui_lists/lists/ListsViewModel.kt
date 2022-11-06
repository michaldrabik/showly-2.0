package com.michaldrabik.ui_lists.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.events.TraktSyncError
import com.michaldrabik.ui_base.events.TraktSyncSuccess
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_lists.lists.cases.MainListsCase
import com.michaldrabik.ui_lists.lists.cases.SortOrderListsCase
import com.michaldrabik.ui_lists.lists.helpers.ListsItemImage
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.michaldrabik.ui_base.events.Event as EventSync

@HiltViewModel
class ListsViewModel @Inject constructor(
  private val mainCase: MainListsCase,
  private val sortCase: SortOrderListsCase,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val eventsManager: EventsManager,
  workManager: WorkManager,
) : ViewModel() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<ListsItem>?>(null)
  private val scrollState = MutableStateFlow(Event(false))
  private val sortOrderState = MutableStateFlow<Pair<SortOrder, SortType>?>(null)
  private val syncingState = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      eventsManager.events.collect { onEvent(it) }
    }
    workManager.getWorkInfosByTagLiveData(TraktSyncWorker.TAG_ID).observeForever { work ->
      syncingState.value = work.any { it.state == WorkInfo.State.RUNNING }
    }
  }

  fun loadItems(
    resetScroll: Boolean,
    searchQuery: String? = null,
  ) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      sortOrderState.value = sortCase.loadSortOrder()
      itemsState.value = mainCase.loadLists(searchQuery)
      scrollState.value = Event(resetScroll)
    }
  }

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    viewModelScope.launch {
      sortCase.setSortOrder(sortOrder, sortType)
      loadItems(resetScroll = true)
    }
  }

  fun loadMissingImage(item: ListsItem, itemImage: ListsItemImage, force: Boolean) {
    viewModelScope.launch {
      try {
        val imageType = itemImage.image.type

        val image =
          when {
            itemImage.isShow() -> showImagesProvider.loadRemoteImage(itemImage.show!!, imageType, force)
            itemImage.isMovie() -> movieImagesProvider.loadRemoteImage(itemImage.movie!!, imageType, force)
            else -> throw IllegalStateException()
          }

        val updateItemImage = itemImage.copy(image = image)
        val updateImages = item.images.toMutableList()
        updateImages.findReplace(updateItemImage) { it.getIds()?.trakt == updateItemImage.getIds()?.trakt }
        updateItem(item.copy(images = updateImages))
      } catch (t: Throwable) {
        val updateItemImage = itemImage.copy(image = Image.createUnavailable(itemImage.image.type))
        val updateImages = item.images.toMutableList()
        updateImages.findReplace(updateItemImage) { it.getIds()?.trakt == updateItemImage.getIds()?.trakt }
        updateItem(item.copy(images = updateImages))
      }
    }
  }

  private fun updateItem(newItem: ListsItem) {
    val currentItems = uiState.value.items?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(newItem) { it.list.id == newItem.list.id }
    itemsState.value = currentItems
  }

  private fun onEvent(event: EventSync) {
    if (event in arrayOf(TraktSyncError, TraktSyncAuthError, TraktSyncSuccess)) {
      loadItems(resetScroll = true)
    }
  }

  val uiState = combine(
    itemsState,
    scrollState,
    sortOrderState,
    syncingState
  ) { s1, s2, s3, s4 ->
    ListsUiState(
      items = s1,
      resetScroll = s2,
      sortOrder = s3,
      isSyncing = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ListsUiState()
  )
}
