package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.showly2.model.ImageUrl
import com.michaldrabik.showly2.model.ImageUrl.Status.AVAILABLE
import com.michaldrabik.showly2.model.ImageUrl.Status.UNAVAILABLE
import com.michaldrabik.showly2.ui.common.ImageType.FANART
import com.michaldrabik.showly2.ui.common.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.storage.cache.ImagesUrlCache
import com.michaldrabik.storage.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val cloud: Cloud,
  private val userRepository: UserRepository,
  private val imagesCache: ImagesUrlCache,
  private val interactor: DiscoverInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<DiscoverUiModel>() }

  fun loadTrendingShows() {
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(showLoading = true)
      try {
        val shows = interactor.loadTrendingShows()
        onShowsLoaded(shows)
      } catch (t: Throwable) {
        onError(t)
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
      }
    }
  }

  private fun onShowsLoaded(shows: List<Show>) {
    val items = shows.mapIndexed { index, show ->
      val itemType =
        when (index) {
          in (0..200 step 16), in (9..200 step 16) -> FANART
          else -> POSTER
        }
      val cachedImageUrl = imagesCache.getImageUrl(show.ids.tvdb, itemType.key)
      DiscoverListItem(show, ImageUrl.fromString(cachedImageUrl), type = itemType)
    }
    uiStream.value = DiscoverUiModel(trendingShows = items)
  }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {
    val tvdbId = item.show.ids.tvdb
    val cachedImageUrl = ImageUrl.fromString(imagesCache.getImageUrl(tvdbId, item.type.key))
    if (cachedImageUrl.status == AVAILABLE && !force) {
      uiStream.value = DiscoverUiModel(updateListItem = item.copy(imageUrl = cachedImageUrl))
      return
    }
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(updateListItem = item.copy(isLoading = true))
      try {
        checkAuthorization()
        val images = cloud.tvdbApi.fetchImages(userRepository.tvdbToken, tvdbId, item.type.key)
        onMissingImageLoaded(item, images, tvdbId)
      } catch (t: Throwable) {
        onError(t)
        uiStream.value =
          DiscoverUiModel(updateListItem = item.copy(isLoading = false, imageUrl = ImageUrl.UNAVAILABLE))
      }
    }
  }

  private fun onMissingImageLoaded(item: DiscoverListItem, images: List<TvdbImage>, tvdbId: Long) {
    val imageUrl =
      when (item.type) {
        POSTER -> ImageUrl.fromString(images.firstOrNull()?.thumbnail)
        FANART -> ImageUrl.fromString(images.firstOrNull()?.fileName)
      }
    if (imageUrl.status != UNAVAILABLE) {
      imagesCache.saveImageUrl(tvdbId, imageUrl.url, item.type.key)
    } else {
      imagesCache.removeImageUrl(tvdbId, item.type.key)
    }
    uiStream.value =
      DiscoverUiModel(updateListItem = item.copy(imageUrl = imageUrl, isLoading = false))
  }

  private suspend fun checkAuthorization() {
    if (!userRepository.isTvdbAuthorized) {
      val token = cloud.tvdbApi.authorize()
      userRepository.tvdbToken = token.token
    }
  }

  private fun onError(error: Throwable) {
    //TODO
  }
}