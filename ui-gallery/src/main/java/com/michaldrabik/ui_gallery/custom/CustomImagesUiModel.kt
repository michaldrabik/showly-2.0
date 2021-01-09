package com.michaldrabik.ui_gallery.custom

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Image

data class CustomImagesUiModel(
  val image: Image? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CustomImagesUiModel).copy(
      image = newModel.image ?: image,
    )
}
