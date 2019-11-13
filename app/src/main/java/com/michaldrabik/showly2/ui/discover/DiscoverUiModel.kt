package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.UiModel

data class DiscoverUiModel(
  val showLoading: Boolean? = null,
  val applyUiCache: UiCache? = null,
  val resetScroll: Boolean? = null,
  val error: Error? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as DiscoverUiModel).copy(
      showLoading = newModel.showLoading ?: showLoading,
      applyUiCache = newModel.applyUiCache ?: applyUiCache,
      resetScroll = newModel.resetScroll ?: resetScroll,
      error = newModel.error ?: error
    )
}