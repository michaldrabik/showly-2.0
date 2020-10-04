package com.michaldrabik.ui_show.gallery

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Image

data class FanartGalleryUiModel(
  val images: List<Image>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as FanartGalleryUiModel).copy(
      images = newModel.images ?: images
    )
}
