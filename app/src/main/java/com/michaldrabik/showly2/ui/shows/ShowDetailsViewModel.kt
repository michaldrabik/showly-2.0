package com.michaldrabik.showly2.ui.shows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.async
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
        val show = interactor.loadShowDetails(id)
        uiStream.value = ShowDetailsUiModel(show, imageLoading = true)

        val image = async { interactor.loadBackgroundImage(show) }
        val nextEpisodeAsync = async {
          delay(350) //Added for UI transition to finish nicely
          interactor.loadNextEpisode(show)
        }

        uiStream.value = ShowDetailsUiModel(image = image.await())
        val nextEpisode = nextEpisodeAsync.await()
        if (nextEpisode?.firstAired != null) {
          uiStream.value = ShowDetailsUiModel(nextEpisode = nextEpisode)
        }
      } catch (error: Throwable) {
        //TODO
        uiStream.value = ShowDetailsUiModel(imageLoading = false)
      }
    }
  }
}
