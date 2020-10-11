package com.michaldrabik.ui_my_shows.myshows

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
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
import com.michaldrabik.ui_my_shows.myshows.cases.MyShowsLoadShowsCase
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val loadShowsCase: MyShowsLoadShowsCase
) : BaseViewModel<MyShowsUiModel>() {

  fun loadShows(notifyListsUpdate: Boolean = false) {
    viewModelScope.launch {
      val settings = loadShowsCase.loadSettings()
      val shows = loadShowsCase.loadAllShows().map { toListItemAsync(MyShowsItem.Type.ALL_SHOWS_ITEM, it) }.awaitAll()
      val seasons = loadShowsCase.loadSeasonsForShows(shows.map { it.show.traktId })

      val allShows = loadShowsCase.filterSectionShows(shows, seasons, ALL)

      val runningShows =
        if (settings.myShowsRunningIsEnabled) loadShowsCase.filterSectionShows(shows, seasons, WATCHING)
        else emptyList()

      val endedShows =
        if (settings.myShowsEndedIsEnabled) loadShowsCase.filterSectionShows(shows, seasons, FINISHED)
        else emptyList()

      val incomingShows =
        if (settings.myShowsIncomingIsEnabled) loadShowsCase.filterSectionShows(shows, seasons, UPCOMING)
        else emptyList()

      val recentShows = if (settings.myShowsRecentIsEnabled) {
        loadShowsCase.loadRecentShows().map { toListItemAsync(MyShowsItem.Type.RECENT_SHOWS, it, ImageType.FANART) }.awaitAll()
      } else {
        emptyList()
      }

      val listItems = mutableListOf<MyShowsItem>()
      listItems.run {
        if (recentShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(RECENTS, recentShows.count(), null))
          add(MyShowsItem.createRecentsSection(recentShows))
        }
        if (runningShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(WATCHING, runningShows.count(), loadShowsCase.loadSortOrder(WATCHING)))
          add(MyShowsItem.createHorizontalSection(WATCHING, runningShows))
        }
        if (incomingShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(UPCOMING, incomingShows.count(), loadShowsCase.loadSortOrder(UPCOMING)))
          add(MyShowsItem.createHorizontalSection(UPCOMING, incomingShows))
        }
        if (endedShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(FINISHED, endedShows.count(), loadShowsCase.loadSortOrder(FINISHED)))
          add(MyShowsItem.createHorizontalSection(FINISHED, endedShows))
        }
        if (allShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(ALL, allShows.count(), loadShowsCase.loadSortOrder(ALL)))
          addAll(allShows)
        }
      }

      uiState = MyShowsUiModel(listItems = listItems, notifyListsUpdate = notifyListsUpdate)
    }
  }

  fun loadSortedSection(section: MyShowsSection, order: SortOrder) {
    viewModelScope.launch {
      loadShowsCase.setSectionSortOrder(section, order)
      loadShows(notifyListsUpdate = true)
    }
  }

  fun loadMissingImage(item: MyShowsItem, force: Boolean) {

    fun updateItem(new: MyShowsItem) {
      val items = uiState?.listItems?.toMutableList() ?: mutableListOf()
      items.findReplace(new) { it.isSameAs(new) }
      uiState = uiState?.copy(listItems = items)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = loadShowsCase.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadSectionMissingItem(item: MyShowsItem, itemSection: MyShowsItem.HorizontalSection, force: Boolean) {

    fun updateItem(newItem: MyShowsItem, newSection: MyShowsItem.HorizontalSection) {
      val items = uiState?.listItems?.toMutableList() ?: mutableListOf()
      val section = items.find { it.horizontalSection?.section == newSection.section }?.horizontalSection

      val sectionItems = section?.items?.toMutableList() ?: mutableListOf()
      sectionItems.findReplace(newItem) { it.isSameAs(newItem) }

      val newSecWithItems = section?.copy(items = sectionItems)
      items.findReplace(newItem.copy(horizontalSection = newSecWithItems)) { it.horizontalSection?.section == newSection.section }

      uiState = uiState?.copy(listItems = items)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true), itemSection)
      try {
        val image = loadShowsCase.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image), itemSection)
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)), itemSection)
      }
    }
  }

  private fun CoroutineScope.toListItemAsync(
    itemType: MyShowsItem.Type,
    show: Show,
    type: ImageType = POSTER
  ) = async {
    val image = loadShowsCase.findCachedImage(show, type)
    MyShowsItem(itemType, null, null, null, show, image, false)
  }
}
