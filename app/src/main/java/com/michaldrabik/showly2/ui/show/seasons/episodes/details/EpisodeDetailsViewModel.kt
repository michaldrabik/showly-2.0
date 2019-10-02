package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(
  private val imagesManager: ImagesManager
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<EpisodeDetailsUiModel>() }

  fun loadImage(episodeTvdbId: Long) {
    viewModelScope.launch {
      try {
        uiStream.value = EpisodeDetailsUiModel(imageLoading = true)
        val ids = Ids.EMPTY.copy(tvdb = episodeTvdbId)
        val episode = Episode.EMPTY.copy(ids = ids)
        val episodeImage = imagesManager.loadRemoteImage(episode)
        uiStream.value = EpisodeDetailsUiModel(image = episodeImage)
      } catch (t: Throwable) {
        uiStream.value = EpisodeDetailsUiModel(imageLoading = false)
      }
    }
  }
}
