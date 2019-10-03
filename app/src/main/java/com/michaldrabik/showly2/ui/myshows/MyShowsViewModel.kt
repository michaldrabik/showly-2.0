package com.michaldrabik.showly2.ui.myshows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<MyShowsUiModel>() }

  fun loadMyShows() {
    viewModelScope.launch {
      try {
        val shows = interactor.loadMyShows()

        val recentShows = shows.recentsShows.map {
          val image = interactor.findCachedImage(it, FANART)
          MyShowListItem(it, image)
        }

        val runningShows = shows.runningShows.map {
          val image = interactor.findCachedImage(it, POSTER)
          MyShowListItem(it, image)
        }

        val endedShows = shows.endedShows.map {
          val image = interactor.findCachedImage(it, POSTER)
          MyShowListItem(it, image)
        }

        uiStream.value = MyShowsUiModel(
          recentShows = recentShows,
          runningShows = runningShows,
          endedShows = endedShows
        )
      } catch (t: Throwable) {
        TODO()
      }
    }
  }
}