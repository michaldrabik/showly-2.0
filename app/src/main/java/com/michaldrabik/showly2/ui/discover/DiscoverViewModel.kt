package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val cloud: Cloud
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<DiscoverUiModel>() }

  fun loadTrendingShows() {
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(showLoading = true)
      try {
        val shows = cloud.traktApi.fetchTrendingShows().map { DiscoverListItem(it) }
        uiStream.value = DiscoverUiModel(trendingShows = shows)
      } catch (t: Throwable) {
        //TODO Errors
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
      }
    }
  }

  fun loadMissingImage(ids: Ids) {
    viewModelScope.launch {
      try {
        val token =
          "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1NjcwNjM2NDUsImlkIjoiU2hvd2x5IDIuMCIsIm9yaWdfaWF0IjoxNTY2OTc3MjQ1LCJ1c2VyaWQiOjQyNjU2MCwidXNlcm5hbWUiOiJkcmFiIn0.Cr-wuXJgHpe4VQhESk1Bixp41ZIBTeMdAelQXs6T178i94XTLwlP5rSLYthKjm7ok8z60fEWYlNFbOyois_3x1dYAmET8f5IOzawhtiy8Bh5LlE4kkjfaNtPK8xJJQPrOJx8-WPZ9hb6vTXs_jIAOiraMPBb7EBkj5C1MTurwp6NShB7vTvx7nAHGVfC20XA1Lot858qoLP1HAS_xqzaiZrISg2AQGjg1lmqAZCu06kJEVFWwm45LWgljx9H7Z-PGTYcgYLFlJ-qG4AqQNHpFBIaIO2i6N8xD9uAnuLyP6o34QkiZCpngUddmpzGVct1olsE-ersxQYvWA8jHJbmsg"
        val images = cloud.tvdbApi.fetchPosterImages(token, ids.tvdb)
        val imageUrl = images.firstOrNull()?.thumbnail ?: ""
        uiStream.value = DiscoverUiModel(missingImage = Pair(ids, imageUrl))
      } catch (t: Throwable) {
        //TODO Errors
      }
    }
  }
}