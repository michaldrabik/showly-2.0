package com.michaldrabik.showly2.ui.myshows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.*
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.myshows.helpers.ResultType.*
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<MyShowsUiModel>() }
  private lateinit var searchJob: Job

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

    uiStream.value = MyShowsUiModel(
      recentShows = recentShows,
      runningShows = MyShowsBundle(runningShows, RUNNING, settings.myShowsRunningSortBy),
      endedShows = MyShowsBundle(endedShows, ENDED, settings.myShowsEndedSortBy),
      incomingShows = MyShowsBundle(incomingShows, COMING_SOON, settings.myShowsIncomingSortBy),
      mainListPosition = uiCache.myShowsListPosition,
      sectionsPositions = uiCache.myShowsSectionPositions
    )
  }

  fun loadSortedSection(section: MyShowsSection, order: SortOrder) = viewModelScope.launch {
    interactor.setSectionSortOrder(section, order)
    val shows = interactor.loadShows(section).map {
      val image = interactor.findCachedImage(it, POSTER)
      MyShowsListItem(it, image)
    }
    uiStream.value = when (section) {
      RUNNING -> MyShowsUiModel(runningShows = MyShowsBundle(shows, section, order))
      ENDED -> MyShowsUiModel(endedShows = MyShowsBundle(shows, section, order))
      COMING_SOON -> MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, order))
    }
  }

  fun loadMissingImage(item: MyShowsListItem, force: Boolean) =
    viewModelScope.launch {
      uiStream.value = MyShowsUiModel(updateListItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value =
          MyShowsUiModel(updateListItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        uiStream.value =
          MyShowsUiModel(updateListItem = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }


  fun searchMyShows(query: String) {
    if (query.trim().isBlank()) {
      if (this::searchJob.isInitialized) searchJob.cancel()
      val result = MyShowsSearchResult(emptyList(), EMPTY)
      uiStream.value = MyShowsUiModel(searchResult = result)
      return
    }
    if (this::searchJob.isInitialized) searchJob.cancel()
    searchJob = viewModelScope.launch {
      val results = interactor.searchMyShows(query)
        .map {
          val image = interactor.findCachedImage(it, FANART)
          MyShowsListItem(it, image)
        }
      val type = if (results.isEmpty()) NO_RESULTS else RESULTS
      val searchResult = MyShowsSearchResult(results, type)
      uiStream.value = MyShowsUiModel(searchResult = searchResult)
    }
  }

  fun saveListPosition(
    position: Int,
    sectionPositions: Map<MyShowsSection, Pair<Int, Int>>
  ) {
    uiCache.myShowsListPosition = Pair(position, 0)
    uiCache.myShowsSectionPositions.putAll(sectionPositions)
  }

  fun clearCache() = interactor.clearCache()
}
