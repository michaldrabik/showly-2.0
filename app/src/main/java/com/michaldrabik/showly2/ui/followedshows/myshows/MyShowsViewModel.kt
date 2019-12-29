package com.michaldrabik.showly2.ui.followedshows.myshows

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.ALL
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.ENDED
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel<MyShowsUiModel>() {

  fun loadMyShows() = viewModelScope.launch {

    suspend fun List<Show>.mapToListItem() = this.map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }

    val recentShows = interactor.loadRecentShows().mapToListItem()
    val allShows = interactor.loadShows(ALL).mapToListItem()
    val runningShows = interactor.loadShows(RUNNING).mapToListItem()
    val endedShows = interactor.loadShows(ENDED).mapToListItem()
    val incomingShows = interactor.loadShows(COMING_SOON).mapToListItem()

    val settings = interactor.loadSettings()

    uiState = MyShowsUiModel(
      recentShows = recentShows,
      runningShows = MyShowsBundle(runningShows, RUNNING, settings.myShowsRunningSortBy, settings.myShowsRunningIsCollapsed),
      endedShows = MyShowsBundle(endedShows, ENDED, settings.myShowsEndedSortBy, settings.myShowsEndedIsCollapsed),
      incomingShows = MyShowsBundle(incomingShows, COMING_SOON, settings.myShowsIncomingSortBy, settings.myShowsIncomingIsCollapsed),
      allShows = MyShowsBundle(allShows, ALL, settings.myShowsAllSortBy, null),
      sectionsPositions = uiCache.myShowsSectionPositions
    )
  }

  fun loadSortedSection(section: MyShowsSection, order: SortOrder) = viewModelScope.launch {
    interactor.setSectionSortOrder(section, order)
    val shows = interactor.loadShows(section).map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }
    uiState = when (section) {
      ALL -> MyShowsUiModel(allShows = MyShowsBundle(shows, section, order, null))
      RUNNING -> MyShowsUiModel(runningShows = MyShowsBundle(shows, section, order, null))
      ENDED -> MyShowsUiModel(endedShows = MyShowsBundle(shows, section, order, null))
      COMING_SOON -> MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, order, null))
    }
  }

  fun loadCollapsedSection(section: MyShowsSection, isCollapsed: Boolean) = viewModelScope.launch {
    interactor.setSectionCollapsed(section, isCollapsed)
    val shows = interactor.loadShows(section).map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }
    uiState = when (section) {
      ALL -> MyShowsUiModel(allShows = MyShowsBundle(shows, section, null, isCollapsed))
      RUNNING -> MyShowsUiModel(runningShows = MyShowsBundle(shows, section, null, isCollapsed))
      ENDED -> MyShowsUiModel(endedShows = MyShowsBundle(shows, section, null, isCollapsed))
      COMING_SOON -> MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, null, isCollapsed))
    }
  }

  fun loadMissingImage(item: MyShowsListItem, force: Boolean) {

    fun updateItem(new: MyShowsListItem) {
      val all = uiState?.allShows?.items?.toMutableList() ?: mutableListOf()
      val incoming = uiState?.incomingShows?.items?.toMutableList() ?: mutableListOf()
      val ended = uiState?.endedShows?.items?.toMutableList() ?: mutableListOf()
      val running = uiState?.runningShows?.items?.toMutableList() ?: mutableListOf()

      listOf(all, incoming, ended, running).forEach { section ->
        section.findReplace(new) { it.isSameAs(new) }
      }

      uiState = uiState?.copy(
        incomingShows = uiState?.incomingShows?.copy(items = incoming.toList()),
        endedShows = uiState?.endedShows?.copy(items = ended.toList()),
        runningShows = uiState?.runningShows?.copy(items = running.toList()),
        allShows = uiState?.allShows?.copy(items = all.toList())
      )
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun saveListPosition(
    sectionPositions: Map<MyShowsSection, Pair<Int, Int>>
  ) {
    uiCache.myShowsSectionPositions.putAll(sectionPositions)
  }
}
