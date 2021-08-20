package com.michaldrabik.ui_gallery.fanart

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType

data class ArtGalleryUiState(
  val images: List<Image>? = null,
  val type: ImageType = ImageType.FANART,
  val pickedImage: ActionEvent<Image>? = null,
  val isLoading: Boolean = false,
)
