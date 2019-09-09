package com.michaldrabik.showly2.ui.shows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
  private val interactor: ShowDetailsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<ShowDetailsUiModel>() }

  fun loadShowDetails(id: Long) {
    viewModelScope.launch {
      try {
        uiStream.value = ShowDetailsUiModel(showLoading = true)
        val show = interactor.loadShowDetails(id)
        uiStream.value = ShowDetailsUiModel(show, showLoading = false, imageLoading = true)

        coroutineScope {
          val image = async { interactor.loadBackgroundImage(show) }
          val actors = async { interactor.loadActors(show) }
          val nextEpisodeAsync = async {
            delay(250) //Added for UI transition to finish nicely
            interactor.loadNextEpisode(show)
          }

          uiStream.value = ShowDetailsUiModel(image = image.await())
          uiStream.value = ShowDetailsUiModel(actors = actors.await())

          val nextEpisode = nextEpisodeAsync.await()
          if (nextEpisode?.firstAired != null) {
            uiStream.value = ShowDetailsUiModel(nextEpisode = nextEpisode)
          }
        }
      } catch (error: Throwable) {
        //TODO
        uiStream.value = ShowDetailsUiModel(imageLoading = false)
      }
    }
  }
}
