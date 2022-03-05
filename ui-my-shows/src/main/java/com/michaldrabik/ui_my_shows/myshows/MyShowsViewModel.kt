package com.michaldrabik.ui_my_shows.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.ALL
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.main.FollowedShowsUiState
import com.michaldrabik.ui_my_shows.myshows.cases.MyShowsLoadShowsCase
import com.michaldrabik.ui_my_shows.myshows.cases.MyShowsRatingsCase
import com.michaldrabik.ui_my_shows.myshows.cases.MyShowsSortingCase
import com.michaldrabik.ui_my_shows.myshows.cases.MyShowsTranslationsCase
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyShowsViewModel @Inject constructor(
  private val loadShowsCase: MyShowsLoadShowsCase,
  private val sortingCase: MyShowsSortingCase,
  private val ratingsCase: MyShowsRatingsCase,
  private val translationsCase: MyShowsTranslationsCase,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider
) : ViewModel() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<MyShowsItem>?>(null)
  private val itemsUpdateState = MutableStateFlow<Event<List<Type>?>?>(null)

  private var searchQuery: String? = null

  fun onParentState(state: FollowedShowsUiState) {
    when {
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        val resetScrolls =
          if (state.searchQuery.isNullOrBlank()) listOf(Type.ALL_SHOWS_ITEM)
          else emptyList()
        loadShows(resetScroll = resetScrolls)
      }
    }
  }

  fun loadShows(resetScroll: List<Type>? = null) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      val settings = settingsRepository.load()
      val shows = loadShowsCase.loadAllShows().map { toListItemAsync(Type.ALL_SHOWS_ITEM, it) }.awaitAll()
      val seasons = loadShowsCase.loadSeasonsForShows(shows.map { it.show.traktId })

      val sortingOrder = sortingCase.loadSectionSortOrder(ALL)
      val allShows = loadShowsCase.filterSectionShows(shows, seasons, ALL, sortingOrder, searchQuery)

      val runningShows =
        if (settings.myShowsRunningIsEnabled) {
          val sortOrder = sortingCase.loadSectionSortOrder(WATCHING)
          loadShowsCase.filterSectionShows(shows, seasons, WATCHING, sortOrder)
        } else {
          emptyList()
        }

      val endedShows =
        if (settings.myShowsEndedIsEnabled) {
          val sortOrder = sortingCase.loadSectionSortOrder(FINISHED)
          loadShowsCase.filterSectionShows(shows, seasons, FINISHED, sortOrder)
        } else {
          emptyList()
        }

      val incomingShows =
        if (settings.myShowsIncomingIsEnabled) {
          val sortOrder = sortingCase.loadSectionSortOrder(UPCOMING)
          loadShowsCase.filterSectionShows(shows, seasons, UPCOMING, sortOrder)
        } else {
          emptyList()
        }

      val recentShows = if (settings.myShowsRecentIsEnabled) {
        loadShowsCase.loadRecentShows().map { toListItemAsync(Type.RECENT_SHOWS, it, ImageType.FANART) }.awaitAll()
      } else {
        emptyList()
      }

      val isNotSearching = searchQuery.isNullOrBlank()
      val listItems = mutableListOf<MyShowsItem>()
      listItems.run {
        if (isNotSearching && recentShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(RECENTS, recentShows.count(), null))
          add(MyShowsItem.createRecentsSection(recentShows))
        }
        if (isNotSearching && runningShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(WATCHING, runningShows.count(), sortingCase.loadSectionSortOrder(WATCHING)))
          add(MyShowsItem.createHorizontalSection(WATCHING, runningShows))
        }
        if (isNotSearching && incomingShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(UPCOMING, incomingShows.count(), sortingCase.loadSectionSortOrder(UPCOMING)))
          add(MyShowsItem.createHorizontalSection(UPCOMING, incomingShows))
        }
        if (isNotSearching && endedShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(FINISHED, endedShows.count(), sortingCase.loadSectionSortOrder(FINISHED)))
          add(MyShowsItem.createHorizontalSection(FINISHED, endedShows))
        }
        if (allShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(ALL, allShows.count(), sortingCase.loadSectionSortOrder(ALL)))
          addAll(allShows)
        }
      }

      itemsState.value = listItems
      itemsUpdateState.value = Event(resetScroll)

      loadRatings(listItems, resetScroll)
    }
  }

  private fun loadRatings(items: List<MyShowsItem>, resetScroll: List<Type>?) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        itemsState.value = listItems
        itemsUpdateState.value = Event(resetScroll)
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "MyShowsViewModel::loadRatings()")
      }
    }
  }

  fun setSectionSortOrder(section: MyShowsSection, sortOrder: SortOrder, sortType: SortType) {
    viewModelScope.launch {
      sortingCase.setSectionSortOrder(section, sortOrder, sortType)
      loadShows(resetScroll = listOf(Type.HORIZONTAL_SHOWS))
    }
  }

  fun loadMissingImage(item: MyShowsItem, force: Boolean) {
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

  fun loadSectionMissingItem(item: MyShowsItem, itemSection: MyShowsItem.HorizontalSection, force: Boolean) {

    fun updateItem(newItem: MyShowsItem, newSection: MyShowsItem.HorizontalSection) {
      val items = uiState.value.items?.toMutableList()
      val section = items?.find { it.horizontalSection?.section == newSection.section }?.horizontalSection

      val sectionItems = section?.items?.toMutableList() ?: mutableListOf()
      sectionItems.findReplace(newItem) { it.isSameAs(newItem) }

      val newSecWithItems = section?.copy(items = sectionItems)
      items?.findReplace(newItem.copy(horizontalSection = newSecWithItems)) { it.horizontalSection?.section == newSection.section }

      itemsState.value = items
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true), itemSection)
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image), itemSection)
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)), itemSection)
      }
    }
  }

  fun loadMissingTranslation(item: MyShowsItem) {
    if (item.translation != null || translationsCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsCase.loadTranslation(item.show, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "MyShowsViewModel::loadMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: MyShowsItem) {
    val items = uiState.value.items?.toMutableList()
    items?.findReplace(new) { it.isSameAs(new) }
    itemsState.value = items
  }

  private fun CoroutineScope.toListItemAsync(
    itemType: Type,
    show: Show,
    type: ImageType = POSTER,
  ) = async {
    val image = imagesProvider.findCachedImage(show, type)
    val translation = translationsCase.loadTranslation(show, true)
    MyShowsItem(itemType, null, null, null, show, image, false, translation)
  }

  val uiState = combine(
    itemsState,
    itemsUpdateState
  ) { itemsState, itemsUpdateState ->
    MyShowsUiState(
      items = itemsState,
      resetScrollMap = itemsUpdateState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MyShowsUiState()
  )
}
