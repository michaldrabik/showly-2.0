package com.michaldrabik.showly2.ui.shows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.shows.related.RelatedListItem
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
          launch {
            val image = interactor.loadBackgroundImage(show)
            uiStream.value = ShowDetailsUiModel(image = image)
          }

          launch {
            val actors = interactor.loadActors(show)
            uiStream.value = ShowDetailsUiModel(actors = actors)
          }

          launch {
            val relatedShows = interactor.loadRelatedShows(show).map {
              val image = interactor.findCachedImage(it, POSTER)
              RelatedListItem(it, image)
            }
            uiStream.value = ShowDetailsUiModel(relatedShows = relatedShows)
          }

          val nextEpisode = interactor.loadNextEpisode(show)
          if (nextEpisode?.firstAired != null) {
            delay(250)
            uiStream.value = ShowDetailsUiModel(nextEpisode = nextEpisode)
          }
        }
      } catch (error: Throwable) {
        //TODO
        uiStream.value = ShowDetailsUiModel(imageLoading = false)
      }
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
