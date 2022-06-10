package com.michaldrabik.ui_show.sections.related

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.ui_show.sections.related.cases.ShowDetailsRelatedCase
import com.michaldrabik.ui_show.sections.related.recycler.RelatedListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShowDetailsRelatedViewModel @Inject constructor(
  private val relatedCase: ShowDetailsRelatedCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val imagesProvider: ShowImagesProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var show: Show

  private val loadingState = MutableStateFlow(true)
  private val relatedItemsState = MutableStateFlow<List<RelatedListItem>?>(null)

  fun loadRelatedShows(show: Show) {
    if (this::show.isInitialized) return
    this.show = show
    viewModelScope.launch {
      try {
        val (myShows, watchlistShows) = myShowsCase.getAllIds()
        val related = relatedCase.loadRelatedShows(show).map {
          val image = imagesProvider.findCachedImage(it, ImageType.POSTER)
          RelatedListItem(
            show = it,
            image = image,
            isFollowed = it.traktId in myShows,
            isWatchlist = it.traktId in watchlistShows
          )
        }
        relatedItemsState.value = related
      } catch (error: Throwable) {
        relatedItemsState.value = emptyList()
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        loadingState.value = false
      }
    }
    Timber.d("Loading related shows...")
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = uiState.value.relatedShows?.toMutableList()
      currentItems?.findReplace(new) { it isSameAs new }
      relatedItemsState.value = currentItems
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  val uiState = combine(
    loadingState,
    relatedItemsState
  ) { s1, s2 ->
    ShowDetailsRelatedUiState(
      isLoading = s1,
      relatedShows = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsRelatedUiState()
  )
}
