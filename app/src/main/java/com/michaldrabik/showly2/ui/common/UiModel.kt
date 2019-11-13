package com.michaldrabik.showly2.ui.common

abstract class UiModel {
  open fun update(newModel: UiModel): UiModel = this
}