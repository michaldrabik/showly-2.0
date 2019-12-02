package com.michaldrabik.showly2.ui.trakt

import com.michaldrabik.showly2.ui.common.UiModel

data class TraktImportUiModel(
  val st: String? = null
) : UiModel() {

  override fun update(newModel: UiModel): UiModel {
    return newModel
  }
}