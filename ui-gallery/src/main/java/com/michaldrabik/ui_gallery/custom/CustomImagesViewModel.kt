package com.michaldrabik.ui_gallery.custom

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import kotlinx.coroutines.launch
import javax.inject.Inject

class CustomImagesViewModel @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) : BaseViewModel<CustomImagesUiModel>() {

  fun loadPoster(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily
  ) {
    viewModelScope.launch {
      val image = when (family) {
        SHOW -> showImagesProvider.findCustomImage(showTraktId.id, POSTER)
        MOVIE -> movieImagesProvider.findCustomImage(movieTraktId.id, POSTER)
        else -> error("Invalid type")
      }
      image?.let {
        uiState = CustomImagesUiModel(posterImage = it)
      }
    }
  }

  fun loadFanart(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily
  ) {
    viewModelScope.launch {
      val image = when (family) {
        SHOW -> showImagesProvider.findCustomImage(showTraktId.id, FANART)
        MOVIE -> movieImagesProvider.findCustomImage(movieTraktId.id, FANART)
        else -> error("Invalid type")
      }
      image?.let {
        uiState = CustomImagesUiModel(fanartImage = it)
      }
    }
  }

  fun deletePoster(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily
  ) {
    viewModelScope.launch {
      when (family) {
        SHOW -> showImagesProvider.deleteCustomImage(showTraktId, family, POSTER)
        MOVIE -> movieImagesProvider.deleteCustomImage(movieTraktId, family, POSTER)
        else -> error("Invalid type")
      }
      uiState = CustomImagesUiModel(posterImage = Image.createUnavailable(POSTER, family))
    }
  }

  fun deleteFanart(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    family: ImageFamily
  ) {
    viewModelScope.launch {
      when (family) {
        SHOW -> showImagesProvider.deleteCustomImage(showTraktId, family, FANART)
        MOVIE -> movieImagesProvider.deleteCustomImage(movieTraktId, family, FANART)
        else -> error("Invalid type")
      }
      uiState = CustomImagesUiModel(fanartImage = Image.createUnavailable(FANART, family))
    }
  }
}
