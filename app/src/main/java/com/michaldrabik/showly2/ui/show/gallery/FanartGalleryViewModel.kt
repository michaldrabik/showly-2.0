package com.michaldrabik.showly2.ui.show.gallery

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Image.Status
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

class FanartGalleryViewModel @Inject constructor(
  private val interactor: FanartGalleryInteractor
) : BaseViewModel<FanartGalleryUiModel>() {

  fun loadImage(id: IdTrakt) {
    viewModelScope.launch {
      val image = interactor.loadInitialImage(id)
      if (image.status == Status.AVAILABLE) {
        uiState = FanartGalleryUiModel(listOf(image))
      }
      try {
        val allImages = interactor.loadAllImages(id, image)
        uiState = FanartGalleryUiModel(allImages)
      } catch (t: Throwable) {
        // NOOP Don't show rest of the gallery
      }
    }
  }
}
