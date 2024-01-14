package com.michaldrabik.ui_gallery.fanart

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType

data class ArtGalleryUiState(
  val images: List<Image>? = null,
  val type: ImageType = ImageType.FANART,
  val pickedImage: Event<Image>? = null,
  val isLoading: Boolean = false,
)
