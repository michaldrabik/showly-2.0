package com.michaldrabik.ui_gallery

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageStatus
import kotlinx.coroutines.launch
import javax.inject.Inject

class FanartGalleryViewModel @Inject constructor(
  private val imagesCase: FanartLoadImagesCase
) : BaseViewModel<FanartGalleryUiModel>() {

  fun loadImage(id: IdTrakt, type: ImageFamily) {
    viewModelScope.launch {
      val image = imagesCase.loadInitialImage(id, type)
      if (image.status == ImageStatus.AVAILABLE) {
        uiState = FanartGalleryUiModel(listOf(image))
      }
      try {
        val allImages = imagesCase.loadAllImages(id, type, image)
        uiState = FanartGalleryUiModel(allImages)
      } catch (t: Throwable) {
        // NOOP Don't show rest of the gallery
      }
    }
  }
}
