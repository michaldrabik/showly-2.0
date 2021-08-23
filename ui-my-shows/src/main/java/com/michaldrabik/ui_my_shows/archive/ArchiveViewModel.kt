package com.michaldrabik.ui_my_shows.archive

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.archive.cases.ArchiveLoadShowsCase
import com.michaldrabik.ui_my_shows.archive.cases.ArchiveRatingsCase
import com.michaldrabik.ui_my_shows.archive.cases.ArchiveSortOrderCase
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
  private val sortOrderCase: ArchiveSortOrderCase,
  private val loadShowsCase: ArchiveLoadShowsCase,
  private val ratingsCase: ArchiveRatingsCase,
  private val imagesProvider: ShowImagesProvider,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<ArchiveListItem>>(emptyList())
  private val sortOrderState = MutableStateFlow<ActionEvent<SortOrder>?>(null)
  private val scrollState = MutableStateFlow<ActionEvent<Boolean>?>(null)

  val uiState = combine(
    itemsState,
    sortOrderState,
    scrollState
  ) { itemsState, sortOrderState, scrollState ->
    ArchiveUiState(
      items = itemsState,
      sortOrder = sortOrderState,
      resetScroll = scrollState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ArchiveUiState()
  )

  fun loadShows(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val items = loadShowsCase.loadShows().map {
        val image = imagesProvider.findCachedImage(it.first, POSTER)
        ArchiveListItem(it.first, image, false, it.second)
      }
      itemsState.value = items
      scrollState.value = ActionEvent(resetScroll)
      loadRatings(items, resetScroll)
    }
  }

  private fun loadRatings(items: List<ArchiveListItem>, resetScroll: Boolean) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        itemsState.value = listItems
        scrollState.value = ActionEvent(resetScroll)
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ArchiveViewModel::loadRatings()")
      }
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      sortOrderState.value = ActionEvent(sortOrder)
    }
  }

  fun loadMissingImage(item: ArchiveListItem, force: Boolean) {
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingTranslation(item: ArchiveListItem) {
    if (item.translation != null || loadShowsCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = loadShowsCase.loadTranslation(item.show, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ArchiveViewModel::loadMissingTranslation()")
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadShows(resetScroll = true)
    }
  }

  private fun updateItem(new: ArchiveListItem) {
    val currentItems = uiState.value.items.toMutableList()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
  }
}
