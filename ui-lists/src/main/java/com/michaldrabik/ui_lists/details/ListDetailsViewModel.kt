package com.michaldrabik.ui_lists.details

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.cases.ListDetailsItemsCase
import com.michaldrabik.ui_lists.details.cases.ListDetailsMainCase
import com.michaldrabik.ui_lists.details.cases.ListDetailsSortCase
import com.michaldrabik.ui_lists.details.cases.ListDetailsTipsCase
import com.michaldrabik.ui_lists.details.cases.ListDetailsTranslationsCase
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Tip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailsViewModel @Inject constructor(
  private val mainCase: ListDetailsMainCase,
  private val itemsCase: ListDetailsItemsCase,
  private val translationsCase: ListDetailsTranslationsCase,
  private val sortCase: ListDetailsSortCase,
  private val tipsCase: ListDetailsTipsCase,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) : BaseViewModel() {

  private val listDetailsState = MutableStateFlow<CustomList?>(null)
  private val listItemsState = MutableStateFlow<List<ListDetailsItem>?>(null)
  private val listDeleteState = MutableStateFlow<Event<Boolean>?>(null)
  private val manageModeState = MutableStateFlow(false)
  private val quickRemoveState = MutableStateFlow(false)
  private val scrollState = MutableStateFlow<Event<Boolean>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val filtersVisibleState = MutableStateFlow(false)

  fun loadDetails(id: Long) {
    viewModelScope.launch {
      val list = mainCase.loadDetails(id)
      val listItems = itemsCase.loadItems(list)

      listDetailsState.value = list
      listItemsState.value = listItems
      manageModeState.value = false
      filtersVisibleState.value = listItems.isNotEmpty()
      quickRemoveState.value = mainCase.isQuickRemoveEnabled(list)

      val tip = Tip.LIST_ITEM_SWIPE_DELETE
      if (listItems.isNotEmpty() && !tipsCase.isTipShown(tip)) {
        _messageChannel.send(MessageEvent.info(tip.textResId, indefinite = true))
        tipsCase.setTipShown(tip)
      }
    }
  }

  fun loadMissingImage(item: ListDetailsItem, force: Boolean) {
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image =
          when {
            item.isShow() -> showImagesProvider.loadRemoteImage(item.requireShow(), item.image.type, force)
            item.isMovie() -> movieImagesProvider.loadRemoteImage(item.requireMovie(), item.image.type, force)
            else -> throw IllegalStateException()
          }
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingTranslation(item: ListDetailsItem) {
    if (item.translation != null || translationsCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsCase.loadTranslation(item, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ListDetailsViewModel::loadMissingTranslation()")
      }
    }
  }

  fun setReorderMode(listId: Long, isReorderMode: Boolean) {
    viewModelScope.launch {
      if (isReorderMode) {
        val list = mainCase.loadDetails(listId).copy(
          sortByLocal = SortOrder.RANK,
          sortHowLocal = SortType.ASCENDING,
          filterTypeLocal = Mode.getAll()
        )
        val listItems = itemsCase.loadItems(list).map { it.copy(isManageMode = true) }
        listItemsState.value = listItems
        manageModeState.value = true
        filtersVisibleState.value = false
        scrollState.value = Event(false)
      } else {
        val list = mainCase.loadDetails(listId)
        val listItems = itemsCase.loadItems(list).map { it.copy(isManageMode = false) }
        listItemsState.value = listItems
        manageModeState.value = false
        filtersVisibleState.value = true
        scrollState.value = Event(true)
      }
    }
  }

  fun updateRanks(listId: Long, items: List<ListDetailsItem>) {
    viewModelScope.launch {
      val updatedItems = mainCase.updateRanks(listId, items)
      listItemsState.value = updatedItems
    }
  }

  fun setSortOrder(id: Long, sortOrder: SortOrder, sortType: SortType) {
    viewModelScope.launch {
      val list = sortCase.setSortOrder(id, sortOrder, sortType)

      val currentItems = uiState.value.listItems?.toList() ?: emptyList()
      val sortedItems = itemsCase.sortItems(
        currentItems,
        list.sortByLocal,
        list.sortHowLocal,
        list.filterTypeLocal
      )

      listDetailsState.value = list
      listItemsState.value = sortedItems
      scrollState.value = Event(true)
    }
  }

  fun setFilterTypes(listId: Long, types: List<Mode>) {
    viewModelScope.launch {
      val list = sortCase.setFilterTypes(listId, types)
      val sortedItems = itemsCase.loadItems(list)

      listDetailsState.value = list
      listItemsState.value = sortedItems
      filtersVisibleState.value = true
      scrollState.value = Event(true)
    }
  }

  fun deleteList(listId: Long, removeFromTrakt: Boolean) {
    viewModelScope.launch {
      try {
        if (removeFromTrakt) {
          loadingState.value = true
        }
        mainCase.deleteList(listId, removeFromTrakt)
        loadingState.value = false
        listDeleteState.value = Event(true)
      } catch (error: Throwable) {
        loadingState.value = false
        _messageChannel.send(MessageEvent.error(R.string.errorCouldNotDeleteList))
      }
    }
  }

  fun deleteListItem(listId: Long, item: ListDetailsItem) {
    viewModelScope.launch {
      val type =
        when {
          item.isShow() -> SHOWS
          item.isMovie() -> MOVIES
          else -> throw IllegalStateException()
        }
      itemsCase.deleteListItem(listId, item.getTraktId(), type)
      loadDetails(listId)
    }
  }

  private fun updateItem(newItem: ListDetailsItem) {
    val currentItems = uiState.value.listItems?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(newItem) { it.id == newItem.id }
    listItemsState.value = currentItems
  }

  val uiState = combine(
    listDetailsState,
    listItemsState,
    manageModeState,
    quickRemoveState,
    loadingState,
    listDeleteState,
    scrollState,
    filtersVisibleState
  ) { s1, s2, s3, s4, s5, s6, s7, s8 ->
    ListDetailsUiState(
      listDetails = s1,
      listItems = s2,
      isManageMode = s3,
      isQuickRemoveEnabled = s4,
      isLoading = s5,
      deleteEvent = s6,
      resetScroll = s7,
      isFiltersVisible = s8
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ListDetailsUiState()
  )
}
