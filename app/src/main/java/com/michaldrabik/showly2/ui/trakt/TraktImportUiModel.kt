package com.michaldrabik.showly2.ui.trakt

import com.michaldrabik.showly2.ui.common.UiModel

data class TraktImportUiModel(
  val isProgress: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as TraktImportUiModel).copy(
      isProgress = newModel.isProgress ?: isProgress
    )
}