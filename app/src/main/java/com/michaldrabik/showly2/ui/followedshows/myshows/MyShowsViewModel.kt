package com.michaldrabik.showly2.ui.followedshows.myshows

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.ALL
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.ENDED
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor
) : BaseViewModel<MyShowsUiModel>() {

  fun loadShows() {
    viewModelScope.launch {
      val settings = interactor.loadSettings()
      val shows = interactor.loadAllShows().map { toListItemAsync(it) }.awaitAll()

      val allShows = interactor.filterSectionShows(shows, ALL)

      val runningShows =
        if (settings.myShowsRunningIsEnabled) interactor.filterSectionShows(shows, RUNNING)
        else emptyList()

      val endedShows =
        if (settings.myShowsEndedIsEnabled) interactor.filterSectionShows(shows, ENDED)
        else emptyList()

      val incomingShows =
        if (settings.myShowsIncomingIsEnabled) interactor.filterSectionShows(shows, COMING_SOON)
        else emptyList()

      val recentShows =
        if (settings.myShowsRecentIsEnabled) interactor.loadRecentShows().map { toListItemAsync(it, FANART) }.awaitAll()
        else emptyList()

      uiState = MyShowsUiModel(
        recentShowsVisible = settings.myShowsRecentIsEnabled,
        recentShows = recentShows,
        runningShows = MyShowsBundle(
          runningShows,
          RUNNING,
          settings.myShowsRunningSortBy,
          settings.myShowsRunningIsCollapsed,
          settings.myShowsRunningIsEnabled
        ),
        endedShows = MyShowsBundle(
          endedShows,
          ENDED,
          settings.myShowsEndedSortBy,
          settings.myShowsEndedIsCollapsed,
          settings.myShowsEndedIsEnabled
        ),
        incomingShows = MyShowsBundle(
          incomingShows,
          COMING_SOON,
          settings.myShowsIncomingSortBy,
          settings.myShowsIncomingIsCollapsed,
          settings.myShowsIncomingIsEnabled
        ),
        allShows = MyShowsBundle(allShows, ALL, settings.myShowsAllSortBy, null, true)
      )
    }
  }

  fun loadSortedSection(section: MyShowsSection, order: SortOrder) {
    viewModelScope.launch {
      interactor.setSectionSortOrder(section, order)

      val allShows = interactor.loadAllShows().map { toListItemAsync(it) }.awaitAll()
      val shows = interactor.filterSectionShows(allShows, section)

      uiState = when (section) {
        ALL -> MyShowsUiModel(allShows = MyShowsBundle(shows, section, order, null, true))
        RUNNING -> MyShowsUiModel(runningShows = MyShowsBundle(shows, section, order, null, true))
        ENDED -> MyShowsUiModel(endedShows = MyShowsBundle(shows, section, order, null, true))
        COMING_SOON -> MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, order, null, true))
        else -> error("Should not be used here.")
      }
    }
  }

  fun loadCollapsedSection(section: MyShowsSection, isCollapsed: Boolean) {
    viewModelScope.launch {
      interactor.setSectionCollapsed(section, isCollapsed)

      val allShows = interactor.loadAllShows().map { toListItemAsync(it) }.awaitAll()
      val shows = interactor.filterSectionShows(allShows, section)

      uiState = when (section) {
        ALL -> MyShowsUiModel(allShows = MyShowsBundle(shows, section, null, isCollapsed, false))
        RUNNING -> MyShowsUiModel(runningShows = MyShowsBundle(shows, section, null, isCollapsed, true))
        ENDED -> MyShowsUiModel(endedShows = MyShowsBundle(shows, section, null, isCollapsed, true))
        COMING_SOON -> MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, null, isCollapsed, true))
        else -> error("Should not be used here.")
      }
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

  private fun CoroutineScope.toListItemAsync(show: Show, type: ImageType = POSTER) =
    async {
      val image = interactor.findCachedImage(show, type)
      MyShowsListItem(show, image)
    }
}
