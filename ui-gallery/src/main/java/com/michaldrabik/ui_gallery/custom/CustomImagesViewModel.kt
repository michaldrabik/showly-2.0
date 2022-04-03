package com.michaldrabik.ui_gallery.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomImagesViewModel @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val posterImageState = MutableStateFlow<Image?>(null)
  private val fanartImageState = MutableStateFlow<Image?>(null)

  fun loadPoster(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily,
  ) {
    viewModelScope.launch {
      val image = when (family) {
        SHOW -> showImagesProvider.findCustomImage(showTraktId.id, POSTER)
        MOVIE -> movieImagesProvider.findCustomImage(movieTraktId.id, POSTER)
        else -> error("Invalid type")
      }
      posterImageState.value = image
    }
  }

  fun loadFanart(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily,
  ) {
    viewModelScope.launch {
      val image = when (family) {
        SHOW -> showImagesProvider.findCustomImage(showTraktId.id, FANART)
        MOVIE -> movieImagesProvider.findCustomImage(movieTraktId.id, FANART)
        else -> error("Invalid type")
      }
      fanartImageState.value = image
    }
  }

  fun deletePoster(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily,
  ) {
    viewModelScope.launch {
      when (family) {
        SHOW -> showImagesProvider.deleteCustomImage(showTraktId, family, POSTER)
        MOVIE -> movieImagesProvider.deleteCustomImage(movieTraktId, family, POSTER)
        else -> error("Invalid type")
      }
      posterImageState.value = Image.createUnavailable(POSTER, family)
    }
  }

  fun deleteFanart(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily,
  ) {
    viewModelScope.launch {
      when (family) {
        SHOW -> showImagesProvider.deleteCustomImage(showTraktId, family, FANART)
        MOVIE -> movieImagesProvider.deleteCustomImage(movieTraktId, family, FANART)
        else -> error("Invalid type")
      }
      fanartImageState.value = Image.createUnavailable(FANART, family)
    }
  }

  val uiState = combine(
    posterImageState,
    fanartImageState
  ) { posterImageState, fanartImageState ->
    CustomImagesUiState(
      posterImage = posterImageState,
      fanartImage = fanartImageState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CustomImagesUiState()
  )
}
