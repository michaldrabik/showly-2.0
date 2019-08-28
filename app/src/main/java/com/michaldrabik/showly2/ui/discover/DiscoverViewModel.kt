package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.showly2.model.ImageUrl
import com.michaldrabik.showly2.model.ImageUrl.Status.AVAILABLE
import com.michaldrabik.showly2.model.ImageUrl.Status.UNAVAILABLE
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
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
        val shows = cloud.traktApi.fetchTrendingShows().map {
          val cachedImageUrl = imagesRepository.getPosterImageUrl(it.ids.tvdb)
          DiscoverListItem(it, ImageUrl.fromString(cachedImageUrl))
        }
        uiStream.value = DiscoverUiModel(trendingShows = shows)
      } catch (t: Throwable) {
        //TODO Errors
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
      }
    }
  }

  fun loadMissingImage(ids: Ids, force: Boolean) {
    val cachedImageUrl = ImageUrl.fromString(imagesRepository.getPosterImageUrl(ids.tvdb))
    if (cachedImageUrl.status == AVAILABLE && !force) {
      uiStream.value = DiscoverUiModel(missingImage = Pair(ids, cachedImageUrl))
      return
    }

    viewModelScope.launch {
      try {
        checkAuthorization()
        val images = cloud.tvdbApi.fetchPosterImages(userRepository.tvdbToken, ids.tvdb)
        val imageUrl = ImageUrl.fromString(images.firstOrNull()?.thumbnail)
        if (imageUrl.status != UNAVAILABLE) {
          imagesRepository.savePosterImageUrl(ids.tvdb, imageUrl.url)
        } else {
          imagesRepository.removePosterImageUrl(ids.tvdb)
        }
        uiStream.value = DiscoverUiModel(missingImage = Pair(ids, imageUrl))
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