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
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsListItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<MyShowsUiModel>() }

  fun loadMyShows() = viewModelScope.launch {
    try {
      val recentShows = interactor.loadRecentShows().map {
        val image = interactor.findCachedImage(it, FANART)
        MyShowsListItem(it, image)
      }

      val runningSortOrder = interactor.loadSortOrder(RUNNING)
      val runningShows = interactor.loadRunningShows().map {
        val image = interactor.findCachedImage(it, POSTER)
        MyShowsListItem(it, image)
      }

      val endedSortOrder = interactor.loadSortOrder(ENDED)
      val endedShows = interactor.loadEndedShows().map {
        val image = interactor.findCachedImage(it, POSTER)
        MyShowsListItem(it, image)
      }

      val incomingSortOrder = interactor.loadSortOrder(COMING_SOON)
      val incomingShows = interactor.loadIncomingShows().map {
        val image = interactor.findCachedImage(it, POSTER)
        MyShowsListItem(it, image)
      }

      uiStream.value = MyShowsUiModel(
        recentShows = recentShows,
        runningShows = MyShowsBundle(runningShows, RUNNING, runningSortOrder),
        endedShows = MyShowsBundle(endedShows, ENDED, endedSortOrder),
        incomingShows = MyShowsBundle(incomingShows, COMING_SOON, incomingSortOrder),
        listPosition = uiCache.myShowsListPosition
      )
    } catch (t: Throwable) {
      TODO()
    }
  }

  fun loadSortedSection(section: MyShowsSection, order: SortOrder) {
    viewModelScope.launch {
      try {
        interactor.setSectionSortOrder(section, order)
        when (section) {
          RUNNING -> {
            val shows = interactor.loadRunningShows().map {
              val image = interactor.findCachedImage(it, POSTER)
              MyShowsListItem(it, image)
            }
            uiStream.value = MyShowsUiModel(runningShows = MyShowsBundle(shows, section, order))
          }
          ENDED -> {
            val shows = interactor.loadEndedShows().map {
              val image = interactor.findCachedImage(it, POSTER)
              MyShowsListItem(it, image)
            }
            uiStream.value = MyShowsUiModel(endedShows = MyShowsBundle(shows, section, order))
          }
          COMING_SOON -> {
            val shows = interactor.loadIncomingShows().map {
              val image = interactor.findCachedImage(it, POSTER)
              MyShowsListItem(it, image)
            }
            uiStream.value = MyShowsUiModel(incomingShows = MyShowsBundle(shows, section, order))
          }
        }
      } catch (t: Throwable) {
        TODO()
      }
    }
  }

  fun loadMissingImage(item: MyShowsListItem, force: Boolean) {
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
  }

  fun saveListPosition(position: Int, offset: Int) {
    uiCache.myShowsListPosition = Pair(position, offset)
  }
}
