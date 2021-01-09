package com.michaldrabik.ui_gallery.custom

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageSource.CUSTOM
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CustomImagesViewModel @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) : BaseViewModel<CustomImagesUiModel>() {

  fun loadPoster(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    url: String?,
    family: ImageFamily
  ) {
    viewModelScope.launch {
      if (url.isNullOrBlank()) {
        try {
          val image = when (family) {
            SHOW -> showImagesProvider.findCustomImage(showTraktId.id, POSTER)
            MOVIE -> movieImagesProvider.findCustomImage(movieTraktId.id, POSTER)
            else -> error("Invalid type")
          }
          image?.let {
            uiState = CustomImagesUiModel(posterImage = it)
          }
        } catch (error: Throwable) {
          Timber.e(error)
        }
      } else {
        val ids = Ids.EMPTY.copy(trakt = IdTrakt(showTraktId.id))
        val image = Image.createAvailable(ids, POSTER, family, url, CUSTOM)
        uiState = CustomImagesUiModel(posterImage = image)
      }
    }
  }

  fun loadFanart(
    showTraktId: IdTrakt,
    movieTraktId: IdTrakt,
    url: String?,
    family: ImageFamily
  ) {
    viewModelScope.launch {
      if (url.isNullOrBlank()) {
        try {
          val image = when (family) {
            SHOW -> showImagesProvider.findCustomImage(showTraktId.id, FANART)
            MOVIE -> movieImagesProvider.findCustomImage(movieTraktId.id, FANART)
            else -> error("Invalid type")
          }
          image?.let {
            uiState = CustomImagesUiModel(fanartImage = it)
          }
        } catch (error: Throwable) {
          Timber.e(error)
        }
      } else {
        val ids = Ids.EMPTY.copy(trakt = IdTrakt(showTraktId.id))
        val image = Image.createAvailable(ids, FANART, family, url, CUSTOM)
        uiState = CustomImagesUiModel(fanartImage = image)
      }
    }
  }
}
