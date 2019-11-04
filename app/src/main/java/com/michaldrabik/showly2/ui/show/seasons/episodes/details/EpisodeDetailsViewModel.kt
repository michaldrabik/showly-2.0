package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(
  private val imagesManager: ImagesManager
) : BaseViewModel<EpisodeDetailsUiModel>() {

  fun loadImage(tvdb: IdTvdb) {
    viewModelScope.launch {
      try {
        _uiStream.value = EpisodeDetailsUiModel(imageLoading = true)
        val ids = Ids.EMPTY.copy(tvdb = tvdb)
        val episode = Episode.EMPTY.copy(ids = ids)
        val episodeImage = imagesManager.loadRemoteImage(episode)
        _uiStream.value = EpisodeDetailsUiModel(image = episodeImage)
      } catch (t: Throwable) {
        _uiStream.value = EpisodeDetailsUiModel(imageLoading = false)
      }
    }
  }
}
