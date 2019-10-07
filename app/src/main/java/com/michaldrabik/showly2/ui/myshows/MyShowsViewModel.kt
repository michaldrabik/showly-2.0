package com.michaldrabik.showly2.ui.myshows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<MyShowsUiModel>() }

  fun loadMyShows() {
    viewModelScope.launch {
      try {
        val shows = interactor.loadMyShows()

        val recentShows = shows.recentsShows.map {
          val image = interactor.findCachedImage(it, FANART)
          MyShowsListItem(it, image)
        }

        val runningShows = shows.runningShows.map {
          val image = interactor.findCachedImage(it, POSTER)
          MyShowsListItem(it, image)
        }

        val endedShows = shows.endedShows.map {
          val image = interactor.findCachedImage(it, POSTER)
          MyShowsListItem(it, image)
        }

        val incomingShows = shows.incomingShows.map {
          val image = interactor.findCachedImage(it, POSTER)
          MyShowsListItem(it, image)
        }

        uiStream.value = MyShowsUiModel(
          recentShows,
          runningShows,
          endedShows,
          incomingShows,
          listPosition = uiCache.myShowsListPosition
        )
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