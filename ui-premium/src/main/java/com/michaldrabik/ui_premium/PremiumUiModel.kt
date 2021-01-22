package com.michaldrabik.ui_premium

import com.michaldrabik.ui_base.UiModel

data class PremiumUiModel(
  val isLoading: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as PremiumUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading

    )
}
