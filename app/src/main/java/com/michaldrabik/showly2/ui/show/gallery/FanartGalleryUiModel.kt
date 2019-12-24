package com.michaldrabik.showly2.ui.show.gallery

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.UiModel

data class FanartGalleryUiModel(
  val images: List<Image>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as FanartGalleryUiModel).copy(
      images = newModel.images ?: images
    )
}
