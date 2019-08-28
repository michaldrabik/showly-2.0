package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.model.ImageUrl
import com.michaldrabik.showly2.model.ImageUrl.Status.AVAILABLE
import com.michaldrabik.showly2.model.ImageUrl.Status.UNAVAILABLE
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem.Type.FANART
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem.Type.POSTER
import com.michaldrabik.storage.repository.ImagesRepository
import com.michaldrabik.storage.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val cloud: Cloud,
  private val userRepository: UserRepository,
  private val imagesRepository: ImagesRepository
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<DiscoverUiModel>() }

  fun loadTrendingShows() {
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(showLoading = true)
      try {
        val shows = cloud.traktApi.fetchTrendingShows()
        prepareTrendingItems(shows)
      } catch (t: Throwable) {
        //TODO Errors
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
      }
    }
  }

  private fun prepareTrendingItems(shows: List<Show>) {
    val items = shows.mapIndexed { index, show ->
      val itemType =
        when (index) {
          in (0..200 step 16), in (9..200 step 16) -> FANART
          else -> POSTER
        }
      val cachedImageUrl = imagesRepository.getImageUrl(show.ids.tvdb, itemType.name)
      DiscoverListItem(show, ImageUrl.fromString(cachedImageUrl), type = itemType)
    }
    uiStream.value = DiscoverUiModel(trendingShows = items)
  }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {
    val tvdbId = item.show.ids.tvdb
    val cachedImageUrl = ImageUrl.fromString(imagesRepository.getImageUrl(tvdbId, item.type.name))
    if (cachedImageUrl.status == AVAILABLE && !force) {
      uiStream.value = DiscoverUiModel(updateListItem = item.copy(imageUrl = cachedImageUrl))
      return
    }
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(updateListItem = item.copy(isLoading = true))
      try {
        checkAuthorization()

        val images =
          when (item.type) {
            POSTER -> cloud.tvdbApi.fetchPosterImages(userRepository.tvdbToken, tvdbId)
            FANART -> cloud.tvdbApi.fetchFanartImages(userRepository.tvdbToken, tvdbId)
          }

        val imageUrl =
          when (item.type) {
            POSTER -> ImageUrl.fromString(images.firstOrNull()?.thumbnail)
            FANART -> ImageUrl.fromString(images.firstOrNull()?.fileName)
          }
        if (imageUrl.status != UNAVAILABLE) {
          imagesRepository.saveImageUrl(tvdbId, imageUrl.url, item.type.name)
        } else {
          imagesRepository.removeImageUrl(tvdbId, item.type.name)
        }

        uiStream.value = DiscoverUiModel(
          updateListItem = item.copy(imageUrl = imageUrl, isLoading = false)
        )
      } catch (t: Throwable) {
        //TODO Errors
      }
    }
  }

  private suspend fun checkAuthorization() {
    if (!userRepository.isTvdbAuthorized) {
      val token = cloud.tvdbApi.authorize()
      userRepository.tvdbToken = token.token
    }
  }
}