package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Genre
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.*
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val interactor: DiscoverInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

  val discoverShowsStream by lazy { MutableLiveData<List<DiscoverListItem>>() }
  val uiStream by lazy { MutableLiveData<DiscoverUiModel>() }

  fun loadDiscoverShows(skipCache: Boolean = false) {
    uiStream.value = DiscoverUiModel(uiCache = uiCache)
    viewModelScope.launch {
      val progress = launch {
        delay(750)
        uiStream.value = DiscoverUiModel(showLoading = true)
      }
      try {
        val shows = interactor.loadDiscoverShows(uiCache.discoverActiveGenres, skipCache)
        onShowsLoaded(shows)
      } catch (t: Throwable) {
        onError(Error(t))
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
        progress.cancel()
      }
    }
  }

  private suspend fun onShowsLoaded(shows: List<Show>) {
    val items = shows.mapIndexed { index, show ->
      val itemType =
        when (index) {
          in (0..500 step 14) -> FANART_WIDE
          in (5..500 step 14), in (9..500 step 14) -> FANART
          else -> POSTER
        }
      val image = interactor.findCachedImage(show, itemType)
      DiscoverListItem(show, image)
    }
    discoverShowsStream.value = items
  }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(updateListItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value =
          DiscoverUiModel(updateListItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        uiStream.value =
          DiscoverUiModel(updateListItem = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun saveUiPositions(searchPosition: Float, chipsPosition: Float, activeGenres: List<Genre>) {
    uiCache.discoverSearchPosition = searchPosition
    uiCache.discoverChipsPosition = chipsPosition
    uiCache.discoverActiveGenres.run {
      clear()
      addAll(activeGenres)
    }
  }

  fun clearCache() = uiCache.clear()

  private fun onError(error: Error) {
    uiStream.value = DiscoverUiModel(error = error)
  }
}