package com.michaldrabik.showly2.ui.followedshows.myshows

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.ENDED
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.utilities.extensions.replaceItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel<MyShowsUiModel>() {

  fun loadMyShows() = viewModelScope.launch {
    val recentShows = interactor.loadRecentShows().map {
      val image = interactor.findCachedImage(it, FANART)
      MyShowsListItem(it, image)
    }

    val runningShows = interactor.loadShows(RUNNING).map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }

    val endedShows = interactor.loadShows(ENDED).map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }

    val incomingShows = interactor.loadShows(COMING_SOON).map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }

    val settings = interactor.loadSettings()

    uiState = MyShowsUiModel(
      recentShows = recentShows,
      runningShows = MyShowsBundle(runningShows, RUNNING, settings.myShowsRunningSortBy),
      endedShows = MyShowsBundle(endedShows, ENDED, settings.myShowsEndedSortBy),
      incomingShows = MyShowsBundle(incomingShows, COMING_SOON, settings.myShowsIncomingSortBy),
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
      RUNNING -> MyShowsUiModel(runningShows = MyShowsBundle(shows, section, order))
      ENDED -> MyShowsUiModel(endedShows = MyShowsBundle(shows, section, order))
      COMING_SOON -> MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, order))
    }
  }

  fun loadMissingImage(item: MyShowsListItem, force: Boolean) {

    fun updateItem(new: MyShowsListItem) {
      val incoming = uiState?.incomingShows?.items?.toMutableList() ?: mutableListOf()
      val ended = uiState?.endedShows?.items?.toMutableList() ?: mutableListOf()
      val running = uiState?.runningShows?.items?.toMutableList() ?: mutableListOf()

      listOf(incoming, ended, running).forEach { section ->
        section.find { it.show.ids.trakt == new.show.ids.trakt }?.let {
          section.replaceItem(it, new)
        }
      }

      uiState = uiState?.copy(
        incomingShows = uiState?.incomingShows?.copy(items = incoming.toList()),
        endedShows = uiState?.endedShows?.copy(items = ended.toList()),
        runningShows = uiState?.runningShows?.copy(items = running.toList())
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
