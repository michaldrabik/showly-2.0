package com.michaldrabik.showly2.ui.common

abstract class UiModel {
  abstract fun update(newModel: UiModel): UiModel
}