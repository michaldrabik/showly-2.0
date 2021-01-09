package com.michaldrabik.ui_gallery.fanart

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType

data class ArtGalleryUiModel(
  val images: List<Image>? = null,
  val type: ImageType? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ArtGalleryUiModel).copy(
      images = newModel.images ?: images,
      type = newModel.type ?: type
    )
}
