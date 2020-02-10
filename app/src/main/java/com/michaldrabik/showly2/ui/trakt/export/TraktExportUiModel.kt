package com.michaldrabik.showly2.ui.trakt.export

import com.michaldrabik.showly2.ui.common.UiModel

data class TraktExportUiModel(
  val isProgress: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as TraktExportUiModel).copy(
      isProgress = newModel.isProgress ?: isProgress
    )
}
