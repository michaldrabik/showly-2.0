package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.UiModel

data class DiscoverUiModel(
  val showLoading: Boolean? = null,
  val applyUiCache: UiCache? = null,
  val resetScroll: Boolean? = null,
  val error: Error? = null
) : UiModel()