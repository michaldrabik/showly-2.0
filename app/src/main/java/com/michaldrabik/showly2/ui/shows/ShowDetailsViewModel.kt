package com.michaldrabik.showly2.ui.shows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.shows.related.RelatedListItem
import com.michaldrabik.showly2.ui.shows.seasons.SeasonListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
  private val interactor: ShowDetailsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<ShowDetailsUiModel>() }

  fun loadShowDetails(id: Long) {
    //TODO Errors
    viewModelScope.launch {
      uiStream.value = ShowDetailsUiModel(showLoading = true)

      val showAsync = async { interactor.loadShowDetails(id) }
      val nextEpisodeAsync = async { interactor.loadNextEpisode(id) }
      val show = showAsync.await()
      val nextEpisode = nextEpisodeAsync.await()

      uiStream.value = ShowDetailsUiModel(show = show, nextEpisode = nextEpisode, showLoading = false)

      launch { loadBackgroundImage(show) }
      launch { loadActors(show) }
      launch { loadSeasons(show) }
      launch { loadRelatedShows(show) }
    }
  }

  private suspend fun loadBackgroundImage(show: Show) {
    try {
      uiStream.value = ShowDetailsUiModel(imageLoading = true)
      val backgroundImage = interactor.loadBackgroundImage(show)
      uiStream.value = ShowDetailsUiModel(image = backgroundImage, imageLoading = false)
    } catch (e: Exception) {
      uiStream.value = ShowDetailsUiModel(image = Image.createUnavailable(FANART), imageLoading = false)
    }
  }

  private suspend fun loadActors(show: Show) {
    try {
      val actors = interactor.loadActors(show)
      uiStream.value = ShowDetailsUiModel(actors = actors)
    } catch (e: Exception) {
      uiStream.value = ShowDetailsUiModel(actors = emptyList())
    }
  }

  private suspend fun loadSeasons(show: Show) {
    try {
      val seasons = interactor.loadSeasons(show)
      uiStream.value = ShowDetailsUiModel(seasons = seasons.map { SeasonListItem(it) })
    } catch (e: Exception) {
      uiStream.value = ShowDetailsUiModel(seasons = emptyList())
    }
  }

  private suspend fun loadRelatedShows(show: Show) {
    try {
      val relatedShows = interactor.loadRelatedShows(show).map {
        val image = interactor.findCachedImage(it, ImageType.POSTER)
        RelatedListItem(it, image)
      }
      uiStream.value = ShowDetailsUiModel(relatedShows = relatedShows)
    } catch (e: Exception) {
      uiStream.value = ShowDetailsUiModel(relatedShows = emptyList())
    }
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {
    viewModelScope.launch {
      uiStream.value = ShowDetailsUiModel(updateRelatedShow = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value =
          ShowDetailsUiModel(updateRelatedShow = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        uiStream.value =
          ShowDetailsUiModel(updateRelatedShow = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }
}
