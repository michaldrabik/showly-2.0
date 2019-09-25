package com.michaldrabik.showly2.ui.show

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.FollowedState
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.show.related.RelatedListItem
import com.michaldrabik.showly2.ui.show.seasons.SeasonListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
  private val interactor: ShowDetailsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<ShowDetailsUiModel>() }

  private var isSeasonsLoaded = false

  fun loadShowDetails(id: Long) {
    //TODO Errors
    viewModelScope.launch {
      uiStream.value = ShowDetailsUiModel(showLoading = true)
      val show = interactor.loadShowDetails(id)
      val isFollowed = interactor.isFollowed(show)
      uiStream.value = ShowDetailsUiModel(
        show = show,
        showLoading = false,
        isFollowed = FollowedState(isFollowed = isFollowed, withAnimation = false)
      )

      launch { loadNextEpisode(show) }
      launch { loadBackgroundImage(show) }
      launch { loadActors(show) }
      launch { loadSeasons(show) }
      launch { loadRelatedShows(show) }
    }
  }

  private suspend fun loadNextEpisode(show: Show) {
    try {
      val episode = interactor.loadNextEpisode(show.ids.trakt)
      uiStream.value = ShowDetailsUiModel(nextEpisode = episode)
    } catch (e: Exception) {
      //NOOP
    }
  }

  private suspend fun loadBackgroundImage(show: Show) {
    try {
      val backgroundImage = interactor.loadBackgroundImage(show)
      uiStream.value = ShowDetailsUiModel(image = backgroundImage)
    } catch (e: Exception) {
      uiStream.value = ShowDetailsUiModel(image = Image.createUnavailable(FANART))
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
      isSeasonsLoaded = true
      uiStream.value = ShowDetailsUiModel(seasons = seasons.map { SeasonListItem(it) })
    } catch (e: Exception) {
      uiStream.value = ShowDetailsUiModel(seasons = emptyList())
    }
  }

  private suspend fun loadRelatedShows(show: Show) {
    try {
      delay(750)
      val relatedShows = interactor.loadRelatedShows(show).map {
        val image = interactor.findCachedImage(it, POSTER)
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

  fun toggleFollowedShow(show: Show) {
    if (!isSeasonsLoaded) return

    viewModelScope.launch {
      val isFollowed = interactor.isFollowed(show)
      when {
        isFollowed -> interactor.removeFromFollowed(show)
        else -> interactor.addToFollowed(show)
      }
      val state = FollowedState(isFollowed = !isFollowed, withAnimation = true)
      uiStream.value = ShowDetailsUiModel(isFollowed = state)
    }
  }
}
