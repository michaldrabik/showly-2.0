package com.michaldrabik.ui_gallery.fanart

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageSource.CUSTOM
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtGalleryViewModel @Inject constructor(
  private val imagesCase: ArtLoadImagesCase
) : BaseViewModel<ArtGalleryUiModel>() {

  fun loadImages(id: IdTrakt, family: ImageFamily, type: ImageType) {
    viewModelScope.launch {
      val image = imagesCase.loadInitialImage(id, family, type)
      if (image.status == ImageStatus.AVAILABLE) {
        uiState = ArtGalleryUiModel(listOf(image), type)
      }
      try {
        uiState = ArtGalleryUiModel(isLoading = true)
        val allImages = imagesCase.loadAllImages(id, family, type, image)
        uiState = ArtGalleryUiModel(allImages, type, isLoading = false)
      } catch (t: Throwable) {
        uiState = ArtGalleryUiModel(isLoading = false)
      }
    }
  }

  fun saveCustomImage(id: IdTrakt, image: Image, family: ImageFamily, type: ImageType) {
    viewModelScope.launch {
      imagesCase.saveCustomImage(id, image, family, type)
      uiState = ArtGalleryUiModel(pickedImage = ActionEvent(image))
    }
  }

  fun addImageFromUrl(imageUrl: String, family: ImageFamily, type: ImageType) {
    if (imageUrl.isBlank()) return

    val currentImages = uiState?.images?.toMutableList() ?: mutableListOf()
    val image = Image.createAvailable(Ids.EMPTY, type, family, imageUrl.trim(), CUSTOM)
    currentImages.add(0, image)

    uiState = ArtGalleryUiModel(images = currentImages)
  }
}
