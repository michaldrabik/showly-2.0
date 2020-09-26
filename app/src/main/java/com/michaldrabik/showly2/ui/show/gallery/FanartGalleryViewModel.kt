package com.michaldrabik.showly2.ui.show.gallery

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image.Status
import kotlinx.coroutines.launch
import javax.inject.Inject

class FanartGalleryViewModel @Inject constructor(
  private val imagesCase: FanartLoadImagesCase
) : BaseViewModel<FanartGalleryUiModel>() {

  fun loadImage(id: IdTrakt) {
    viewModelScope.launch {
      val image = imagesCase.loadInitialImage(id)
      if (image.status == Status.AVAILABLE) {
        uiState = FanartGalleryUiModel(listOf(image))
      }
      try {
        val allImages = imagesCase.loadAllImages(id, image)
        uiState = FanartGalleryUiModel(allImages)
      } catch (t: Throwable) {
        // NOOP Don't show rest of the gallery
      }
    }
  }
}
