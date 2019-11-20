package com.michaldrabik.showly2.ui.show.gallery

import com.michaldrabik.showly2.ui.common.UiModel

data class FanartGalleryUiModel(
  val stub: String? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as FanartGalleryUiModel).copy(
      stub = newModel.stub ?: stub
    )
}