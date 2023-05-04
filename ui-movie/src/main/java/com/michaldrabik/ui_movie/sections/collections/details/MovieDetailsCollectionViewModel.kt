package com.michaldrabik.ui_movie.sections.collections.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_movie.sections.collections.details.cases.MovieDetailsCollectionLoadCase
import com.michaldrabik.ui_movie.sections.collections.details.cases.MovieDetailsCollectionMoviesCase
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsCollectionViewModel @Inject constructor(
  private val collectionDetailsCase: MovieDetailsCollectionLoadCase,
  private val collectionMoviesCase: MovieDetailsCollectionMoviesCase,
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val itemsState = MutableStateFlow<List<MovieDetailsCollectionItem>?>(null)

  fun loadCollection(collectionId: IdTrakt) {
    viewModelScope.launch {
      try {
        val headerItem = collectionDetailsCase.loadCollection(collectionId)
        itemsState.value = listOf(headerItem)
        loadCollectionMovies(collectionId)
      } catch (error: Throwable) {
        // TODO Collection not available
        rethrowCancellation(error)
      }
    }
  }

  private fun loadCollectionMovies(collectionId: IdTrakt) {
    viewModelScope.launch {
      try {
        val moviesItems = collectionMoviesCase.loadCollectionMovies(collectionId)
        itemsState.update {
          it?.toMutableList()?.apply { addAll(moviesItems) }
        }
      } catch (error: Throwable) {
        // TODO Error
        rethrowCancellation(error)
      }
    }
  }

  private fun updateItem(newItem: MovieDetailsCollectionItem) {
    val currentItems = itemsState.value?.toMutableList()
    currentItems?.findReplace(newItem) { it.getId() == newItem.getId() }
    itemsState.value = currentItems
  }

  val uiState = combine(
    itemsState
  ) { s1 ->
    MovieDetailsCollectionUiState(
      items = s1[0]
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsCollectionUiState()
  )
}
