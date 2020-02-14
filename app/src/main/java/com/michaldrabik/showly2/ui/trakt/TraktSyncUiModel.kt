package com.michaldrabik.showly2.ui.trakt

import com.michaldrabik.showly2.ui.common.UiModel

data class TraktSyncUiModel(
  val isProgress: Boolean? = null,
  val isAuthorized: Boolean? = null,
  val authError: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as TraktSyncUiModel).copy(
      isProgress = newModel.isProgress ?: isProgress,
      isAuthorized = newModel.isAuthorized ?: isAuthorized,
      authError = newModel.authError ?: authError
    )
}
