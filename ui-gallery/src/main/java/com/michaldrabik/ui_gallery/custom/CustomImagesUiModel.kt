package com.michaldrabik.ui_gallery.custom

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Image

data class CustomImagesUiModel(
  val posterImage: Image? = null,
  val fanartImage: Image? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CustomImagesUiModel).copy(
      posterImage = newModel.posterImage ?: posterImage,
      fanartImage = newModel.fanartImage ?: fanartImage
    )
}
