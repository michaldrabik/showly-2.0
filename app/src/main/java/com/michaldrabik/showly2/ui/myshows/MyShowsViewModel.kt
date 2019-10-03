package com.michaldrabik.showly2.ui.myshows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyShowsViewModel @Inject constructor(
  private val interactor: MyShowsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<MyShowsUiModel>() }

  fun loadMyShows() {
    viewModelScope.launch {
      launch { loadRecents() }
    }
  }

  private suspend fun loadRecents() {
    try {
      val shows = interactor.loadRecentShows()
        .map {
          val image = interactor.findCachedImage(it, FANART)
          MyShowListItem(it, image)
        }
      uiStream.value = MyShowsUiModel(recentShows = shows)
    } catch (t: Throwable) {
      TODO()
    }
  }
}